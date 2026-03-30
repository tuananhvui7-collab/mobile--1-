package com.ecommerce.mobile.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.mobile.entity.Employee;
import com.ecommerce.mobile.entity.Manager;
import com.ecommerce.mobile.entity.Role;
import com.ecommerce.mobile.repository.EmployessRepository;
import com.ecommerce.mobile.repository.ManagerRepository;
import com.ecommerce.mobile.repository.RoleRepository;

@Configuration
public class DataSeederConfig {

    @Bean
    CommandLineRunner seedInternalAccounts(RoleRepository roleRepository,
                                           EmployessRepository employessRepository,
                                           ManagerRepository managerRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            Role managerRole = roleRepository.findByNameRole("MANAGER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setNameRole("MANAGER");
                        return roleRepository.save(role);
                    });

            Role employeeRole = roleRepository.findByNameRole("EMPLOYEE")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setNameRole("EMPLOYEE");
                        return roleRepository.save(role);
                    });

            if (managerRepository.findByEmail("manager1@gmail.com").isEmpty()) {
                Manager manager = new Manager();
                manager.setEmail("manager1@gmail.com");
                manager.setHashPassword(passwordEncoder.encode("123456"));
                manager.setFullName("Manager 1");
                manager.setPhone("0900000001");
                manager.setIsActive(true);
                manager.setRole(managerRole);
                manager.setSalary(new BigDecimal("20000000"));
                manager.setHireDate(LocalDateTime.now());
                managerRepository.save(manager);
            }

            if (employessRepository.findByEmail("employee1@gmail.com").isEmpty()) {
                Employee employee = new Employee();
                employee.setEmail("employee1@gmail.com");
                employee.setHashPassword(passwordEncoder.encode("123456"));
                employee.setFullName("Employee 1");
                employee.setPhone("0900000002");
                employee.setIsActive(true);
                employee.setRole(employeeRole);
                employee.setSalary(new BigDecimal("10000000"));
                employee.setHireDate(LocalDateTime.now());
                employessRepository.save(employee);
            }
        };
    }
}
