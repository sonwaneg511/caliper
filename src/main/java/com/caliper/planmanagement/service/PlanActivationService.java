package com.caliper.planmanagement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.onboarding.service.OnboardingService;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.entity.PlanDurationType;
import com.caliper.planmanagement.entity.PlanHistory;
import com.caliper.planmanagement.entity.ServicePlanMapping;
import com.caliper.planmanagement.entity.payment.CaliperPayment;
import com.caliper.planmanagement.entity.payment.RazorpayOrder;
import com.caliper.planmanagement.repository.PlanHistoryRepository;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.repository.ServicePlanMappingRepository;
import com.caliper.planmanagement.repository.payment.CaliperPaymentRepository;
import com.caliper.planmanagement.repository.payment.RazorpayOrderRepository;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;

@Service
public class PlanActivationService {

    private static final Logger log = LoggerFactory.getLogger(PlanActivationService.class);

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanHistoryRepository planHistoryRepository;

    @Autowired
    private ServicePlanMappingRepository servicePlanMappingRepository;

    @Autowired
    private CaliperPaymentRepository caliperPaymentRepository;

    @Autowired
    private RazorpayOrderRepository razorpayOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleClientMappingRepository userRoleClientMappingRepository;

    @Autowired
    private OnboardingService onboardingService;

    @Transactional
    public Plan activatePlan(RazorpayOrder order, String paymentId, String signature) {
        log.info("Activating plan for clientId={}, razorpayOrderId={}", order.getClientId(), order.getRazorpayOrderId());

        Date now = new Date();
        Date endDate = calculateEndDate(order.getDurationType());

        String planName = switch (PlanDurationType.valueOf(order.getDurationType())) {
            case MONTHLY -> "Monthly Plan";
            case HALF_YEARLY -> "Half-Yearly Plan";
            case ANNUAL -> "Annual Plan";
        };

        Plan plan = new Plan();
        plan.setClientId(order.getClientId());
        plan.setStartDate(now);
        plan.setEndDate(endDate);
        plan.setLocationCount(order.getLocationCount());
        plan.setStatus(Plan.STATUS_ACTIVE);
        plan.setCreatedBy(order.getUserId());
        plan.setCreatedAt(now);
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setDurationType(PlanDurationType.valueOf(order.getDurationType()));
        plan.setRazorpayOrderId(order.getRazorpayOrderId());
        plan.setPlanName(planName);

        Plan savedPlan = planRepository.save(plan);
        log.info("Plan created with id={}", savedPlan.getId());

        planHistoryRepository.save(new PlanHistory(
                0L,
                savedPlan.getId(),
                savedPlan.getClientId(),
                savedPlan.getStartDate(),
                savedPlan.getEndDate(),
                savedPlan.getLocationCount(),
                savedPlan.getStatus(),
                savedPlan.getCreatedBy(),
                now,
                null,
                savedPlan.getDurationType() != null ? savedPlan.getDurationType().name() : null,
                savedPlan.getPlanName(),
                order.getAmountPaise() / 100.0
        ));

        // Fix: the Razorpay payment flow was missing SUPER_ADMIN role mapping creation
        // (the old /plan/createplan endpoint did this; the new flow did not)
        UserRoleClientMapping roleMapping = new UserRoleClientMapping();
        roleMapping.setUserId(order.getUserId());
        roleMapping.setClientId(order.getClientId());
        roleMapping.setRole(UserRoleClientMapping.ROLE_SUPER_ADMIN);
        userRoleClientMappingRepository.save(roleMapping);
        log.info("SUPER_ADMIN role mapping created for userId={}, clientId={}", order.getUserId(), order.getClientId());

        List<String> serviceKeyList = Arrays.asList(order.getServiceKeys().split(","));
        for (String serviceKey : serviceKeyList) {
            String trimmed = serviceKey.trim();
            if (!trimmed.isEmpty()) {
                ServicePlanMapping mapping = new ServicePlanMapping();
                mapping.setPlanId(savedPlan.getId());
                mapping.setServiceKey(trimmed);
                servicePlanMappingRepository.save(mapping);
            }
        }

        BigDecimal cgstAmount = order.getCgstPaise() != null
                ? BigDecimal.valueOf(order.getCgstPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;
        BigDecimal sgstAmount = order.getSgstPaise() != null
                ? BigDecimal.valueOf(order.getSgstPaise()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;

        CaliperPayment payment = CaliperPayment.builder()
                .clientId(order.getClientId())
                .userId(order.getUserId())
                .paymentId(paymentId)
                .orderId(order.getRazorpayOrderId())
                .amount(order.getAmountPaise() / 100.0)
                .status(CaliperPayment.PAYMENT_SUCCESS)
                .planId(savedPlan.getId())
                .transactionDatetime(now)
                .razorpaySignature(signature)
                .durationType(order.getDurationType())
                .cgstAmount(cgstAmount)
                .sgstAmount(sgstAmount)
                .build();

        caliperPaymentRepository.save(payment);

        order.setStatus(RazorpayOrder.STATUS_PAID);
        order.setPlanId(savedPlan.getId());
        razorpayOrderRepository.save(order);

        Optional<User> userOpt = userRepository.findByUserId(order.getUserId());
        userOpt.ifPresent(user -> {
            user.setActive(Plan.STATUS_ACTIVE);
            userRepository.save(user);
        });

        // Trigger onboarding state machine — determines the first onboarding step
        // based on which services were purchased (POST/REVIEW → social account setup, etc.)
        onboardingService.initializeFromPlan(order.getClientId(), order.getUserId(), serviceKeyList);

        log.info("Plan activation complete for planId={}", savedPlan.getId());
        return savedPlan;
    }

    private Date calculateEndDate(String durationType) {
        long millis = System.currentTimeMillis();
        return switch (PlanDurationType.valueOf(durationType)) {
            case MONTHLY -> new Date(millis + 30L * 24 * 60 * 60 * 1000);
            case HALF_YEARLY -> new Date(millis + 180L * 24 * 60 * 60 * 1000);
            case ANNUAL -> new Date(millis + 365L * 24 * 60 * 60 * 1000);
        };
    }
}
