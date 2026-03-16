package com.ecommerce.mobile.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Data
@NoArgsConstructor
@DiscriminatorValue("MANAGER")

public class Manager extends Employee {
	
}
