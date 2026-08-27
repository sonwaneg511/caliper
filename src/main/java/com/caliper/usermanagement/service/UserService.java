package com.caliper.usermanagement.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.gmb.entity.GMBLocation;
import com.caliper.location.gmb.service.GBPCompletenessScoreCalculator;
import com.caliper.location.gmb.service.GMBLocationService;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.onboarding.entity.OnboardingStep;
import com.caliper.onboarding.service.OnboardingService;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.service.PlanService;
import com.caliper.security.jwt.AuthTokenFilter;
import com.caliper.usermanagement.dto.CreateUserRequestDto;
import com.caliper.usermanagement.dto.CreateUserResponseDto;
import com.caliper.usermanagement.dto.EditUserRequestDto;
import com.caliper.usermanagement.dto.ForgotPasswordRequest;
import com.caliper.usermanagement.dto.ForgotPasswordResponse;
import com.caliper.usermanagement.dto.GenericMessageResponse;
import com.caliper.usermanagement.dto.LocationDetails;
import com.caliper.usermanagement.dto.NewUserResponseDto;
import com.caliper.usermanagement.dto.SignUpRequest;
import com.caliper.usermanagement.dto.SignUpResponse;
import com.caliper.usermanagement.dto.UserResponseDto;
import com.caliper.usermanagement.dto.ViewDealerDetailsResponse;
import com.caliper.usermanagement.dto.ViewExistingUserResponse;
import com.caliper.usermanagement.dto.ViewNewUserResponseDto;
import com.caliper.usermanagement.dto.request.PasswordResetRequest;
import com.caliper.usermanagement.dto.request.VerifyEmailRequest;
import com.caliper.usermanagement.entity.EmailVerificationToken;
import com.caliper.usermanagement.entity.ForgotPassword;
import com.caliper.usermanagement.entity.RoleMaster;
import com.caliper.usermanagement.entity.SecureHashing;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.EmailVerificationTokenRepository;
import com.caliper.usermanagement.repository.ForgotPasswordRepository;
import com.caliper.usermanagement.repository.RoleMasterRepository;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;
import com.caliper.usermanagement.rule.UserActionStrategyFactory;
import com.caliper.utils.email.EmailService;
import com.caliper.utils.email.EmailUtility;
import com.caliper.utils.exception.customException.ResourceNotFoundException;
import com.caliper.utils.security.CookieUtils;
import com.caliper.utils.security.PasswordValidator;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserService {

	@Autowired
	public UserRoleClientMappingRepository userRoleClientMappingRepository;

	@Autowired
	public UserRepository userRepository;
	
	@Autowired
	public ClientRepository clientRepository;

	@Autowired
	public RoleMasterRepository roleMasterRepository;

	@Autowired
	public UserClientLocMappingRepository userClientLocMappingRepository;

	@Autowired
	public ForgotPasswordRepository forgotPasswordRepository;

	@Autowired
	public EmailVerificationTokenRepository emailVerificationTokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private DealerLocationRepository dealerLocationRepository;
	
	@Autowired
	private PlanRepository planRepository;
	
	@Autowired
	private PlanService planService;

	@Autowired
	private OnboardingService onboardingService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private GMBLocationService gmbLocationService;

	@Autowired
	private GBPCompletenessScoreCalculator gbpCompletenessScoreCalculator;

	@Value("${app.password-reset.url}")
	private String passwordResetBaseUrl;

	@Value("${app.email-verification.url}")
	private String emailVerificationBaseUrl;

	
	public UserRoleClientMapping insertUserClientMapping(UserRoleClientMapping userClientMapping) {
		return userRoleClientMappingRepository.save(userClientMapping);
	}

	public Optional<UserRoleClientMapping> getUserClientMappingById(long id){
		return userRoleClientMappingRepository.findById(id);
	}

    public List<UserRoleClientMapping> getUserRoleClientMappingByUserId(String userId){
        return userRoleClientMappingRepository.getUserRoleClientMappingByUserId(userId);
    }

    public List<UserRoleClientMapping> getUserRoleClientMappingByUserIdAndClientId(String userId, String cleintId){
        return userRoleClientMappingRepository.getUserRoleClientMappingByUserIdAndClientId(userId, cleintId);
    }
    
	public List<UserRoleClientMapping> getAllUserClientMapping(){
		return userRoleClientMappingRepository.findAll();
	}

	public User insertUser(User user) {
		return userRepository.save(user);
	}

	public Optional<User> getUserById(long id){
		return userRepository.findById(id);
	}

	public Optional<User> getUserByUserId(String userId){
		return userRepository.findByUserId(userId);
	}

	public List<User> getAllUsers(){
		return userRepository.findAll();
	}

	public RoleMaster insertRoleMaster(RoleMaster roleMaster) {
		return roleMasterRepository.save(roleMaster);
	}

	public Optional<RoleMaster> getRoleMasterById(long id){
		return roleMasterRepository.findById(id);
	}

	public List<RoleMaster> getAllRoleMaster(){
		return roleMasterRepository.findAll();
	}

	public List<RoleMaster> getAllRoleMasterById(long id){
		return roleMasterRepository.findAllRoleMasterById(id);
	}

	public UserClientLocMapping insertUserRoleLocMapping(UserClientLocMapping userRoleLocMapping) {
		return userClientLocMappingRepository.save(userRoleLocMapping);
	}

	public List<UserClientLocMapping> getAllUserRoleLocMapping() {
		return userClientLocMappingRepository.findAll();
	}
	
	public List<UserClientLocMapping> findByUserIdAndclientId(String userId, String clientId){
		return userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);
	}

	public ForgotPassword insertForgotPassword(ForgotPassword forgotPassword) {
		return forgotPasswordRepository.save(forgotPassword);
	}

	public List<ForgotPassword> getAllForgotPassword(){
		return forgotPasswordRepository.findAll();
	}

	public Optional<ForgotPassword> getForgotPasswordByUserId(String userId){
		return forgotPasswordRepository.getForgotPasswordByUserId(userId);
	}

	//----------------------API CALLS---------------------------------------------------	

	// 1---------------------------LOGIN CHECK---------------------------
//	public UserResponseDto loginCheck(UserRequestDto userRequestDto) {
//
//	    UserResponseDto resp = new UserResponseDto();
//	    List<String> dealerIds = new ArrayList<>();
//	    List<String> roles = new ArrayList<>();
//
////	    if (userRequestDto == null) {
////	        resp.setStatus(HttpStatus.BAD_REQUEST.value());
////	        resp.setMessage("Invalid login request");
////	        resp.setError("Request body cannot be null");
////	        resp.setSuccess(false);
////	        return ResponseEntity.badRequest().body(resp);
////	    }
//
//	    String userId = userRequestDto.getUserId();
//	    //String passwordMatch = userRequestDto.getPassword();
//
//	    Optional<User> userOpt = userRepository.findByUserId(userId);
//
////	    if (userOpt.isEmpty()) {
////	        resp.setStatus(HttpStatus.UNAUTHORIZED.value());
////	        resp.setMessage("Login failed");
////	        resp.setError("Invalid credentials");
////	        resp.setSuccess(false);
////	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
////	    }
//
//	    User existingUser = userOpt.get();
////
////	    if (!passwordEncoder.matches(userRequestDto.getPassword(), existingUser.getPassword())) {
////	        resp.setStatus(HttpStatus.UNAUTHORIZED.value());
////	        resp.setMessage("Login failed");
////	        resp.setError("Invalid credentials");
////	        resp.setSuccess(false);
////	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
////	    }
//
//	    String clientId = existingUser.getClientId();
//	    List<UserRoleClientMapping> listRoles =
//	            userRoleClientMappingRepository.getUserRoleClientMappingByUserId(userId);
//	    List<UserClientLocMapping> listDealerIds =
//	            userClientLocMappingRepository.findByUserId(userId);
//
//	    listRoles.forEach(role -> roles.add(role.getRole()));
//	    listDealerIds.forEach(loc -> dealerIds.add(loc.getDealerId()));
//	    List<SimpleGrantedAuthority> rolesList = listRoles.stream().map(a->new SimpleGrantedAuthority("ROLE_" +a.getRole())).toList();
//	    
////	    List<String> roles = userDetails.getAuthorities().stream()
////                .map(GrantedAuthority::getAuthority)
////                .collect(Collectors.toList());
//
//	    resp.setClientId(clientId);
//	    resp.setDealerIds(dealerIds);
//	    resp.setRoles(rolesList);
//	    resp.setStatus(HttpStatus.OK.value());
//	    resp.setMessage("Login successful");
//	    resp.setSuccess(true);
//
//	    return resp;
//	}
	public UserResponseDto loginCheck(String userId) {

		UserResponseDto resp = new UserResponseDto();
		List<String> dealerIds = new ArrayList<>();
		List<String> rolesList = new ArrayList<>();

		User existingUser = userRepository.findByUserId(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		boolean emailVerified = !User.STATUS_PENDING.equals(existingUser.getActive());

		// Fetch the Client record via email (the stable link between User and Client tables).
		// Client.clientId is only set after PlanService.createPlan(), so fall back to
		// User.clientId for accounts that completed plan creation before this fix.
		Client client = clientRepository.findByEmail(userId);
		String clientId = (client != null && client.getClientId() != null)
				? client.getClientId()
				: existingUser.getClientId();
		String clientName = (client != null) ? client.getClientName() : null;

		// Map roles from DB to authorities
		List<UserRoleClientMapping> listRoles =
				userRoleClientMappingRepository.getUserRoleClientMappingByUserId(userId);
		List<UserClientLocMapping> listDealerIds =
				userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);

		listRoles.forEach(role -> rolesList.add(role.getRole()));
		listDealerIds.forEach(loc -> dealerIds.add(loc.getDealerId()));

		Plan plan = planRepository.findByClientId(existingUser.getClientId());

		List<String> allowedroles = new ArrayList<>();
		if(!rolesList.isEmpty()) {
			if(rolesList.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN) || rolesList.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
				allowedroles = planService.getAllowedRoles(clientId);
			}
		}

		if(rolesList.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
			resp.setRole(UserRoleClientMapping.ROLE_SUPER_ADMIN);
			resp.setTotalLocations(plan != null ? plan.getLocationCount() : 0);
		} else if(rolesList.contains(UserRoleClientMapping.ROLE_ADMIN)) {
			resp.setRole(UserRoleClientMapping.ROLE_ADMIN);
			resp.setTotalLocations(plan != null ? plan.getLocationCount() : 0);
		} else {
			resp.setRole(UserRoleClientMapping.ROLE_USER);
			resp.setTotalLocations(listDealerIds != null ? listDealerIds.size() : 0);
		}

		resp.setUserId(userId);
		resp.setClientId(clientId);
		resp.setClientName(clientName);
		resp.setDealerIds(dealerIds);
		resp.setProfileCompletenessScore(calculateProfileCompletenessScore(clientId, dealerIds));
		if(!rolesList.isEmpty()) {
			if(rolesList.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN) || rolesList.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
				resp.setModules(allowedroles);
			} else {
				resp.setModules(rolesList);
			}
		}else {
			resp.setModules(new ArrayList<>());
		}

		resp.setStatus(HttpStatus.OK.value());
		resp.setSuccess(true);
		resp.setAccountStatus(existingUser.getActive());
		resp.setEmailVerified(emailVerified);

		// Only meaningful when emailVerified is false: tells the frontend whether the
		// link already sitting in the user's inbox is still usable, or whether it must
		// prompt a resend. No token on file counts as expired — there's nothing to click.
		boolean verificationLinkExpired = !emailVerified && emailVerificationTokenRepository.findByUserId(userId)
				.map(t -> t.getExpiresAt().isBefore(java.time.LocalDateTime.now()))
				.orElse(true);
		resp.setVerificationLinkExpired(verificationLinkExpired);

		resp.setMessage(emailVerified ? "Login successful" : "Please verify your email to complete login.");
		if(plan != null) {
			resp.setPlanStatus(plan.getStatus());
		} else {
			resp.setPlanStatus(Plan.STATUS_INACTIVE);
		}

		// Include onboarding step and social connection statuses for frontend routing.
		// Returns null for legacy users: active plan but no onboarding record → route to dashboard.
		// Returns PLAN_PENDING when no plan exists or plan is not ACTIVE and no onboarding record.
		com.caliper.onboarding.dto.OnboardingStatusResponse onboardingStatus =
				(clientId != null) ? onboardingService.getStatus(clientId) : null;

		String onboardingStep = (onboardingStatus != null) ? onboardingStatus.getCurrentStep() : null;

		if (onboardingStep == null && (plan == null || !Plan.STATUS_ACTIVE.equals(plan.getStatus()))) {
			onboardingStep = OnboardingStep.PLAN_PENDING.name();
		}

		resp.setOnboardingStep(onboardingStep);
		if (onboardingStatus != null) {
			resp.setGmbStatus(onboardingStatus.getGmbStatus());
			resp.setMetaStatus(onboardingStatus.getMetaStatus());
			resp.setLocationStatus(onboardingStatus.getLocationStatus());
		}

		return resp;
	}

	// GBP profile-completeness score (0-100), averaged across every GMB location mapped
	// to this user for this client.
	private int calculateProfileCompletenessScore(String clientId, List<String> dealerIds) {

		if (clientId == null || dealerIds == null || dealerIds.isEmpty()) {
			return 0;
		}

		Set<String> dealerIdSet = dealerIds.stream()
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toSet());

		if (dealerIdSet.isEmpty()) {
			return 0;
		}

		List<GMBLocation> gmbLocations = gmbLocationService.fetchLocationsByClientAndDealerIds(clientId, dealerIdSet);

		if (gmbLocations.isEmpty()) {
			return 0;
		}

		double averageScore = gmbLocations.stream()
				.mapToInt(gbpCompletenessScoreCalculator::calculateScore)
				.average()
				.orElse(0.0);

		return (int) Math.round(averageScore);
	}


	// 2---------------------------CREATE ADMIN USER---------------------------
	public ResponseEntity<SignUpResponse> createAdminUser(SignUpRequest signUpRequest) {
		SignUpResponse resp = new SignUpResponse();
		//NULL REQUEST CHECK
		if(signUpRequest == null) {
			resp.setStatus(HttpStatus.BAD_REQUEST.value());       // 400
		    resp.setMessage("Invalid signup request");
		    resp.setError("Request body cannot be null");
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
		}
		
		//EXISTING USER CHECK
		Optional<User> existingUser = userRepository.findByUserId(signUpRequest.getUserId().toLowerCase());
		boolean userNameProvided = signUpRequest.getUserName() != null && !signUpRequest.getUserName().isBlank();
		Optional<User> existingUserName = userNameProvided
				? userRepository.findByUserName(signUpRequest.getUserName().toLowerCase())
				: Optional.empty();
		boolean exists = clientRepository.findAll().stream().anyMatch(client -> client.getClientName() .equalsIgnoreCase(signUpRequest.getClientName()));

		if (existingUser.isPresent()) {
		        resp.setStatus(HttpStatus.CONFLICT.value()); // Conflict
		        resp.setMessage("Signup failed");
		        resp.setError("User ID already exists");
		        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
		    }

		 if (existingUserName.isPresent()) {
		        resp.setStatus(HttpStatus.CONFLICT.value()); // Conflict
		        resp.setMessage("Signup failed");
		        resp.setError("User Name already exists");
		        return ResponseEntity.status(HttpStatus.CONFLICT).body(resp);
		    }
		
		//CHECK FOR DUPLICATE CLIENT NAME
		if(exists) { 
		resp.setStatus(HttpStatus.BAD_REQUEST.value());
		resp.setStatus(HttpStatus.CONFLICT.value());
		resp.setMessage("Signup Failed"); 
		resp.setError("Client Name already exists");
		return ResponseEntity.badRequest().body(resp); }
				 
		 // Generate clientId at signup — same format used elsewhere: {ClientName}_{6-digit-random}
		int randomNum = (int) (Math.random() * 900000) + 100000;
		String generatedClientId = signUpRequest.getClientName().trim().replace(" ", "_") + "_" + randomNum;

		 //NEW USER CREATION- UPPERCASE IS ALLOWED
		 User newUser = new User();
		    newUser.setUserId(signUpRequest.getUserId().toLowerCase());
		    newUser.setUserName(userNameProvided ? signUpRequest.getUserName().toLowerCase() : null);
		    newUser.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
		    newUser.setClientId(generatedClientId);
		    newUser.setCreatedDate(new Date());
		    newUser.setActive(User.STATUS_PENDING);
		    newUser.setCreatedBy(signUpRequest.getUserId().toLowerCase());

		    userRepository.save(newUser);

		 //NEW CLIENT CREATION
		   Client newClient = new Client();
		   newClient.setEmail(signUpRequest.getUserId().toLowerCase());
		   newClient.setClientAddedDate(new Date());
		   newClient.setClientName(signUpRequest.getClientName());
		   newClient.setClientId(generatedClientId);
		   newClient.setCountryCode(signUpRequest.getCountryCode());
		   newClient.setPhoneNumber(signUpRequest.getPhoneNumber());

		   clientRepository.save(newClient);

		// SUPER_ADMIN ROLE MAPPING — assign SUPER_ADMIN role to the new user at signup
		   UserRoleClientMapping roleMapping = UserRoleClientMapping.builder()
		           .userId(signUpRequest.getUserId().toLowerCase())
		           .clientId(generatedClientId)
		           .role(UserRoleClientMapping.ROLE_SUPER_ADMIN)
		           .build();
		   userRoleClientMappingRepository.save(roleMapping);

		   issueVerificationToken(signUpRequest.getUserId().toLowerCase());

		    resp.setStatus(HttpStatus.CREATED.value());
		    resp.setMessage("User created successfully");
		    resp.setError(null);
		    resp.setClientId(generatedClientId);

		    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
	}

	// Generates a fresh email-verification token for the user, saves it, and emails the link.
	private void issueVerificationToken(String userId) {
		String rawToken = SecureHashing.generateToken();
		String tokenHash = SecureHashing.encryptPassword(rawToken);

		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		Optional<EmailVerificationToken> existingOpt = emailVerificationTokenRepository.findByUserId(userId);

		EmailVerificationToken record = existingOpt.orElseGet(EmailVerificationToken::new);
		record.setUserId(userId);
		record.setTokenHash(tokenHash);
		record.setCreatedAt(now);
		record.setExpiresAt(now.plusMinutes(15));
		emailVerificationTokenRepository.save(record);

		String verificationLink = emailVerificationBaseUrl + "?token=" + rawToken;
		String subject = "Verify Your Email Address";
		String body = "<p>Hello,</p>"
				+ "<p>Thanks for signing up. Please verify your email address to activate your account.</p>"
				+ "<p>Click the link below to verify your email (valid for 15 minutes):</p>"
				+ "<p><a href=\"" + verificationLink + "\">Verify My Email</a></p>"
				+ "<p>If you did not create this account, please ignore this email.</p>"
				+ "<p>Regards,<br>Support Team</p>";

		try {
			emailService.sendHtmlEmail(userId, subject, body);
		} catch (Exception ex) {
			//log.error("Failed to send verification email to {}: {}", userId, ex.getMessage(), ex);
		}
	}

	// ---------------------------VERIFY EMAIL---------------------------
	@Transactional
	public GenericMessageResponse verifyEmail(VerifyEmailRequest request) {
		if (request == null || request.getToken() == null || request.getToken().isBlank()) {
			return new GenericMessageResponse(HttpStatus.BAD_REQUEST.value(), "Verification token is required.");
		}

		String tokenHash = SecureHashing.encryptPassword(request.getToken());
		Optional<EmailVerificationToken> recordOpt = emailVerificationTokenRepository.findByTokenHash(tokenHash);

		if (recordOpt.isEmpty()) {
			return new GenericMessageResponse(HttpStatus.BAD_REQUEST.value(), "Invalid or expired verification link.");
		}

		EmailVerificationToken record = recordOpt.get();

		if (record.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
			return new GenericMessageResponse(HttpStatus.BAD_REQUEST.value(),
					"Verification link has expired. Please request a new one.");
		}

		Optional<User> userOpt = userRepository.findByUserId(record.getUserId());
		if (userOpt.isEmpty()) {
			return new GenericMessageResponse(HttpStatus.BAD_REQUEST.value(), "Invalid or expired verification link.");
		}

		User user = userOpt.get();
		user.setActive(User.STATUS_ACTIVE);
		userRepository.save(user);

		emailVerificationTokenRepository.delete(record);

		return new GenericMessageResponse(HttpStatus.OK.value(), "Email verified successfully. You can now log in.");
	}

	// ---------------------------RESEND VERIFICATION EMAIL---------------------------
	/**
	 * Always returns silently — never reveals whether the email exists or is already verified.
	 */
	public void resendVerificationEmail(String email) {
		if (email == null || email.isBlank()) return;

		String normalizedEmail = email.toLowerCase().trim();
		Optional<User> userOpt = userRepository.findByUserId(normalizedEmail);
		if (userOpt.isEmpty() || !User.STATUS_PENDING.equals(userOpt.get().getActive())) {
			return; // Silently do nothing — caller returns the same generic response
		}

		issueVerificationToken(normalizedEmail);
	}

	
	// 2---------------------------CREATE USER---------------------------
	public ViewNewUserResponseDto sendUserDetails(String userId, String clientId) {

	    if (userId == null || clientId == null) {
	        throw new IllegalArgumentException("UserId or ClientId cannot be null");
	    }

	    ViewNewUserResponseDto response = new ViewNewUserResponseDto();

	    List<String> dealerIds = userClientLocMappingRepository
	            .findByUserIdAndclientId(userId, clientId)
	            .stream()
	            .map(UserClientLocMapping::getDealerId)
	            .toList();

	    List<String> roles = userRoleClientMappingRepository
	            .findByUserIdAndClientId(userId, clientId)
	            .stream()
	            .map(UserRoleClientMapping::getRole)
	            .toList();

	    List<LocationDetails> locationDetailsList = new ArrayList<>();

	    for (String dealerId : dealerIds) {

	        DealerLocation dealerLocation =
	                dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealerId, clientId);

	        if (dealerLocation == null) {
	            continue; // safely skip missing dealer
	        }

	        LocationDetails locationDetail = new LocationDetails();
	        locationDetail.setId(dealerLocation.getId());
	        locationDetail.setArea(dealerLocation.getArea());
	        locationDetail.setCity(dealerLocation.getCity());
	        locationDetail.setDealerId(dealerLocation.getDealerId());
	        locationDetail.setDealerName(dealerLocation.getDealerName());
	        locationDetail.setState(dealerLocation.getState());

	        locationDetailsList.add(locationDetail);
	    }

	    response.setUserId(userId);
	    response.setRoles(roles);
	    response.setLocationDetails(locationDetailsList);

	    return response;
	}

	
	public CreateUserResponseDto createUser(CreateUserRequestDto dto) {

	    CreateUserResponseDto response = new CreateUserResponseDto();

	    if (dto == null || dto.getNewUser().isEmpty()) {
	        response.setMessage("User id cannot be empty");
	        return response;
	    }
	    
	    String creationUser = dto.getUserId();
	    List<String> newUsersList = dto.getNewUser();
	    List<String> validUserList = new ArrayList<>();
	    List<String> existingUserList = new ArrayList<>();
	    
	    Set<String> loggedInUserDealerIds = userClientLocMappingRepository
	            .getUserClientLocMappingByUserId(creationUser)
	            .stream()
	            .map(UserClientLocMapping::getDealerId)
	            .collect(Collectors.toSet());
	    Set<String> viewerDealerIdSet = new HashSet<>(loggedInUserDealerIds);
	    
	    // Validate users
	    for (String userId : newUsersList) {
	        if (userRepository.findByUserId(userId).isPresent()) {
	            existingUserList.add(userId);
	        } else {
	            validUserList.add(userId);
	        }
	    }

	    // Create valid users
	    for (String userId : validUserList) {

	        // Dealer mappings
	        if(dto.getDealerIds() != null && !dto.getDealerIds().isEmpty()) {
	        	
	        	boolean isSubset = dto.getDealerIds().stream()
	                    .allMatch(viewerDealerIdSet::contains);
	        	
	        	if (!isSubset) {
	        		response.setInvalidUserIds(existingUserList);
	        	    response.setMessage("Location acces is denied");

	        	    return response;
	            }
	        	
	        	for (String dealerId : dto.getDealerIds()) {
	                userClientLocMappingRepository.save(
	                        UserClientLocMapping.builder()
	                                .userId(userId.toLowerCase())
	                                .dealerId(dealerId)
	                                .clientId(dto.getClientId())
	                                .build()
	                );
	            }
	        }

	        // Role mappings
	        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
	            for (String role : dto.getRoles()) {
	                userRoleClientMappingRepository.save(
	                        UserRoleClientMapping.builder()
	                                .userId(userId.toLowerCase())
	                                .clientId(dto.getClientId())
	                                .role(role)
	                                .build()
	                );
	            }
	        }
	        
	        String rawPassword = EmailUtility.generatePasswordFromEmail(userId.toLowerCase());

	        User user = User.builder()
	                .userId(userId.toLowerCase())
	                .password(passwordEncoder.encode(rawPassword))
	                .userName(userId.substring(0, userId.indexOf("@")))
	                .clientId(dto.getClientId())
	                .createdDate(new Date())
	                .createdBy(creationUser)
	                .active(User.STATUS_ACTIVE)
	                .build();

	        userRepository.save(user);

	        // Send email (acceptable for now)
	        sendEmailForPassword(userId.toLowerCase(), rawPassword, dto.getAction());
	    }

	    response.setInvalidUserIds(existingUserList);
	    response.setMessage("User creation completed");

	    return response;
	}
	
	// 3---------------------------MODIFY USER---------------------------
	public ViewExistingUserResponse getUserForModify(String userId, String clientId) {

	    if (userId == null || clientId == null) {
	        throw new IllegalArgumentException("UserId or ClientId cannot be null");
	    }

	    boolean userPresent = userRepository.existsByUserId(userId);
		if(!userPresent) {
			throw new ResourceNotFoundException("User not found");
		}
	    
	    ViewExistingUserResponse response = new ViewExistingUserResponse();

	    List<String> dealerIds = userClientLocMappingRepository
	            .findByUserIdAndclientId(userId, clientId)
	            .stream()
	            .map(UserClientLocMapping::getDealerId)
	            .toList();

	    List<String> roles = userRoleClientMappingRepository
	            .findByUserIdAndClientId(userId, clientId)
	            .stream()
	            .map(UserRoleClientMapping::getRole)
	            .toList();
	    
	    List<ViewDealerDetailsResponse> viewDealerDetailsResponseList = getDealerDetails(userId, clientId);
	    
	    List<String> allowedroles = new ArrayList<>();
	    if(!roles.isEmpty()) {
        if(roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN) || roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
        	allowedroles = planService.getAllowedRoles(clientId);
        }

	    response.setUserId(userId);
	    response.setDealerIds(dealerIds);
        if(roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN) || roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
        	 response.setModules(allowedroles);
        } else {
        	response.setModules(roles);
        }
	    response.setViewDealerDetailsResponseList(viewDealerDetailsResponseList);
	    } else {
	    	throw new ResourceNotFoundException("User Not Found");
	    }
	    if(roles.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	    	response.setRoles(UserRoleClientMapping.ROLE_SUPER_ADMIN);
        } else if(roles.contains(UserRoleClientMapping.ROLE_ADMIN)) {
        	response.setRoles(UserRoleClientMapping.ROLE_ADMIN);
        } else {
        	response.setRoles(UserRoleClientMapping.ROLE_USER);
        }
	    
	    return response;
	}

	public void modifyUser(EditUserRequestDto dto) {
		
		
		
		userClientLocMappingRepository.deleteByUserIdAndClientId(dto.getUserId(), dto.getClientId());
		userRoleClientMappingRepository.deleteByUserIdAndClientId(dto.getUserId(), dto.getClientId());
		
		// Save dealer mappings
		if (dto.getDealerIds() != null && !dto.getDealerIds().isEmpty()) {

	        List<UserClientLocMapping> dealerMappings =
	                dto.getDealerIds().stream()
	                        .map(dealerId -> UserClientLocMapping.builder()
	                                .userId(dto.getUserId())
	                                .clientId(dto.getClientId())
	                                .dealerId(dealerId)
	                                .build())
	                        .toList();

	        userClientLocMappingRepository.saveAll(dealerMappings);
	    }
	    
		// Save roles
		if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {

	        List<UserRoleClientMapping> roleMappings =
	                dto.getRoles().stream()
	                        .map(role -> UserRoleClientMapping.builder()
	                                .userId(dto.getUserId())
	                                .clientId(dto.getClientId())
	                                .role(role)
	                                .build())
	                        .toList();

	        userRoleClientMappingRepository.saveAll(roleMappings);
	    }
	}
	
	
	// 2---------------------------VIEW ALL USERS---------------------------
	public List<NewUserResponseDto> viewAllUsers(String userId, String clientId) {

		System.out.println(">>> START viewAllUsers");
	    System.out.println("Input userId: " + userId + ", clientId: " + clientId);	
	    List<NewUserResponseDto> newUserResponseDtos = new ArrayList<>();

	    // DealerIds of logged-in user
	    Set<String> viewerDealerIds = userClientLocMappingRepository
	            .getUserClientLocMappingByUserId(userId)
	            .stream()
	            .map(UserClientLocMapping::getDealerId)
	            .collect(Collectors.toSet());
	    Set<String> viewerSet = new HashSet<>(viewerDealerIds);

	    System.out.println("Viewer Dealer IDs: " + viewerSet);
	    
	    List<User> allUsers = userRepository.findAllByClientId(clientId);
	    System.out.println("Total users fetched: " + (allUsers != null ? allUsers.size() : "NULL"));
	    
	    
	    for (User user : allUsers) {
	    	
	    	System.out.println("\n--- Processing user: " + user.getUserId());

	        if (!User.STATUS_ACTIVE.equalsIgnoreCase(user.getActive())) {
	            continue;
	        }

	        // Dealers of the user being evaluated
	        List<String> userDealerIds = userClientLocMappingRepository
	                .getUserClientLocMappingByUserId(user.getUserId())
	                .stream()
	                .map(UserClientLocMapping::getDealerId)
	                .toList();
	        System.out.println("User Dealer IDs: " + userDealerIds);

	        //dealer overlap
	        boolean hasCommonDealer = userDealerIds.stream()
	                .allMatch(viewerSet::contains);

	        System.out.println("Has common dealer: " + hasCommonDealer);
	        
	        if (!hasCommonDealer) {
	            continue;
	        }

	        NewUserResponseDto dto = new NewUserResponseDto();

	        // Roles
	        List<String> roles = userRoleClientMappingRepository
	                .getUserRoleClientMappingByUserId(user.getUserId())
	                .stream()
	                .map(UserRoleClientMapping::getRole)
	                .toList();

	        System.out.println("Roles: " + roles);
	        
	        List<String> allowedroles = new ArrayList<>();
	        if(roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN) || roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	        	allowedroles = planService.getAllowedRoles(clientId);
	        	System.out.println("Allowed roles from planService: " + allowedroles);
	        }
	        // Decide primary role
			/*
			 * dto.setRole( roles.stream().anyMatch(r ->
			 * r.equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN)) ?
			 * UserRoleClientMapping.ROLE_ADMIN : UserRoleClientMapping.ROLE_USER );
			 */

	        if(roles.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	        	dto.setRole(UserRoleClientMapping.ROLE_SUPER_ADMIN);
	        } else if(roles.contains(UserRoleClientMapping.ROLE_ADMIN)) {
	        	dto.setRole(UserRoleClientMapping.ROLE_ADMIN);
	        } else {
	        	dto.setRole(UserRoleClientMapping.ROLE_USER);
	        }
	        
	        dto.setId(user.getId());
	        if(roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_ADMIN) || roles.get(0).equalsIgnoreCase(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	        	//List<String> allowedroles = planService.getAllowedRoles(clientId);
	        	dto.setModules(allowedroles);
	        } else {
	        	dto.setModules(roles);
	        }
	        dto.setStatus(user.getActive());
	        dto.setUserId(user.getUserId());
	        dto.setLocationCount(userDealerIds.size());
	        dto.setUserName(user.getUserName());
	        dto.setCreatedBy(user.getCreatedBy());
	        
	        if(!dto.getUserId().equalsIgnoreCase(userId) && !roles.contains(UserRoleClientMapping.ROLE_SUPER_ADMIN)) {
	        	newUserResponseDtos.add(dto);
	        	System.out.println("User added to response: " + dto.getUserId());
	        } else {
	        	System.out.println("User skipped at final condition: " + dto.getUserId());
	        }
	    }

	    System.out.println(">>> END viewAllUsers. Response size: " + newUserResponseDtos.size());
	    return newUserResponseDtos;
	}


	// 3---------------------------DELETE USER---------------------------
	public void deleteUser(String userId, String clientId) {
		Optional<User> userOpt = userRepository.findByUserIdAndClientId(userId, clientId);
		
		if(userOpt.isPresent()) {
			User existingUser = userOpt.get();
			existingUser.setActive(User.STATUS_INACTIVE);
			userRepository.save(existingUser);
		}
	}
	
	public String delete(String userId) {
		Optional<User> user = userRepository.findByUserId(userId);
		return "";
	}

	// 1---------------------------INITIATE PASSWORD RESET---------------------------
		/**
		 * Initiates a password reset. Always returns silently — never reveals whether
		 * the email exists (prevents email enumeration attacks).
		 */
		public void initiatePasswordReset(String email) {
		    if (email == null || email.isBlank()) return;

		    String normalizedEmail = email.toLowerCase().trim();
		    Optional<User> userOpt = userRepository.findByUserId(normalizedEmail);
		    if (userOpt.isEmpty()) {
		        return; // Silently do nothing — caller returns the same generic response
		    }

		    String rawToken = SecureHashing.generateToken();
		    String tokenHash = SecureHashing.encryptPassword(rawToken);

		    java.time.LocalDateTime now = java.time.LocalDateTime.now();
		    Optional<ForgotPassword> existingOpt = forgotPasswordRepository.getForgotPasswordByUserId(normalizedEmail);

		    ForgotPassword record = existingOpt.orElseGet(ForgotPassword::new);
		    record.setUserId(normalizedEmail);
		    record.setTokenHash(tokenHash);
		    record.setCreatedAt(now);
		    record.setExpiresAt(now.plusMinutes(15));
		    record.setUsed(false);
		    forgotPasswordRepository.save(record);

		    String resetLink = passwordResetBaseUrl + "?token=" + rawToken;
		    String subject = "Password Reset Request";
		    String body = "<p>Hello,</p>"
		            + "<p>We received a request to reset your password.</p>"
		            + "<p>Click the link below to reset your password (valid for 15 minutes):</p>"
		            + "<p><a href=\"" + resetLink + "\">Reset My Password</a></p>"
		            + "<p>If you did not request a password reset, please ignore this email. Your password will not change.</p>"
		            + "<p>Regards,<br>Support Team</p>";

		    try {
		        emailService.sendHtmlEmail(normalizedEmail, subject, body);
		    } catch (Exception ex) {
		        //log.error("Failed to send password reset email to {}: {}", normalizedEmail, ex.getMessage(), ex);
		        //throw new EmailDeliveryException("Unable to send email. Please try again later.", ex);
		    }
		}

		/**
		 * @deprecated Use initiatePasswordReset(String email) directly.
		 */
		@Deprecated
		public ForgotPasswordResponse checkForgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
		    ForgotPasswordResponse resp = new ForgotPasswordResponse();
		    initiatePasswordReset(forgotPasswordRequest.getUserId());
		    resp.setSuccess(true);
		    resp.setMassage("If your email is registered, you will receive a password reset link shortly.");
		    return resp;
		}




		// 2---------------------------RESET PASSWORD---------------------------
		/**
		 * Validates the reset token and sets the new password.
		 * Token is looked up by its SHA-256 hash — the raw token is never stored.
		 */
		@Transactional
		public ForgotPasswordResponse resetPassword(PasswordResetRequest request) {
		    ForgotPasswordResponse resp = new ForgotPasswordResponse();

		    // Validate passwords match first (before any DB call)
		    if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
		        resp.setSuccess(false);
		        resp.setMassage("New password and confirm password must match.");
		        return resp;
		    }

		    // Validate password strength
		    if (!PasswordValidator.isValid(request.getNewPassword())) {
		        resp.setSuccess(false);
		        resp.setMassage(PasswordValidator.getValidationMessage());
		        return resp;
		    }

		    // Hash the received token to look it up in DB
		    String tokenHash = SecureHashing.encryptPassword(request.getToken());
		    Optional<ForgotPassword> recordOpt = forgotPasswordRepository.findByTokenHash(tokenHash);

		    if (recordOpt.isEmpty()) {
		        resp.setSuccess(false);
		        resp.setMassage("Invalid or expired reset link.");
		        return resp;
		    }

		    ForgotPassword record = recordOpt.get();

		    if (record.isUsed()) {
		        resp.setSuccess(false);
		        resp.setMassage("This reset link has already been used.");
		        return resp;
		    }

		    if (record.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
		        resp.setSuccess(false);
		        resp.setMassage("Reset link has expired. Please request a new one.");
		        return resp;
		    }

		    // Load the user
		    Optional<User> userOpt = userRepository.findByUserId(record.getUserId());
		    if (userOpt.isEmpty()) {
		        resp.setSuccess(false);
		        resp.setMassage("Invalid or expired reset link.");
		        return resp;
		    }

		    User user = userOpt.get();

		    // Prevent reuse of the current password
		    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
		        resp.setSuccess(false);
		        resp.setMassage("New password cannot be the same as your current password.");
		        return resp;
		    }

		    // Update password and invalidate existing JWT sessions
		    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		    user.setPasswordChangedAt(java.time.LocalDateTime.now());
		    userRepository.save(user);

		    // Mark token as used (prevent replay)
		    record.setUsed(true);
		    forgotPasswordRepository.save(record);

		    // Send confirmation email (fire-and-forget)
		    try {
		        String body = "<p>Hello,</p><p>Your password has been successfully reset.</p>"
		                + "<p>If you did not perform this action, please contact support immediately.</p>"
		                + "<p>Regards,<br>Support Team</p>";
		        emailService.sendHtmlEmail(user.getUserId(), "Password Reset Successful", body);
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }

		    resp.setSuccess(true);
		    resp.setMassage("Password has been reset successfully. Please login with your new password.");
		    return resp;
		}

		/**
		 * @deprecated Use resetPassword(PasswordResetRequest) directly.
		 */
		@Deprecated
		public ForgotPasswordResponse changeForgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
		    PasswordResetRequest req = new PasswordResetRequest(
		            forgotPasswordRequest.getPasswordToken(),
		            forgotPasswordRequest.getNewPassword(),
		            forgotPasswordRequest.getConfirmPassword()
		    );
		    return resetPassword(req);
		}

		/**
		 * Sends a password reset email to the user and updates the password in the database.
		 *
		 * @param userId       User ID (email) for which password reset is requested
		 * @param newPassword  Newly generated password to be sent via email
		 * @return error message if any issue occurs, otherwise null
		 */
		public String sendEmailForPassword(String userId, String newPassword, String action) {

		    // Variable to hold error message (null means success)
		    String error = null;

		    // Fetch user details using userId
		    Optional<User> userOpt = userRepository.findByUserId(userId);

		    // If user does not exist, return error message
		    if (userOpt.isEmpty()) {
		        return "User Id does not exist";
		    }

		    // Retrieve User entity
		    User user = userOpt.get();

		    // User email ID (assuming userId is the email)
		    String userMailId = user.getUserId();

		    // Email subject
		    String subject = "Reset Your Password";
		    
		    if(action.equalsIgnoreCase(User.USER_PASSWORD)) {
		    	subject = "Your New Password";
		    }

		    // Email body content (HTML format)
		    String mailBody = "Dear, " + userId
		            + "Your New Password is <b> " + newPassword + "</b> <br><br>"
		            + "Regards,<br>"
		            + "Support Team (Interactive Avenues)";

		    try {
		        emailService.sendHtmlEmail(userMailId, subject, mailBody);
		    } catch (Exception ex) {
		        ex.printStackTrace();
		        error = "Failed to send mail, Please try again";
		    }

		    return error;
		}

	
	public List<ViewDealerDetailsResponse> getDealerDetails(String userId, String clientId){
		
		List<ViewDealerDetailsResponse> viewDealerDetailsResponses = new ArrayList<>();

	    List<UserClientLocMapping> clientLocMappingList = userClientLocMappingRepository.findByUserIdAndclientId(userId, clientId);

	    if (clientLocMappingList == null || clientLocMappingList.isEmpty()) {
	        return viewDealerDetailsResponses;
	    }

	    List<String> dealerIds = new ArrayList<>();
	    for (UserClientLocMapping clientLocMapping : clientLocMappingList) {
	        dealerIds.add(clientLocMapping.getDealerId());
	    }
		
		List<DealerLocation> dealerLocationList = new ArrayList<>();
		
		for(String dealer : dealerIds) {
			DealerLocation dealerLocation = dealerLocationRepository.getDealerLocationByDealerIdAndClientId(dealer, clientId);
			dealerLocationList.add(dealerLocation);
		}
		
		for (DealerLocation dealerLocation : dealerLocationList) {

	        ViewDealerDetailsResponse viewDealerDetailsResponse = new ViewDealerDetailsResponse();

	        viewDealerDetailsResponse.setId(dealerLocation.getId());
	        viewDealerDetailsResponse.setArea(dealerLocation.getArea());
	        viewDealerDetailsResponse.setCity(dealerLocation.getCity());
	        viewDealerDetailsResponse.setDealerId(dealerLocation.getDealerId());
	        viewDealerDetailsResponse.setDealerName(dealerLocation.getDealerName());
	        viewDealerDetailsResponse.setState(dealerLocation.getState());

	        viewDealerDetailsResponses.add(viewDealerDetailsResponse);
	    }
		
		return viewDealerDetailsResponses;
	}
	
	public void logout(HttpServletResponse response) {
		CookieUtils.clearJwtCookie(response);
	}
}