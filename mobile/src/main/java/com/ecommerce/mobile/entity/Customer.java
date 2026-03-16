package com.ecommerce.mobile.entity;

import com.ecommerce.mobile.enums.Rank;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode (callSuper = true, exclude = "role") // so sánh đối tượng, bỏ cột role đi. 
@Entity
@Table (name = "users")

// điểm khác biệt: Chỉ có giá trị này trong cột mới được phép thêm vào bảng users. 
public class Customer extends User {
    @Column (name = "loyalty_point")
    private int loyaltyPoint = 0 ;
    
    @Column (name = "rank_level")
    private Rank rank = Rank.BRONZE;


    // constructor
    public Customer(){
        super(); // Gọi constructor của user cha. (Cái này quên rồi)
    }
}
