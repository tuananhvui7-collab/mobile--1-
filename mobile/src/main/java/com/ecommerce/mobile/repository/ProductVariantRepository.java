package com.ecommerce.mobile.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.mobile.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
///implement by codex
    @Query("select v from ProductVariant v where v.product.product_id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") Long productId);

    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByStockQtyLessThanEqual(Integer stockQty);
}
