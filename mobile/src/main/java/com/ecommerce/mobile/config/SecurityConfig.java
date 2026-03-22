package com.ecommerce.mobile.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration: day la class cau hinh, Spring doc khi khoi dong
@Configuration
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

        // thêm injection class vào.


    // @Bean: tao 1 object BCryptPasswordEncoder va dang ky voi Spring
    // Object nay duoc inject vao CustomerService de ma hoa password
    // BCrypt: tu dong them "salt" ngau nhien --> rat an toan
    // Cung 1 password ma hoa 2 lan se ra 2 chuoi khac nhau
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // SecurityFilterChain: chuoi cac rule bao mat
    // Spring doc cau hinh nay de biet phan quyen nhu the nao
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ===== PHAN QUYEN URL =====
            .authorizeHttpRequests(auth -> auth

                // CONG KHAI -- ai cung vao duoc, khong can dang nhap
                .requestMatchers(
                    "/",               // Trang chu
                    "/products/**",    // Xem san pham (U4)
                    "/login",          // Trang dang nhap (U1)
                    "/register",       // Trang dang ky (U3)
                    "/css/**",         // File CSS
                    "/js/**",          // File JavaScript
                    "/images/**",      // Anh tinh
                    "/api/public/**"   // REST API cong khai
                ).permitAll()

                // CHI ADMIN (Manager co role ADMIN)
                .requestMatchers(
                    "/admin/**",       // Trang quan tri (U13, U14)
                    "/api/admin/**"    // API admin
                ).hasRole("ADMIN")

                // CHI EMPLOYEE
                .requestMatchers(
                    "/employee/**",    // Trang nhan vien (U12, U15)
                    "/api/employee/**"
                ).hasRole("EMPLOYEE")

                // CHI CUSTOMER da dang nhap
                .requestMatchers(
                    "/cart/**",        // Gio hang (U5)
                    "/orders/**",      // Don hang (U6, U8, U9)
                    "/profile/**",     // Thong tin ca nhan (U11)
                    "/api/customer/**"
                ).hasRole("CUSTOMER")

                // TAT CA TRANG CON LAI: phai dang nhap
                .anyRequest().authenticated()
            )

            // ===== CAU HINH FORM DANG NHAP =====
            .formLogin(form -> form
                .loginPage("/login")          // URL trang login tuy chinh
                .loginProcessingUrl("/login") // URL xu ly POST form
                .defaultSuccessUrl("/")       // Dang nhap thanh cong -> trang chu
                .failureUrl("/login?error")   // Sai mat khau -> them ?error
                .permitAll()                  // Ai cung vao duoc trang login
            )

            // ===== CAU HINH DANG XUAT =====
            .logout(logout -> logout
                .logoutUrl("/logout")              // URL goi de logout
                .logoutSuccessUrl("/login?logout") // Logout xong -> trang login
                .invalidateHttpSession(true)       // Xoa session
                .deleteCookies("JSESSIONID")       // Xoa cookie
                .permitAll()
            );

        return http.build();
    }
}
