package com.caliper.planmanagement.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.caliper.campaign.google.entity.GoogleCampaign;
import com.caliper.campaign.google.repository.GoogleCampaignRepository;
import com.caliper.location.entity.Client;
import com.caliper.location.entity.DealerLocation;
import com.caliper.location.repository.ClientRepository;
import com.caliper.location.repository.DealerLocationRepository;
import com.caliper.onboarding.service.OnboardingService;
import com.caliper.planmanagement.dto.CampaignPaymentDetailDto;
import com.caliper.planmanagement.dto.CampaignPaymentReportRequest;
import com.caliper.planmanagement.dto.CampaignPaymentReportResponse;
import com.caliper.planmanagement.dto.PaymentPlanRequest;
import com.caliper.planmanagement.dto.PaymentPlanResponse;
import com.caliper.planmanagement.dto.PlanRequestDto;
import com.caliper.planmanagement.dto.PlanResponseDto;
import com.caliper.planmanagement.dto.SubscriptionDetailResponse;
import com.caliper.planmanagement.dto.SubscriptionHistoryResponse;
import com.caliper.planmanagement.entity.Plan;
import com.caliper.planmanagement.entity.PlanHistory;
import com.caliper.planmanagement.entity.PlanServiceMaster;
import com.caliper.planmanagement.entity.ServicePlanMapping;
import com.caliper.planmanagement.entity.payment.CaliperPayment;
import com.caliper.planmanagement.repository.PlanHistoryRepository;
import com.caliper.planmanagement.repository.PlanRepository;
import com.caliper.planmanagement.repository.PlanServiceMasterRepository;
import com.caliper.planmanagement.repository.ServicePlanMappingRepository;
import com.caliper.planmanagement.repository.payment.CaliperPaymentRepository;
import com.caliper.usermanagement.entity.Roles;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.repository.UserClientLocMappingRepository;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;
import com.caliper.utils.exception.customException.ResourceNotFoundException;

@Service
public class PlanService {

	private static final int CAMPAIGN_PAYMENTS_DEFAULT_PAGE_SIZE = 10;

	@Autowired
	public PlanServiceMasterRepository planServiceMasterRepository;
	
	@Autowired
	public ClientRepository clientRepository;
	
	@Autowired
	public PlanRepository planRepository;
	
	@Autowired
	public PlanHistoryRepository planHistoryRepository;
	
	@Autowired
	public ServicePlanMappingRepository servicePlanMappingRepository;
	
	@Autowired
	public UserRoleClientMappingRepository userRoleClientMappingRepository;
	
	@Autowired
	public UserRepository userRepository;
	
	@Autowired
	public CaliperPaymentRepository caliperPaymentRepository;
	
	@Autowired
	public GoogleCampaignRepository googleCampaignRepository;

	@Autowired
	public UserClientLocMappingRepository userClientLocMappingRepository;

	@Autowired
	public DealerLocationRepository dealerLocationRepository;

	@Autowired
	private OnboardingService onboardingService;

	// 1---------------------------GET ALL PLANS API---------------------------
	public ResponseEntity<List<PlanServiceMaster>> showPlans(){
		List<PlanServiceMaster> planServiceMasters = planServiceMasterRepository.findAll();

	    // Return empty list if no plans exist
	    if (planServiceMasters.isEmpty()) {
	    	return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ArrayList<>());
	    }
	    
	    return ResponseEntity.ok(planServiceMasters);
	}
	
	// 2---------------------------CREATE PLANS---------------------------
	public ResponseEntity<PlanResponseDto> createPlan(PlanRequestDto dto) {

	    PlanResponseDto resp = new PlanResponseDto();

	    // NULL CHECK
	    if (dto == null) {
	        resp.setStatus(HttpStatus.BAD_REQUEST.value());
	        resp.setMessage("Invalid plan creation request");
	        resp.setError("Request body cannot be null");
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
	    }

	    if (dto.getStartDate() == null || dto.getEndDate() == null) {
	        resp.setStatus(HttpStatus.BAD_REQUEST.value());
	        resp.setMessage("Start date and end date are required");
	        return ResponseEntity.badRequest().body(resp);
	    }
	    
	    //check for duplicate client name
		/*
		 * boolean exists = clientRepository.findAll().stream().anyMatch(client ->
		 * client.getClientName() .equalsIgnoreCase(dto.getClientName()));
		 */
	    
		/*
		 * if(exists) { resp.setStatus(HttpStatus.BAD_REQUEST.value());
		 * resp.setMessage("Client Name already exists"); return
		 * ResponseEntity.badRequest().body(resp); }
		 */
	    
	    // CREATE CLIENT

		/*
		 * Client client = new Client(); client.setClientName(dto.getClientName());
		 * client.setEmail(dto.getUserId().toLowerCase()); client.setClientAddedDate(new
		 * Date()); client.setClientId(dto.getClientName().trim().replace(" ", "_") +
		 * "_" + randomNum);
		 * 
		 * clientRepository.save(client);
		 */
	    
	    int randomNum = (int) (Math.random() * 900000) + 100000;
	    Client existingClient = clientRepository.findByEmail(dto.getUserId());
	    if(existingClient == null) {
	    	resp.setStatus(HttpStatus.NOT_FOUND.value());
	    	resp.setMessage("Client Not Found");
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
	    } else {
	    existingClient.setClientId(existingClient.getClientName().replace(" ", "_") + "_" + randomNum);
	    clientRepository.save(existingClient);
	    }

	    // FETCH latest client
	    Client latestClient = clientRepository.findTopByOrderByIdDesc();
	    if (latestClient == null) {
	        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
	        resp.setMessage("Client creation failed");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
	    }

	    //UPDATE CLIENT ID IN USER TABLE
	    System.out.print("Email: " +latestClient.getEmail());
	    Optional<User> latestUserOpt = userRepository.findByUserId(latestClient.getEmail());

	    if (latestUserOpt.isPresent()) {
	        User latestUser = latestUserOpt.get();
	        System.out.print("Email: " +latestUser.getUserId().toLowerCase());

	        latestUser.setClientId(latestClient.getClientId());
	        latestUser.setActive(Plan.STATUS_ACTIVE);
	        userRepository.save(latestUser);
	    }
	    
	    // CREATE PLAN
	    Plan newPlan = new Plan();
	    newPlan.setClientId(latestClient.getClientId());
	    newPlan.setStartDate(dto.getStartDate());
	    newPlan.setEndDate(dto.getEndDate());   // <---- USING REQUEST END DATE
	    newPlan.setLocationCount(dto.getLocationCount());
	    newPlan.setStatus(Plan.STATUS_ACTIVE);
	    newPlan.setCreatedBy(dto.getUserId().toLowerCase());
	    newPlan.setCreatedAt(new Date());

	    planRepository.save(newPlan);

	    // FETCH latest plan ID
	    Long latestPlanId = planRepository.findLatestId();

	    // SERVICE PLAN MAPPING
	    for (String id : dto.getServiceIds()) {
	        ServicePlanMapping mapping = new ServicePlanMapping();
	        mapping.setPlanId(latestPlanId);
	        mapping.setServiceKey(id);
	        servicePlanMappingRepository.save(mapping);
	    }

	    // Initialize onboarding state based on purchased services
	    onboardingService.initializeFromPlan(
	            latestClient.getClientId(),
	            dto.getUserId().toLowerCase(),
	            dto.getServiceIds());

	    // USER ROLE MAPPING
	    System.out.print("CLIENT ID LATEST: " + latestClient.getClientId());
	    UserRoleClientMapping mapping = new UserRoleClientMapping();
	    mapping.setUserId(dto.getUserId().toLowerCase());
	    mapping.setClientId(latestClient.getClientId());
	    mapping.setRole(UserRoleClientMapping.ROLE_SUPER_ADMIN);

	    userRoleClientMappingRepository.save(mapping);

	    // SUCCESS RESPONSE
	    resp.setStatus(HttpStatus.CREATED.value());
	    resp.setMessage("Plan created successfully");

	    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
	}
	
	// 2---------------------------CREATE PAYMENTS---------------------------
	public ResponseEntity<PaymentPlanResponse> createPayment(PlanRequestDto planRequestDto) {

	    PaymentPlanResponse response = new PaymentPlanResponse();
	    response.setTimestamp(new Date());

	    try {
	        // Validate request object
	        if (planRequestDto == null || planRequestDto.getPaymentPlanRequest() == null) {
	            response.setStatus(HttpStatus.BAD_REQUEST.value());
	            response.setCode(HttpStatus.BAD_REQUEST.value());
	            response.setMessage("Payment plan request is empty");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        PaymentPlanRequest req = planRequestDto.getPaymentPlanRequest();

	        // Validate mandatory fields
//	        if (req.getPaymentId() == null || req.getOrderId() == null || req.getAmount() <= 0) {
//	            response.setStatus(HttpStatus.BAD_REQUEST.value());
//	            response.setCode(HttpStatus.BAD_REQUEST.value());
//	            response.setMessage("Missing or invalid payment details");
//	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//	        }

	        // Process PLAN payment
	        if (req.isPlan()) {
	        	
	        	Plan latestPlan = planRepository.findTopByOrderByIdDesc();

	        	if(latestPlan == null) {
	        		 response.setStatus(HttpStatus.NOT_FOUND.value());
	                 response.setCode(HttpStatus.NOT_FOUND.value());
	                 response.setMessage("No plan found to attach payment");
	                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	        	}
	            	
	            CaliperPayment payment = CaliperPayment.builder()
	                    .clientId(latestPlan.getClientId())
	                    .paymentId(req.getPaymentId())
	                    .orderId(req.getOrderId())
	                    .amount(req.getAmount())
	                    .status(req.getStatus())
	                    .planId(latestPlan.getId())
	                    .paymentChannelType(req.getPaymentMethod())
	                    .paymentMethod(req.getPaymentMethod())
	                    .transactionDatetime(req.getTransactionDatetime() != null ? req.getTransactionDatetime() : new Date())
	                    .errorDescription(req.getErrorDescription())
	                    .amountRefunded(req.getAmountRefunded())
	                    .userId(latestPlan.getCreatedBy())
	                    .build();

	            caliperPaymentRepository.save(payment);
	        }

	        // Process CAMPAIGN payment
	        if (req.isCampaign()) {
	        	
	        	Optional<GoogleCampaign> latestOptCampaign = googleCampaignRepository.findById(req.getCampaignId());
	        	
	        	if(latestOptCampaign.isPresent()) {
	        		GoogleCampaign latestCampaign = latestOptCampaign.get();
	        		
	        		if(req.getStatus().equalsIgnoreCase(CaliperPayment.PAYMENT_SUCCESS)) {
		        		latestCampaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAYMENT_SUCCESSFUL);
		        		googleCampaignRepository.save(latestCampaign);
		        	} else if(req.getStatus().equalsIgnoreCase(CaliperPayment.PAYMENT_FAILED)){
		        		latestCampaign.setStatus(GoogleCampaign.CALIPER_CAMPAIGN_STATUS_PAYMENT_FAILED);
		        		googleCampaignRepository.save(latestCampaign);
		        	}
	        	} else {
	        		throw new ResourceNotFoundException("Campaign not found for campaign Id" + req.getCampaignId());
	        	}
	        	
	            CaliperPayment payment = CaliperPayment.builder()
	                    .clientId(planRequestDto.getClientId())
	                    .paymentId(req.getPaymentId())
	                    .orderId(req.getOrderId())
	                    .amount(req.getAmount())
	                    .status(req.getStatus())
	                    .campaignId(req.getCampaignId())
	                    .paymentChannelType(req.getPaymentMethod())
	                    .paymentMethod(req.getPaymentMethod())
	                    .transactionDatetime(req.getTransactionDatetime() != null ? req.getTransactionDatetime() : new Date())
	                    .errorDescription(req.getErrorDescription())
	                    .amountRefunded(req.getAmountRefunded())
	                    .userId(planRequestDto.getUserId())
	                    .build();

	            caliperPaymentRepository.save(payment);
	        }

	        // SUCCESS response
	        response.setStatus(HttpStatus.CREATED.value());
	        response.setCode(HttpStatus.CREATED.value());
	        response.setMessage("Payment created successfully");
	        return ResponseEntity.status(HttpStatus.CREATED).body(response);

	    } catch (Exception ex) {

	        // INTERNAL SERVER ERROR
	        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
	        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
	        response.setMessage("Internal server error: " + ex.getMessage());

	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	public List<PlanHistory> getPlanHistory(String clientId) {
		return planHistoryRepository.findByClientIdOrderByCreatedAtDesc(clientId);
	}

	public SubscriptionDetailResponse getSubscriptionDetail(String clientId) {
		Plan plan = planRepository.findByClientId(clientId);
		if (plan == null) {
			throw new ResourceNotFoundException("No active plan found for clientId: " + clientId);
		}

		List<ServicePlanMapping> mappings = servicePlanMappingRepository.findByPlanId(plan.getId());
		List<String> serviceKeys = mappings.stream()
				.map(ServicePlanMapping::getServiceKey)
				.collect(Collectors.toList());

		List<PlanServiceMaster> masters = planServiceMasterRepository.findAllByServiceKeyIn(serviceKeys);
		List<String> moduleNames = masters.stream()
				.map(PlanServiceMaster::getServiceName)
				.collect(Collectors.toList());

		Optional<CaliperPayment> paymentOpt = caliperPaymentRepository.findByPlanId(plan.getId());
		double amount = paymentOpt.map(CaliperPayment::getAmount).orElse(0.0);

		return SubscriptionDetailResponse.builder()
				.planName(plan.getPlanName())
				.renewalDate(plan.getStartDate())
				.amount(amount)
				.locationCount(plan.getLocationCount())
				.purchasedModules(moduleNames)
				.expiresOn(plan.getEndDate())
				.status(plan.getStatus())
				.durationType(plan.getDurationType() != null ? plan.getDurationType().name() : null)
				.build();
	}

	public Page<SubscriptionHistoryResponse> getSubscriptionHistory(String clientId, int page, int size) {
		Page<PlanHistory> historyPage = planHistoryRepository.findByClientIdOrderByCreatedAtDesc(
				clientId, PageRequest.of(page, size));
		return historyPage.map(ph -> SubscriptionHistoryResponse.builder()
				.planId(ph.getPlanId())
				.planName(ph.getPlanName())
				.billingDate(ph.getCreatedAt())
				.amount(ph.getAmount() != null ? ph.getAmount() : 0.0)
				.status(ph.getStatus())
				.build());
	}

	public List<String> getAllowedRoles(String clientId){
		Plan plan  = planRepository.findByClientId(clientId);
		if (plan == null) {
			return new ArrayList<>();
		}
		List<ServicePlanMapping> servicePlanMappings = servicePlanMappingRepository.findByPlanId(plan.getId());
		List<String> allowedroles = new ArrayList<>();
		for(ServicePlanMapping mapping : servicePlanMappings) {
			allowedroles.add(mapping.getServiceKey());
		}
		
		return allowedroles;
	}

	// ---------------------------GET CAMPAIGN PAYMENTS FOR USER'S MAPPED DEALERS---------------------------
	public ResponseEntity<CampaignPaymentReportResponse> getCampaignPaymentsForUser(CampaignPaymentReportRequest request, Authentication authentication) {

		if (request == null || request.getUserId() == null || request.getClientId() == null
				|| request.getStartDate() == null || request.getEndDate() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					CampaignPaymentReportResponse.builder()
							.status(HttpStatus.BAD_REQUEST.value())
							.message("startDate, endDate, clientId and userId are required")
							.payments(new ArrayList<>())
							.build());
		}

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
					CampaignPaymentReportResponse.builder()
							.status(HttpStatus.UNAUTHORIZED.value())
							.message("Authentication required")
							.payments(new ArrayList<>())
							.build());
		}

		// Authorization is scoped to the JWT-authenticated caller, not the request body's
		// userId/clientId — otherwise anyone could read another client's payment data by
		// simply supplying a different clientId in the request.
		String authenticatedUserId = ((UserDetails) authentication.getPrincipal()).getUsername();

		List<UserRoleClientMapping> callerRoles = userRoleClientMappingRepository
				.getUserRoleClientMappingByUserIdAndClientId(authenticatedUserId, request.getClientId());

		boolean isAdmin = callerRoles.stream().anyMatch(mapping ->
				Roles.ADMIN.name().equalsIgnoreCase(mapping.getRole())
						|| Roles.SUPER_ADMIN.name().equalsIgnoreCase(mapping.getRole()));

		if (!isAdmin) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
					CampaignPaymentReportResponse.builder()
							.status(HttpStatus.FORBIDDEN.value())
							.message("You do not have permission to access campaign payments for this client")
							.payments(new ArrayList<>())
							.build());
		}

		// 1. Resolve dealerIds mapped to this user for the given client
		List<UserClientLocMapping> userClientLocMappings =
				userClientLocMappingRepository.findByUserIdAndclientId(request.getUserId(), request.getClientId());

		Set<String> dealerIds = userClientLocMappings.stream()
				.map(UserClientLocMapping::getDealerId)
				.collect(Collectors.toSet());

		if (dealerIds.isEmpty()) {
			return ResponseEntity.ok(CampaignPaymentReportResponse.builder()
					.status(HttpStatus.OK.value())
					.message("No dealer mapping found for user")
					.payments(new ArrayList<>())
					.build());
		}

		// 2. Resolve campaignIds for the mapped dealers within the requested date range
		List<GoogleCampaign> campaigns = googleCampaignRepository.findByClientIdAndDealerIdInAndStartDateBetween(
				request.getClientId(), dealerIds, request.getStartDate(), request.getEndDate());

		List<Long> campaignIds = campaigns.stream()
				.map(GoogleCampaign::getId)
				.collect(Collectors.toList());

		if (campaignIds.isEmpty()) {
			return ResponseEntity.ok(CampaignPaymentReportResponse.builder()
					.status(HttpStatus.OK.value())
					.message("No campaigns found for mapped dealers")
					.payments(new ArrayList<>())
					.build());
		}

		// 3. Fetch all CaliperPayment records for those campaignIds
		List<CaliperPayment> payments = caliperPaymentRepository.findByCampaignIdIn(campaignIds);

		// 4. Join campaign and dealer-location details onto each payment
		Map<Long, GoogleCampaign> campaignById = campaigns.stream()
				.collect(Collectors.toMap(GoogleCampaign::getId, Function.identity()));

		List<DealerLocation> dealerLocations = dealerLocationRepository.findByDealerIdIn(dealerIds);
		Map<String, DealerLocation> dealerLocationByDealerId = dealerLocations.stream()
				.collect(Collectors.toMap(DealerLocation::getDealerId, Function.identity(), (a, b) -> a));

		List<CampaignPaymentDetailDto> paymentDetails = payments.stream()
				.map(payment -> buildCampaignPaymentDetail(payment, campaignById, dealerLocationByDealerId))
				.collect(Collectors.toList());

		// Pagination
		int pageSize = request.getPageSize() > 0 ? request.getPageSize() : CAMPAIGN_PAYMENTS_DEFAULT_PAGE_SIZE;
		int pageNo = Math.max(request.getPageNo(), 0);

		int totalRecords = paymentDetails.size();
		int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

		int fromIndex = pageNo * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, totalRecords);

		List<CampaignPaymentDetailDto> paginatedPayments =
				fromIndex < totalRecords ? paymentDetails.subList(fromIndex, toIndex) : new ArrayList<>();

		return ResponseEntity.ok(CampaignPaymentReportResponse.builder()
				.status(HttpStatus.OK.value())
				.message("Payments fetched successfully")
				.payments(paginatedPayments)
				.totalNoOfPages(totalPages)
				.totalNoOfRecords(totalRecords)
				.build());
	}

	// Builds one payment-detail row, joining in campaign and dealer-location fields.
	// Campaign/location are left blank rather than dropping the payment if either lookup misses.
	private CampaignPaymentDetailDto buildCampaignPaymentDetail(CaliperPayment payment,
			Map<Long, GoogleCampaign> campaignById, Map<String, DealerLocation> dealerLocationByDealerId) {

		GoogleCampaign campaign = campaignById.get(payment.getCampaignId());

		String campaignName = null;
		String location = null;
		Date startDate = null;
		Date endDate = null;
		String campaignStatus = null;
		String scheduleStatus = null;
		long duration = 0;

		if (campaign != null) {
			campaignName = campaign.getCampaignName();
			startDate = campaign.getStartDate();
			endDate = campaign.getEndDate();
			campaignStatus = campaign.getStatus();

			if (startDate != null && endDate != null) {
				// endDate is @Temporal(TemporalType.DATE), which Hibernate backs with java.sql.Date —
				// its toInstant() always throws UnsupportedOperationException, so go via getTime() instead.
				LocalDate startLocalDate = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate endLocalDate = Instant.ofEpochMilli(endDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
				duration = ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1;

				LocalDate today = LocalDate.now();
				if (today.isBefore(startLocalDate)) {
					scheduleStatus = GoogleCampaign.SCHEDULE_STATUS_SCHEDULED;
				} else if (today.isAfter(endLocalDate)) {
					scheduleStatus = GoogleCampaign.SCHEDULE_STATUS_COMPLETED;
				} else {
					scheduleStatus = GoogleCampaign.SCHEDULE_STATUS_ACTIVE;
				}
			}

			DealerLocation dealerLocation = dealerLocationByDealerId.get(campaign.getDealerId());
			if (dealerLocation != null) {
				location = dealerLocation.getDealerName() + "-" + dealerLocation.getDealerId();
			}
		}

		double amountPerDay = duration > 0 ? payment.getAmount() / duration : payment.getAmount();

		return CampaignPaymentDetailDto.builder()
				.campaignName(campaignName)
				.location(location)
				.startDate(startDate)
				.endDate(endDate)
				.campaignStatus(campaignStatus)
				.scheduleStatus(scheduleStatus)
				.duration(duration)
				.amountPaid(payment.getAmount())
				.amountPerDay(amountPerDay)
				.paymentDate(payment.getTransactionDatetime())
				.build();
	}

}
