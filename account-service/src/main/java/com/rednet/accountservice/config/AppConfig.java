package com.rednet.accountservice.config;

import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.repository.RoleRepository;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class AppConfig {
    public AppConfig(RoleRepository roleRepository) {
        Arrays.stream(EnumRoles.values()).map(EnumRoles::name).forEach(role -> {
            if ( ! roleRepository.existsById(role)) roleRepository.save(new Role(role));
        });
    }
}
