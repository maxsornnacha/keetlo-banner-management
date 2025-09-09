package com.keetlo.banner_management;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

@Configuration
@EnableWebSecurity
public class ApplicationConfig {

    // Database connection
    @Bean
    public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                System.out.println("Welcome to Spring Boot API!");

                String sql = "SELECT 1";
                Integer result = jdbcTemplate.queryForObject(sql, Integer.class);

                if(result != null && result == 1){
                    System.out.println("Database connection successful!");
                }

            } catch (Exception error){
                System.err.println("Error connection to the database: " + error.getMessage());
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ปิด CSRF ถ้าเป็น API
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll() // อนุญาตบาง path
//                        .anyRequest().permitAll() // หรือเปิดหมด
//                )
                .formLogin(form -> form.disable()) // ปิดหน้า login form
                .httpBasic(basic -> basic.disable()); // ปิด basic auth ด้วยถ้าไม่ต้องการ
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
