package com.ecommerce.mobile.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    // authorizeHttpRequests(...) → phân quyền theo URL.


    @Bean // để thằng Spring IOC quản lý và 
    // DI vào các method.
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http .authorizeHttpRequests(auth -> auth
            // Trang công khai
            .requestMatchers(
            "/",
             "/products/**",
              "/login" ,
               "/register",
                "/css/**",
                 "/js/**",
                  "/images/**", 
                  "/api/public/**").permitAll() // các đường dẫn public , ai cũng vào dc

            .requestMatchers(
                "/manager/**" , 
            "/api/admin/**" ).hasRole("MANAGER")
            .requestMatchers(
                "/cart/**",
                "/order/**",
                "/profile/**",
                "/api/customer/**").hasRole("CUSTOMER")

            .requestMatchers(
                "/employee/**",
                "/api/employee/**").hasRole("EMPLOYEE")


    // tẤT CẢ CÁC TRAMH CÒN LẠI PHẢI ĐĂNG NHẬP MỚI VÀO. authenticated()


    // aI CHƯA QUÂN SỰ LÊN THẦY DẠY
    .formLogin(form -> form)
    .loginPage("/login"),
    .loginProcessingUrl
            .anyRequest().authenticated()
            );

        

        return http.build();
    }


    // 

}
