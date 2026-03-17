package com.ecommerce.mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.mobile.entity.Employee;
import java.util.Optional;

@Repository
public interface EmployessRepository extends JpaRepository<Employee, Long>{
    
    Optional<Employee> findByEmail (String email) ;
    Boolean existByEmail(String email);



}


