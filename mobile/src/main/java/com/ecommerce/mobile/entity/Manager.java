package com.ecommerce.mobile.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

@Data
@EqualsAndHashCode (callSuper = true, exclude = "role")
@Entity
@Table(name = "users")

public class Manager extends Employee {
    // chua phat trien, chac la se co ten, luong. 
    // manager cung la nhan vien nhung co gi do khac nv kho va don thong thuong.
    // dung de phat trien du an etl.
    

}
