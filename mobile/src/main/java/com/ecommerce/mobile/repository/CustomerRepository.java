package com.ecommerce.mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.mobile.entity.Customer;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{
    
    Optional<Customer> findByEmail (String email) ;
    Boolean existByEmail(String email);
}
