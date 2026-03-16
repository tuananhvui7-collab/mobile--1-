package com.ecommerce.mobile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (callSuper = true, exclude = "role")
@Entity
@Table(name = "users")

public class Employee extends User {
    // chua phat trien, chac la se co ten, luong. 
    @Column ( name = "full_name" )
    private String fullName; 
    
    @Column(name = "salary")
    private BigDecimal salary;
    
    @Column(name = "hire_date")
    private LocalDateTime hireDate;

}
