package com.ecommerce.mobile.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "role_id")
    private Long roleId;

    @Column (name = "name_role", nullable = false)
    private String nameRole;

    //@OneToMany(mappedBy = "role") private List<User> user;
    // user class giờ là @mappedSuper, abstract.
   

    

}
