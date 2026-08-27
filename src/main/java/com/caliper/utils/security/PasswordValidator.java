package com.caliper.utils.security;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final Pattern PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }

    public static String getValidationMessage() {
        return "Password must be at least 8 characters and contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&).";
    }
}
