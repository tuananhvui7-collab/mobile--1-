package com.ecommerce.mobile.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
 
@Entity
@DiscriminatorValue("CUSTOMER") // Phân loại dựa trên customer)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false) // gọi hàm này ra để xử lý cái equals và hashcode thừa trogn data

// điểm khác biệt: Chỉ có giá trị này trong cột mới được phép thêm vào bảng users. 
public class Customer extends User {

}
