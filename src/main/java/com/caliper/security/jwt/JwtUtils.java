package com.caliper.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger((JwtUtils.class));

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.info("Bearer Token: {}", bearerToken);
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);//get Bearer token
        }
        return null;
    }

    public String generateTokenFromUsername(UserDetails userDetails){

        String username = userDetails.getUsername();
        String compact = Jwts.builder().subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();

        return compact;
    }

    public String getUserNameFromToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    
    public ResponseCookie generateJwtCookie(UserDetails userDetails) {

        String jwt = generateTokenFromUsername(userDetails);

        return ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)              // true in prod (HTTPS)
                .path("/")
                .domain(".thecaliper.ai")
                .maxAge(jwtExpirationMs / 1000)
                .sameSite("None")          // "Lax" for same-domain
                .build();
    }


    public void validateToken(String token) {
        try {
            System.out.println("Validating token");

            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token);

        } catch (MalformedJwtException e) {
            logger.error("Invalid Jwt token: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            logger.error("Expired Jwt token: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported Jwt token: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Jwt claims string is empty: {}", e.getMessage());
            throw e;
        }
    }




}
