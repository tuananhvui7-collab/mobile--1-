package com.ecommerce.mobile.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 1 bảng duy nhất
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)


public abstract class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "user_id")
    private Long userID;

    @Column (name = "email", nullable = false, unique = true, length =  100)
    private String email;

    @Column (name = "password_hash", nullable = false)
    private String hashPassword;

    @Column (name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column (name = "phone", length = 15 )
    private String phone;

    @Column (name = "is_active")
    private Boolean isActive;

    @Column (name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "role_id")
    private Role role;

    @PrePersist // annotation này dùng cho hàm,
    // tự chạy trước khi insert vào DB
    // Dùng để tự gán thời gian tạo tài khoản trước khi insert vào DB. (lấy thời gian hiện tại ấy mà)

    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
