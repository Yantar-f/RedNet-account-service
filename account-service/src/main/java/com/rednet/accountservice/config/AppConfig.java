package com.rednet.accountservice.config;

import com.rednet.accountservice.entity.Role;
import com.rednet.accountservice.repository.RoleRepository;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class AppConfig {
    private final RoleRepository roleRepository;

    public AppConfig(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;

        createRolesIfNotExists();
    }

    private void createRolesIfNotExists() {
        Arrays.stream(RolesEnum.values()).map(RolesEnum::name).forEach(role -> {
            if ( ! roleRepository.existsById(role)) roleRepository.save(new Role(role));
        });
    }
}
