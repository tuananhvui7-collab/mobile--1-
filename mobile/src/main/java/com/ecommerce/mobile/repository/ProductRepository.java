package com.ecommerce.mobile.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.mobile.entity.Product;
import com.ecommerce.mobile.enums.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("""
            select p
            from Product p
            where p.status = :status
              and (
                  lower(p.name) like lower(concat('%', :keyword, '%'))
                  or lower(p.brand) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Product> searchByStatusAndKeyword(
            @Param("status") ProductStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(
            String name, String brand, Pageable pageable);

    List<Product> findByCategoryCategoryIdAndStatus(Long categoryId, ProductStatus status);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByBrandAndStatus(String brand, ProductStatus status);

    List<Product> findByCategoryCategoryId(Long categoryId);
}
