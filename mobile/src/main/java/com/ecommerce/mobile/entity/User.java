package com.ecommerce.mobile.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

@MappedSuperclass



public abstract class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long userID;

    @Column 
    private String username;
    private String hashPassword;

}
