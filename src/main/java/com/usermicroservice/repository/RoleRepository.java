package com.usermicroservice.repository;

import com.usermicroservice.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by dani on 2017-02-22.
 */
@Repository
public interface RoleRepository  extends JpaRepository<Role, Integer> {
    Role findByRole(String role);
}

