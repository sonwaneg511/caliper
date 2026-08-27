package com.caliper.security.config;

import com.caliper.security.jwt.AuthEntryPointJwt;
import com.caliper.security.jwt.AuthTokenFilter;
import com.caliper.usermanagement.authfilter.AuthorizationFilter;
import com.caliper.usermanagement.impl.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    public DataSource dataSource;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationAuthTokenFilter(){
        return new AuthTokenFilter();
    }

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

	
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthorizationFilter authorizationFilter) throws Exception {
        http.authorizeHttpRequests((requests) ->
        requests
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // Public endpoints
            .requestMatchers("/api/signup", "/api/request", "/api/change", "/api/login").permitAll()
        .anyRequest().permitAll());
       

        //The below line will be used while using tokens
                //requests.requestMatchers("/signin").permitAll().anyRequest().authenticated());
      //  http.formLogin(withDefaults());
                
        http.sessionManagement(session->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exception->
                exception.authenticationEntryPoint(unauthorizedHandler));
        
        http.headers(headers ->
                headers.frameOptions(frameOptions-> frameOptions.sameOrigin()));
        //http.httpBasic(withDefaults());
        
        //Disables Cross-Site Request Forgery protection.
        //CSRF protection is only needed for stateful web apps with sessions (like form-based login).
        //JWT-based APIs are stateless, so CSRF protection is unnecessary.
        http.csrf(csrf -> csrf.disable());
//        http.addFilterBefore(
//	            authorizationFilter,
//	            UsernamePasswordAuthenticationFilter.class);
        
        //Cors allowed
        http.cors(Customizer.withDefaults());//        .cors(cors -> {})
        http.addFilterBefore(authenticationAuthTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

   // @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

//    @Bean
//    public CommandLineRunner initData(UserDetailsService userDetailsService) {
//        return args -> {
//            UserDetails user1 = User.withUsername("user")
//                    .password(passwordEncoder().encode("password"))
//                    .roles("USER")
//                    .build();
//
//            UserDetails admin = User.withUsername("admin")
//                    .password(passwordEncoder().encode("password"))
//                    .roles("ADMIN")
//                    .build();
//
//            JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
//
//
//            if (!userDetailsManager.userExists("user")) {
//                userDetailsManager.createUser(user1);
//            }
//
//            if (!userDetailsManager.userExists("admin")) {
//                userDetailsManager.createUser(admin);
//            }
//        };
//        //return userDetailsManager;
//        //return new InMemoryUserDetailsManager(user1,admin);
//    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }
}
