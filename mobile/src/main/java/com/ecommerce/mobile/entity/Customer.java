package com.ecommerce.mobile.entity;

import com.ecommerce.mobile.enums.Rank;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@DiscriminatorValue("CUSTOMER")
@Data
@NoArgsConstructor
@AllArgsConstructor



// điểm khác biệt: Chỉ có giá trị này trong cột mới được phép thêm vào bảng users. 
public class Customer extends User {
    @Column (name = "loyalty_point")
    private int loyaltyPoint = 0 ;
    
    @Column (name = "rank_level")
    private Rank rank = Rank.BRONZE;



}
