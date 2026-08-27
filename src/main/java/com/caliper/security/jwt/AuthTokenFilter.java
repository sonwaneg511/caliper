package com.caliper.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsService userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		logger.debug("AuthTokenFilter called for URI:{}", request.getRequestURI());

		try{
			String jwt = getJwtFromCookies(request);
			
			//  String jwt = parseJwt(request);
			//            if (jwt != null && jwtUtils.validateToken(jwt)) {
			if (jwt != null) {

				// This will throw if token is invalid/expired
				jwtUtils.validateToken(jwt);
				String userNameFromToken = jwtUtils.getUserNameFromToken(jwt);
				System.out.println("userNameFromToken = "+userNameFromToken);

				//This ensures the user still exists and their account status is valid (not disabled, etc.).
				UserDetails userDetails = userDetailsService.loadUserByUsername(userNameFromToken);

				//Creates an Authentication object that Spring Security uses internally.
				//
				//Parameters:
				//
				//userDetails: The authenticated principal.
				//
				//null: The credentials (password), not needed since we’re authenticating via JWT.
				//
				//userDetails.getAuthorities(): The roles/permissions granted to the user.
				//
				//This tells Spring Security: “Here’s a verified user with these roles.”
				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(userDetails, null,
								userDetails.getAuthorities());
				logger.debug("Roles for userName from JWT:{}", userDetails.getAuthorities());

				//Adds extra request details (like IP address, session ID, etc.) to the authentication object.
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				//It stores the authentication object in the Spring Security Context, which is thread-local.
				//
				//From this point onward, the user is considered authenticated for the duration of this request.
				SecurityContextHolder.getContext().setAuthentication(authentication);

			}
			
		}catch (ExpiredJwtException e) {
			logger.warn("JWT expired: {}", e.getMessage());
			clearJwtCookie(response);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\":\"Session expired. Please login again.\"}");
			return; // Stop further processing

		} catch (Exception e) {
			logger.error("Invalid JWT: {}", e.getMessage());
			clearJwtCookie(response);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"message\":\"Invalid token.\"}");
			return; // Stop further processing
		}

		filterChain.doFilter(request,response);
	}

	private void clearJwtCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("jwt", "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true); // true in production
		cookie.setPath("/");
		cookie.setMaxAge(0); // delete cookie
		response.addCookie(cookie);
	}


	private String getJwtFromCookies(HttpServletRequest request) {
		//System.out.println("Cookie header = " + request.getHeader("Cookie"));

		if (request.getCookies() == null) return null;
		for (Cookie cookie : request.getCookies()) {
			if ("jwt".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}


	private String parseJwt(HttpServletRequest request){
		String jwtFromHeader = jwtUtils.getJwtFromHeader(request);
		logger.info("AuthTokenFilter.java:{}",jwtFromHeader);
		return jwtFromHeader;
	}
}
