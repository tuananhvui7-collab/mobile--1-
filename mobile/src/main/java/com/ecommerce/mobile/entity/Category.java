package com.ecommerce.mobile.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    /// category_id,name,slug is_active parent_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column (name = "name")
    private String name;
    @Column (name = "slug")
    private String slug;
    @Column (name = "is_active")
    private Boolean isActive;
    
    @ManyToOne
    @JoinColumn (name = "parent_id") // câu hỏi thằng này có thằng cha là gì?
    private Category parent;

    @OneToMany(mappedBy = "category")
    private List<Product> product; // quan hệ 1 nhiều.

    @OneToMany(mappedBy = "parent") // thêm code để trả lời câu hỏi thằng cha có thằng con nào
    private List<Category> children;
}
