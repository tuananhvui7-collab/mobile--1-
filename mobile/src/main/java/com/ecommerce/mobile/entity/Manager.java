package com.ecommerce.mobile.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = "role")
@Entity
public class Manager extends Employee {
	
}
