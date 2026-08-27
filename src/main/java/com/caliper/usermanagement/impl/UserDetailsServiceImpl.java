package com.caliper.usermanagement.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.caliper.usermanagement.entity.User;
import com.caliper.usermanagement.repository.UserRepository;
import com.caliper.usermanagement.repository.UserRoleClientMappingRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleClientMappingRepository roleRepo;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        // Email-verification status is checked after password match in UserService.loginCheck,
        // not here, so that a wrong password always yields "invalid credentials" rather than
        // this account's verification status.
        List<SimpleGrantedAuthority> authorities = roleRepo.getUserRoleClientMappingByUserId(userId)
                .stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRole()))
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(User.STATUS_INACTIVE.equals(user.getActive()))
                .build();
    }
}

