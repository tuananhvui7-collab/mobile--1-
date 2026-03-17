package com.ecommerce.mobile.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
 
@Entity
@DiscriminatorValue("CUSTOMER")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)

// điểm khác biệt: Chỉ có giá trị này trong cột mới được phép thêm vào bảng users. 
public class Customer extends User {

}
