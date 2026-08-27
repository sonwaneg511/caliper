package com.caliper.usermanagement.authfilter;

import com.caliper.usermanagement.service.AuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class AuthorizationFilter extends OncePerRequestFilter {
    
    private final AuthorizationService authorizationService;

    @Autowired
    public AuthorizationFilter(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);

        // Read request body before proceeding
        String body = new String(requestWrapper.getContentAsByteArray());

        if (body != null && !body.isEmpty()) {
            JsonNode json = objectMapper.readTree(body);

            if (json.has("user_id")) {
                String userId = json.get("user_id").asText();
                String clientId = json.get("client_id").asText();
                String service = resolveService(request.getRequestURI());

                // Check service access
                if (!authorizationService.hasAccessToService(clientId, userId, service)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User has no access to service: " + service);
                    return; // IMPORTANT: stop filter chain
                }

                // Check dealer access
                if (json.has("dealerIds")) {
                    List<String> dealerIds = new ArrayList<>();
                    for (JsonNode node : json.get("dealerIds")) {
                        dealerIds.add(node.asText());
                    }
                    if (!authorizationService.hasAccessToLocations(clientId, userId, dealerIds)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "User has no access to one or more dealers");
                        return; // stop filter chain
                    }
                }
            }
        }

        // Only proceed if all checks pass
        filterChain.doFilter(requestWrapper, response);
    }


    private String resolveService(String uri) {
            if (uri.startsWith("/post")) return "POST";
            if (uri.startsWith("/review")) return "REVIEW";
            if (uri.startsWith("/location")) return "LOCATION";
            if (uri.startsWith("/campaign")) return "CAMPAIGN";

        return "UNKNOWN";
    }

}
