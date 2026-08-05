package com.chat.app.config;

import com.chat.app.entity.Role;
import com.chat.app.enums.RoleName;
import com.chat.app.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                log.info("Creating role: {}", roleName);
                return roleRepository.save(Role.builder().name(roleName).build());
            });
        }
    }
}
