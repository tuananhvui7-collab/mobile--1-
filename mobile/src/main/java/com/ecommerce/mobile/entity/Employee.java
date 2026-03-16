package com.ecommerce.mobile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("EMPLOYEE")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Employee extends User {
    // chua phat trien, chac la se co ten, luong. 
    @Column(name = "salary")
    private BigDecimal salary;
    
    @Column(name = "hire_date")
    private LocalDateTime hireDate;

}
