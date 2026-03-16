package com.ecommerce.mobile.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

@Data
@EqualsAndHashCode (callSuper = true, exclude = "role")
@Entity
@Table(name = "users")

public class Employee extends User {
    // chua phat trien, chac la se co ten, luong. 

}
