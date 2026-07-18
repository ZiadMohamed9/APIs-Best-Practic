package com.auth.jwt.config;

import com.auth.jwt.entity.Role;
import com.auth.jwt.entity.User;
import com.auth.jwt.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return _ -> {
            String adminEmail = "admin@example.com";

            // Check if the admin user already exists so we don't insert it twice
            if (!userRepository.existsByEmail(adminEmail)) {
                User adminUser = User.builder()
                        .firstName("Super")
                        .lastName("Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(adminUser);
                System.out.println("Default Admin User created successfully!");
            }
        };
    }
}
