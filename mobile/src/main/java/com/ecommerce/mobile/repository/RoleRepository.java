package com.ecommerce.mobile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.mobile.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
