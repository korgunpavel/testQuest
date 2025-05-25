package com.korgun.bankservice.repository;

import com.korgun.bankservice.entity.Role;
import com.korgun.bankservice.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
