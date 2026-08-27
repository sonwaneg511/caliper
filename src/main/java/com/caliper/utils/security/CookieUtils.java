package com.caliper.utils.security;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

//	public static void clearJwtCookie(HttpServletResponse response) {
//        Cookie cookie = new Cookie("jwt", "");
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(0);
//        cookie.setDomain(".thecaliper.ai");
//        response.addCookie(cookie);
//    }
	
	public static void clearJwtCookie(HttpServletResponse response) {

	    ResponseCookie cookie = ResponseCookie.from("jwt", "")
	            .httpOnly(true)
	            .secure(true)
	            .path("/")
	            .domain(".thecaliper.ai")
	            .sameSite("None")
	            .maxAge(0)
	            .build();

	    response.addHeader("Set-Cookie", cookie.toString());
	}
	
}
