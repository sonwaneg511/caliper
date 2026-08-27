package com.caliper.onboarding.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.location.entity.Client;
import com.caliper.location.gmb.entity.GMBAccount;
import com.caliper.location.gmb.repository.GMBAccountRepository;
import com.caliper.location.repository.ClientRepository;
import com.caliper.onboarding.dto.AccessAccountDto;
import com.caliper.onboarding.dto.OnboardingStatusResponse;
import com.caliper.onboarding.entity.ClientOnboardingState;
import com.caliper.onboarding.entity.ConnectionStatus;
import com.caliper.onboarding.entity.OnboardingStep;
import com.caliper.onboarding.repository.ClientOnboardingStateRepository;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.utils.exception.customException.InvalidRequestException;

@Service
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);

    // ── existing dependencies ────────────────────────────────────────────────
    @Autowired
    public GMBAccountRepository gmbAccountRepository;

    @Autowired
    public ClientRepository clientRepository;

    @Autowired
    public UserClientLocMappingRepository userClientLocMappingRepository;

    // ── new dependencies ─────────────────────────────────────────────────────
    @Autowired
    private ClientOnboardingStateRepository onboardingStateRepository;

    @Autowired
    private OnboardingStepResolver stepResolver;

    // ════════════════════════════════════════════════════════════════════════
    // EXISTING METHOD — untouched
    // ════════════════════════════════════════════════════════════════════════

    public void insertAccessAccount(AccessAccountDto accessAccountDto) {

        Client existingClient = clientRepository.findByEmail(accessAccountDto.getUserId());
        String clientId = existingClient.getClientId();

        GMBAccount existingAccount = gmbAccountRepository
                .findByClientIdAndAccountName(clientId, accessAccountDto.getAccountId())
                .orElse(null);

        if (existingAccount != null) {
            existingAccount.setAccountName(accessAccountDto.getAccountName());
            existingAccount.setLastModifiedBy(accessAccountDto.getUserId());
            existingAccount.setLastModifiedDate(new Date());
            gmbAccountRepository.save(existingAccount);
        } else {
            gmbAccountRepository.save(GMBAccount.builder()
                    .accountName(accessAccountDto.getAccountName())
                    .accountId(accessAccountDto.getAccountId())
                    .clientId(clientId)
                    .lastModifiedBy(accessAccountDto.getUserId())
                    .lastModifiedDate(new Date())
                    .build());
        }

        List<Long> facebookPageIds = accessAccountDto.getFacebookPageIds();
        for (Long facebookPageId : facebookPageIds) {
            userClientLocMappingRepository.save(UserClientLocMapping.builder()
                    .userId(accessAccountDto.getUserId())
                    .clientId(clientId)
                    .facebookPageId(facebookPageId)
                    .build());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // NEW — STATE MACHINE METHODS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Called by PlanActivationService after a successful payment.
     * Creates (or upserts) the ClientOnboardingState for this client and sets
     * the initial step based on which services were purchased.
     * Idempotent — safe to call more than once (e.g., on Razorpay webhook retry).
     */
    @Transactional
    public void initializeFromPlan(String clientId, String userId, List<String> serviceKeys) {
        log.info("Initializing onboarding for clientId={} with services={}", clientId, serviceKeys);

        boolean requiresSocial = serviceKeys.stream().anyMatch(k ->
                k.trim().equalsIgnoreCase("POSTS") || k.trim().equalsIgnoreCase("REVIEWS"));
        boolean requiresCampaign = serviceKeys.stream().anyMatch(k ->
                k.trim().equalsIgnoreCase("CAMPAIGNS"));

        OnboardingStep firstStep = stepResolver.resolveStep(requiresSocial, requiresCampaign);

        // Upsert — if row already exists (e.g., webhook replay) just update it
        ClientOnboardingState state = onboardingStateRepository
                .findByClientId(clientId)
                .orElse(ClientOnboardingState.builder()
                        .clientId(clientId)
                        .createdAt(LocalDateTime.now())
                        .build());

        state.setUserId(userId);
        state.setRequiresSocial(requiresSocial);
        state.setRequiresCampaign(requiresCampaign);
        state.setCurrentStep(firstStep);
        state.setUpdatedAt(LocalDateTime.now());
        state.setCompletedAt(null);

        if (requiresSocial) {
            state.setGmbStatus(ConnectionStatus.PENDING);
            state.setMetaStatus(ConnectionStatus.PENDING);
        } else {
            state.setGmbStatus(null);
            state.setMetaStatus(null);
        }

        state.setCampaignStatus(requiresCampaign ? ConnectionStatus.PENDING : null);
        state.setLocationStatus(ConnectionStatus.PENDING);

        if (firstStep == OnboardingStep.COMPLETED) {
            state.setCompletedAt(LocalDateTime.now());
        }

        onboardingStateRepository.save(state);
        log.info("Onboarding initialized for clientId={}, firstStep={}", clientId, firstStep);
    }

    /**
     * Called by GMBOAuthService after a successful GMB OAuth flow.
     */
    @Transactional
    public void markGmbConnected(String clientId) {
        Optional<ClientOnboardingState> stateOpt = onboardingStateRepository.findByClientId(clientId);
        if (stateOpt.isEmpty()) {
            log.debug("No onboarding record for clientId={} — skipping GMB connected mark (legacy client)", clientId);
            return;
        }
        ClientOnboardingState state = stateOpt.get();
        state.setGmbStatus(ConnectionStatus.CONNECTED);
        state.setUpdatedAt(LocalDateTime.now());
        onboardingStateRepository.save(state);
        log.info("GMB marked CONNECTED for clientId={}", clientId);
        evaluateAndAdvance(clientId);
    }

    /**
     * Called by FacebookOAuthService after a successful Meta OAuth flow.
     */
    @Transactional
    public void markMetaConnected(String clientId) {
        Optional<ClientOnboardingState> stateOpt = onboardingStateRepository.findByClientId(clientId);
        if (stateOpt.isEmpty()) {
            log.debug("No onboarding record for clientId={} — skipping Meta connected mark (legacy client)", clientId);
            return;
        }
        ClientOnboardingState state = stateOpt.get();
        state.setMetaStatus(ConnectionStatus.CONNECTED);
        state.setUpdatedAt(LocalDateTime.now());
        onboardingStateRepository.save(state);
        log.info("Meta marked CONNECTED for clientId={}", clientId);
        evaluateAndAdvance(clientId);
    }

    /**
     * Called when the OAuth callback fails (GMB or Meta).
     * Marks the platform as FAILED — the user must retry or skip.
     * Does NOT advance the step.
     */
    @Transactional
    public void markPlatformFailed(String clientId, String platform) {
        Optional<ClientOnboardingState> stateOpt = onboardingStateRepository.findByClientId(clientId);
        if (stateOpt.isEmpty()) return;

        ClientOnboardingState state = stateOpt.get();
        if ("GMB".equalsIgnoreCase(platform)) {
            state.setGmbStatus(ConnectionStatus.FAILED);
        } else if ("META".equalsIgnoreCase(platform)) {
            state.setMetaStatus(ConnectionStatus.FAILED);
        }
        state.setUpdatedAt(LocalDateTime.now());
        onboardingStateRepository.save(state);
        log.info("Platform {} marked FAILED for clientId={}", platform, clientId);
    }

    /**
     * Called when the user explicitly chooses to skip a social platform for now.
     * Business rule: at least ONE platform must be CONNECTED — both cannot be skipped.
     */
    @Transactional
    public void skipPlatformConnection(String clientId, String platform) {
        ClientOnboardingState state = onboardingStateRepository
                .findByClientId(clientId)
                .orElseThrow(() -> new InvalidRequestException(
                        "No onboarding record found for clientId: " + clientId));

        if (!state.isRequiresSocial()) {
            throw new InvalidRequestException("Social account connection is not required for this plan");
        }

        // Guard: both platforms cannot be skipped — at least one must be CONNECTED
        if ("GMB".equalsIgnoreCase(platform)) {
            if (state.getMetaStatus() == ConnectionStatus.SKIPPED) {
                throw new InvalidRequestException(
                        "Cannot skip GMB — Meta is already skipped. At least one social account (GMB or Meta) must be connected.");
            }
            state.setGmbStatus(ConnectionStatus.SKIPPED);
        } else if ("META".equalsIgnoreCase(platform)) {
            if (state.getGmbStatus() == ConnectionStatus.SKIPPED) {
                throw new InvalidRequestException(
                        "Cannot skip Meta — GMB is already skipped. At least one social account (GMB or Meta) must be connected.");
            }
            state.setMetaStatus(ConnectionStatus.SKIPPED);
        } else {
            throw new InvalidRequestException("Unknown platform: " + platform + ". Valid values: GMB, META");
        }

        state.setUpdatedAt(LocalDateTime.now());
        onboardingStateRepository.save(state);
        log.info("Platform {} marked SKIPPED for clientId={}", platform, clientId);

        evaluateAndAdvance(clientId);
    }

    /**
     * Called when the user completes (or skips) the campaign advertiser setup.
     */
    @Transactional
    public void completeCampaignSetup(String clientId) {
        Optional<ClientOnboardingState> stateOpt = onboardingStateRepository.findByClientId(clientId);
        if (stateOpt.isEmpty()) {
            log.debug("No onboarding record for clientId={} — skipping campaign setup completion", clientId);
            return;
        }
        ClientOnboardingState state = stateOpt.get();
        state.setCampaignStatus(ConnectionStatus.CONNECTED);
        state.setUpdatedAt(LocalDateTime.now());
        onboardingStateRepository.save(state);
        log.info("Campaign setup completed for clientId={}", clientId);

        evaluateAndAdvance(clientId);
    }

    /**
     * Called by GMBLocationService after GMB locations have been inserted into the DB.
     */
    @Transactional
    public void markGmbLocationSelected(String clientId) {
        Optional<ClientOnboardingState> stateOpt = onboardingStateRepository.findByClientId(clientId);
        if (stateOpt.isEmpty()) {
            log.debug("No onboarding record for clientId={} — skipping location selected mark (legacy client)", clientId);
            return;
        }
        ClientOnboardingState state = stateOpt.get();
        state.setLocationStatus(ConnectionStatus.CONNECTED);
        state.setUpdatedAt(LocalDateTime.now());
        onboardingStateRepository.save(state);
        log.info("GMB location selection marked CONNECTED for clientId={}", clientId);
    }

    /**
     * Checks if all required connections for the current step are in a terminal
     * state (CONNECTED or SKIPPED) and advances the step accordingly.
     * Enforces the rule: at least one social platform must be CONNECTED.
     */
    @Transactional
    public void evaluateAndAdvance(String clientId) {
        Optional<ClientOnboardingState> stateOpt = onboardingStateRepository.findByClientId(clientId);
        if (stateOpt.isEmpty()) return;

        ClientOnboardingState state = stateOpt.get();

        if (state.getCurrentStep() == OnboardingStep.SOCIAL_ACCOUNT_SETUP) {
            boolean gmbHandled  = isTerminal(state.getGmbStatus());
            boolean metaHandled = isTerminal(state.getMetaStatus());
            boolean atLeastOneConnected =
                    state.getGmbStatus()  == ConnectionStatus.CONNECTED ||
                    state.getMetaStatus() == ConnectionStatus.CONNECTED;

            if (gmbHandled && metaHandled && atLeastOneConnected) {
                if (state.isRequiresCampaign()) {
                    state.setCurrentStep(OnboardingStep.CAMPAIGN_SETUP);
                    log.info("Onboarding advanced to CAMPAIGN_SETUP for clientId={}", clientId);
                } else {
                    state.setCurrentStep(OnboardingStep.COMPLETED);
                    state.setCompletedAt(LocalDateTime.now());
                    log.info("Onboarding COMPLETED for clientId={}", clientId);
                }
                state.setUpdatedAt(LocalDateTime.now());
                onboardingStateRepository.save(state);
            }

        } else if (state.getCurrentStep() == OnboardingStep.CAMPAIGN_SETUP) {
            if (isTerminal(state.getCampaignStatus())) {
                state.setCurrentStep(OnboardingStep.COMPLETED);
                state.setCompletedAt(LocalDateTime.now());
                state.setUpdatedAt(LocalDateTime.now());
                onboardingStateRepository.save(state);
                log.info("Onboarding COMPLETED (campaign step done) for clientId={}", clientId);
            }
        }
    }

    /**
     * Returns the current onboarding status for the given client.
     */
    public OnboardingStatusResponse getStatus(String clientId) {
        return onboardingStateRepository.findByClientId(clientId)
                .map(state -> OnboardingStatusResponse.builder()
                        .clientId(state.getClientId())
                        .currentStep(state.getCurrentStep().name())
                        .requiresSocial(state.isRequiresSocial())
                        .requiresCampaign(state.isRequiresCampaign())
                        .gmbStatus(state.getGmbStatus() != null ? state.getGmbStatus().name() : null)
                        .metaStatus(state.getMetaStatus() != null ? state.getMetaStatus().name() : null)
                        .locationStatus(state.getLocationStatus() != null ? state.getLocationStatus().name() : null)
                        .campaignStatus(state.getCampaignStatus() != null ? state.getCampaignStatus().name() : null)
                        .updatedAt(state.getUpdatedAt())
                        .completedAt(state.getCompletedAt())
                        .build())
                .orElse(null);
    }

    /**
     * Returns the name of the current onboarding step, or null if no record exists
     * (e.g., legacy clients who signed up before this feature was added).
     */
    public String getCurrentStepName(String clientId) {
        return onboardingStateRepository.findByClientId(clientId)
                .map(state -> state.getCurrentStep().name())
                .orElse(null);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    /** A status is terminal when the user has acted on it (connected or skipped). */
    private boolean isTerminal(ConnectionStatus status) {
        return status == ConnectionStatus.CONNECTED || status == ConnectionStatus.SKIPPED;
    }
}
