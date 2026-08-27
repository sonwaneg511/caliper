package com.caliper.activity.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.caliper.activity.entity.ActivityLog;
import com.caliper.activity.service.ActivityLogService;
import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ActivityLogFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(ActivityLogFilter.class);

	@Autowired
	private ActivityLogService activityLogService;

	@Autowired
	private UserRepository userRepository;

	private static final List<String> EXCLUDED_PATH_PREFIXES = Arrays.asList("/health", "/swagger", "/actuator",
			"/swagger-ui", "/v3/api-docs", "/favicon", "/api/jobs", "/jobs", "/logs");

	private static final List<String> EXCLUDED_EXTENSIONS = Arrays.asList(".css", ".js", ".html", ".ico", ".png",
			".jpg", ".jpeg", ".gif", ".svg", ".woff", ".woff2", ".map");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI().toLowerCase();
		return EXCLUDED_PATH_PREFIXES.stream().anyMatch(path::startsWith)
				|| EXCLUDED_EXTENSIONS.stream().anyMatch(path::endsWith);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} finally {
			try {
				saveActivityLog(request, response);
			} catch (Exception e) {
				logger.error("Failed to save activity log: {}", e.getMessage());
			}
		}
	}

	private void saveActivityLog(HttpServletRequest request, HttpServletResponse response) {
		String userId = extractUserId();
		String clientId = resolveClientId(userId);
		String userAgent = request.getHeader("User-Agent");

		ActivityLog log = ActivityLog.builder().userId(userId).clientId(clientId).timestamp(new Date())
				.userAgent(userAgent).browser(parseBrowser(userAgent)).operatingSystem(parseOperatingSystem(userAgent))
				.deviceName(parseDeviceType(userAgent)).ipAddress(extractClientIp(request))
				.endpoint(request.getRequestURI()).httpMethod(request.getMethod()).responseStatus(response.getStatus())
				.sessionId(request.getRequestedSessionId()).build();

		activityLogService.save(log);
	}

	private String extractUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
			return auth.getName();
		}
		return null;
	}

	private String resolveClientId(String userId) {
		if (userId == null)
			return null;
		return userRepository.findByUserId(userId).map(User::getClientId).orElse(null);
	}

	private String extractClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
			return ip.split(",")[0].trim();
		}
		ip = request.getHeader("X-Real-IP");
		if (ip != null && !ip.isEmpty()) {
			return ip;
		}
		return request.getRemoteAddr();
	}

	private String parseBrowser(String userAgent) {
		if (userAgent == null)
			return "Unknown";
		if (userAgent.contains("Edg/"))
			return "Edge";
		if (userAgent.contains("OPR/") || userAgent.contains("Opera"))
			return "Opera";
		if (userAgent.contains("Chrome"))
			return "Chrome";
		if (userAgent.contains("Firefox"))
			return "Firefox";
		if (userAgent.contains("Safari"))
			return "Safari";
		if (userAgent.contains("MSIE") || userAgent.contains("Trident"))
			return "Internet Explorer";
		return "Unknown";
	}

	private String parseOperatingSystem(String userAgent) {
		if (userAgent == null)
			return "Unknown";
		if (userAgent.contains("Android"))
			return "Android";
		if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
			return "iOS";
		if (userAgent.contains("Windows NT"))
			return "Windows";
		if (userAgent.contains("Mac OS X"))
			return "macOS";
		if (userAgent.contains("Linux"))
			return "Linux";
		return "Unknown";
	}

	private String parseDeviceType(String userAgent) {
		if (userAgent == null)
			return "Unknown";
		if (userAgent.contains("Mobile"))
			return "Mobile";
		if (userAgent.contains("Tablet"))
			return "Tablet";
		return "Desktop";
	}
}
