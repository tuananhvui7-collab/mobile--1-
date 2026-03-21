package com.ecommerce.mobile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.mobile.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {
    public List<Address> findByCustomerUserID(Long customerId);

}
