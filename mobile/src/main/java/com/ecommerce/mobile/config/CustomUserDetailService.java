package com.ecommerce.mobile.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.mobile.entity.User;
import com.ecommerce.mobile.repository.UserRepository;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository; // dùng userRepository để tìm email cho tất cả user.

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
       // tìm user theo email.
        User user = userRepository.findByEmail(email).
        orElseThrow(
            () -> new UsernameNotFoundException("Không tìm thấy tài khoản" + email) 
        );
         // kiểm tra xem tài khoản có bị khóa không
         if (user.getIsActive() == null || !user.getIsActive()){
            throw new UsernameNotFoundException("Tài khoản đã bị khóa !");
         }

         // SPRING SECURITY BẮT CÓ TIỀN TỐ ROLE_ TROGN TÊN QUYỀN
         // security viết hasRole("CUSTOMER") -> SPRING KIỂM TRA "ROLE_CUSTOMER"
         GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getNameRole());



        // Đóng gói lại mang cho spring security config xem. 
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getHashPassword(), 
            List.of(authority) // Danh sách quyền
        );
}
    
    
}
