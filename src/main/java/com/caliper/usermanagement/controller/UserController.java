package com.caliper.usermanagement.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.caliper.security.jwt.JwtUtils;
import com.caliper.usermanagement.dto.CreateUserRequestDto;
import com.caliper.usermanagement.dto.CreateUserResponseDto;
import com.caliper.usermanagement.dto.EditUserRequestDto;
import com.caliper.usermanagement.dto.ForgotPasswordResponse;
import com.caliper.usermanagement.dto.GenericMessageResponse;
import com.caliper.usermanagement.dto.NewUserResponseDto;
import com.caliper.usermanagement.dto.SignUpRequest;
import com.caliper.usermanagement.dto.SignUpResponse;
import com.caliper.usermanagement.dto.UserApiResponse;
import com.caliper.usermanagement.dto.UserRequestDto;
import com.caliper.usermanagement.dto.UserResponseDto;
import com.caliper.usermanagement.dto.ViewDealerDetailsResponse;
import com.caliper.usermanagement.dto.ViewExistingUserResponse;
import com.caliper.usermanagement.dto.request.ForgotPasswordInitRequest;
import com.caliper.usermanagement.dto.request.PasswordResetRequest;
import com.caliper.usermanagement.dto.request.ResendVerificationRequest;
import com.caliper.usermanagement.dto.request.UserActionRequest;
import com.caliper.usermanagement.dto.request.VerifyEmailRequest;
import com.caliper.usermanagement.entity.ForgotPassword;
import com.caliper.usermanagement.entity.RoleMaster;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.entity.UserClientLocMapping;
import com.caliper.usermanagement.entity.UserOperationEnum;
import com.caliper.usermanagement.entity.UserRoleClientMapping;
import com.caliper.usermanagement.service.UserManagementService;
import com.caliper.usermanagement.service.UserService;
import com.caliper.utils.security.PasswordResetRateLimiter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
@Tag(name = "User", description = "Operations related to user, components and roles")
public class UserController {

	@Autowired
	public UserService userService;
	
	@Autowired
	private UserManagementService userManagementService;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private PasswordResetRateLimiter passwordResetRateLimiter;

	@GetMapping("/")
	public String welcome() {
		System.out.println("Welcome");
		return "Welcome";
	}

	@PostMapping("/create/userrolelocmapping")
	@Operation(summary = "create user role loc mapping",
	responses = {
			@ApiResponse(
					responseCode = "200",
					description = "User role loc mapping Created Successfully"

					), @ApiResponse(
							responseCode = "400",
							description = "Bad Request"
							)
	}
			)
	public UserClientLocMapping insertUserRoleLocMapping(@RequestBody UserClientLocMapping userRoleLocMapping) {
		return userService.insertUserRoleLocMapping(userRoleLocMapping);
	}


	@GetMapping("/userrolelocmapping")
	@Operation(
			summary = "Get all user role loc mapping",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "List of all user role loc mapping fetched successfully"
							)
			}
			)
	public List<UserClientLocMapping> getAllUserRoleLocMappings() {
		return userService.getAllUserRoleLocMapping();
	}

	@PostMapping("/create/user")
	@Operation(summary = "create user",
	responses = {
			@ApiResponse(
					responseCode = "200",
					description = "user Created Successfully"

					), @ApiResponse(
							responseCode = "400",
							description = "Bad Request"
							)
	}
			)
	public User insertUser(@RequestBody User user) {
		return userService.insertUser(user);
	}


	@GetMapping("/user/{id}")
	@Operation(
			summary = "Get user by id",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "User fetched successfully"
							),
					@ApiResponse(
							responseCode = "404",
							description = "User not found"
							)
			}
			)
	public Optional<User> getUserById(@PathVariable long id) {
		return userService.getUserById(id);
	}

	@GetMapping("/user/{userId}")
	@Operation(
			summary = "Get user by user id",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "User fetched successfully"
							),
					@ApiResponse(
							responseCode = "404",
							description = "User not found"
							)
			}
			)
	public Optional<User> getUserByUserId(@PathVariable String userId) {
		return userService.getUserByUserId(userId);
	}


	@GetMapping("/user")
	@Operation(
			summary = "Get all users",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "List of all users fetched successfully"
							)
			}
			)
	public List<User> getAllUsers() {
		return userService.getAllUsers();
	}

	@PostMapping("/create/userclientmapping")
	@Operation(summary = "create user client mapping",
	responses = {
			@ApiResponse(
					responseCode = "200",
					description = "User Client mapping Created Successfully"

					), @ApiResponse(
							responseCode = "400",
							description = "Bad Request"
							)
	}
			)
	public UserRoleClientMapping inserUserClientMapping(@RequestBody UserRoleClientMapping userClientMapping) {
		return userService.insertUserClientMapping(userClientMapping);
	}


	@GetMapping("/userclientmapping/{id}")
	@Operation(
			summary = "Get user client mapping by id",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "User client mapping fetched successfully"
							),
					@ApiResponse(
							responseCode = "404",
							description = "User client mapping not found"
							)
			}
			)
	public Optional<UserRoleClientMapping> getUserClientMappingById(@PathVariable long id) {
		return userService.getUserClientMappingById(id);
	}

	@GetMapping("/userclientmapping")
	@Operation(
			summary = "Get all user client mapping",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "List of all user client mapping fetched successfully"
							)
			}
			)
	public List<UserRoleClientMapping> getAllUserClientMapping() {
		return userService.getAllUserClientMapping();
	}

	@PostMapping("/create/rolemaster")
	@Operation(summary = "create role master",
	responses = {
			@ApiResponse(
					responseCode = "200",
					description = "role master Created Successfully"

					), @ApiResponse(
							responseCode = "400",
							description = "Bad Request"
							)
	}
			)
	public RoleMaster insertRoleMaster(@RequestBody RoleMaster roleMaster) {
		return userService.insertRoleMaster(roleMaster);
	}


	@GetMapping("/rolemaster/{id}")
	@Operation(
			summary = "Get role master by id",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "role master fetched successfully"
							),
					@ApiResponse(
							responseCode = "404",
							description = "role master not found"
							)
			}
			)
	public Optional<RoleMaster> getRoleMasterById(@PathVariable long id) {
		return userService.getRoleMasterById(id);
	}

	@GetMapping("/rolemaster")
	@Operation(
			summary = "Get all role masters mapping",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "List of all role master fetched successfully"
							)
			}
			)
	public List<RoleMaster> getAllRoleMaster() {
		return userService.getAllRoleMaster();
	}

	@GetMapping("/rolemasterall/{id}")
	@Operation(
			summary = "Get all role masters mapping by role id",
			responses = {
					@ApiResponse(
							responseCode = "200",
							description = "List of all role master fetched successfully"
							)
			}
			)
	public List<RoleMaster> getAllRoleMasterById(@PathVariable long id) {
		return userService.getAllRoleMasterById(id);
	}



	@PostMapping("/createuser") 
	public CreateUserResponseDto createUser(@RequestBody CreateUserRequestDto createUserRequestDto) { 
		return userService.createUser(createUserRequestDto); 
	}
	
	@GetMapping("/user/self")
	public ResponseEntity<?> selfUserDetails(Authentication authentication){
		
		if(authentication == null || !authentication.isAuthenticated() ||
		        authentication instanceof AnonymousAuthenticationToken) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
		}
		
		UserDetails userDetails = (UserDetails)authentication.getPrincipal();
		
	    // Get business info from UserService
	    UserResponseDto resp = userService.loginCheck(userDetails.getUsername());
	    resp.setMessage("Successfully User Details Fetched");
	    
	    return ResponseEntity.ok()
	            .body(resp);
	}
	
	//user login
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserRequestDto userRequestDto) {

	    Authentication authentication = authenticationManager.authenticate(
	        new UsernamePasswordAuthenticationToken(
	            userRequestDto.getUserId(),
	            userRequestDto.getPassword()
	        )
	    );

	    SecurityContextHolder.getContext().setAuthentication(authentication);

	    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

	    // Get business info from UserService
	    UserResponseDto resp = userService.loginCheck(userDetails.getUsername());
	    
	    if(resp.isEmailVerified()) {
	    	return ResponseEntity.ok()
	    			.header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
	    			.body(resp);
	    }
	    return ResponseEntity.ok()
    			.body(resp);

	}

	@PostMapping("/logintest")
		public UserResponseDto loginCheck(@RequestBody UserRequestDto userRequestDto) {
			return userService.loginCheck(userRequestDto.getUserId());
	}

	@PostMapping("/forgotpassword")
	public ForgotPassword insertForgotPassword(@RequestBody ForgotPassword forgotPassword) {
		return userService.insertForgotPassword(forgotPassword);
	}

	@GetMapping("/forgotpasswordall")
	public List<ForgotPassword> getAllforgForgotPasswords() {
		return userService.getAllForgotPassword();
	}

	@GetMapping("/forgotpassword/{userId}")
	public Optional<ForgotPassword> getForgotPasswordByUserId(@PathVariable String userId) {
		return userService.getForgotPasswordByUserId(userId);
	}

	@PostMapping("/request")
	public ResponseEntity<GenericMessageResponse> requestForgotPassword(
			@RequestBody ForgotPasswordInitRequest request,
			jakarta.servlet.http.HttpServletRequest httpRequest) {

		String ip = httpRequest.getRemoteAddr();
		if (!passwordResetRateLimiter.isAllowed(ip)) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body(new GenericMessageResponse(HttpStatus.TOO_MANY_REQUESTS.value() , "Too many requests. Please try again in 15 minutes."));
		}

		userService.initiatePasswordReset(request.getEmail());

		// Always return the same message regardless of whether email exists
		return ResponseEntity.ok(new GenericMessageResponse(HttpStatus.OK.value(),
				"If your email is registered, you will receive a password reset link shortly."));
	}

	@PostMapping("/change")
	public ResponseEntity<ForgotPasswordResponse> changePassword(
			@RequestBody PasswordResetRequest request) {

		ForgotPasswordResponse response = userService.resetPassword(request);

		if (!response.isSuccess()) {
			return ResponseEntity.badRequest().body(response);
		}

		return ResponseEntity.ok(response);
	}


	//new admin user creation
	@PostMapping("/signup")
	public ResponseEntity<SignUpResponse> insertAdminUser(@Valid @RequestBody SignUpRequest signUpRequest) {
		return userService.createAdminUser(signUpRequest);
	}

	@PostMapping("/verify-email")
	public ResponseEntity<GenericMessageResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
		GenericMessageResponse response = userService.verifyEmail(request);

		if (response.getStatus() != HttpStatus.OK.value()) {
			return ResponseEntity.status(response.getStatus()).body(response);
		}

		return ResponseEntity.ok(response);
	}

	@PostMapping("/resend-verification")
	public ResponseEntity<GenericMessageResponse> resendVerification(@RequestBody ResendVerificationRequest request) {
		userService.resendVerificationEmail(request.getEmail());

		// Always return the same message regardless of whether the email exists or is already verified
		return ResponseEntity.ok(new GenericMessageResponse(HttpStatus.OK.value(),
				"If your email is registered and pending verification, you will receive a verification link shortly."));
	}

	@GetMapping("/viewallusers")
	 public List<NewUserResponseDto> viewAllUsers(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId) {
	     return userService.viewAllUsers(userId, clientId);
	 }

	 @GetMapping("/viewuser")
	 public ViewExistingUserResponse getUserForModify(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId) {
		 return userService.getUserForModify(userId, clientId);
	 }
	 
	 @PutMapping("/modify")
	 public ResponseEntity<Void> modifyUser(@RequestBody EditUserRequestDto dto) {

	     userService.modifyUser(dto);
	     return ResponseEntity.ok().build();
	 }
	 
	 @GetMapping("/viewlocations")
	 public List<ViewDealerDetailsResponse> getDealerDetails(@RequestParam("userId") String userId, @RequestParam("clientId") String clientId){
		 return userService.getDealerDetails(userId, clientId);
	 }	
//
//	@GetMapping("/viewusers")
//	public List<NewUserResponseDto> viewAllUsers(@RequestParam("userId") String userId) {
//		return userService.viewAllUsers(userId);
//	}
//
//	@DeleteMapping("/users/{id}")
//	public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {
//		userService.deleteUser(id);
//		return ResponseEntity.noContent().build();
//	}

	 @PostMapping("/user/create")
	 public ResponseEntity<UserApiResponse> insertUser(@RequestBody UserActionRequest request){
		 UserApiResponse response = userManagementService.handle(UserOperationEnum.INSERT, request);
		 return ResponseEntity.ok(response);
	 }
	 
	 @PostMapping("/user/edit")
	 public ResponseEntity<UserApiResponse> insertEdit(@RequestBody UserActionRequest request){
		 UserApiResponse response =  userManagementService.handle(UserOperationEnum.EDIT, request);
		 return ResponseEntity.ok(response);
	 }
	 
	 @PostMapping("/user/view")
	 public ResponseEntity<UserApiResponse> getUsers(@RequestBody UserActionRequest request){
		 UserApiResponse response = userManagementService.handle(UserOperationEnum.GET, request);
		 return ResponseEntity.ok(response);
	 }
	 
	 @PostMapping("/user/delete")
	 public ResponseEntity<UserApiResponse> deleteUser(@RequestBody UserActionRequest request){
		 UserApiResponse response =  userManagementService.handle(UserOperationEnum.DELETE, request);
		 return ResponseEntity.ok(response);
	 }
	 
	 @PostMapping("/logout")
	 public ResponseEntity<?> logout(HttpServletResponse response) {
	     userService.logout(response);
	     return ResponseEntity.ok("{\"message\":\"Logged out successfully\"}");
	 }
}
