package com.boot1.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boot1.Entities.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {
    boolean existsByName(String permissionName);

    Optional<Permission> findByName(String permissionName);

    Optional<Permission> findByDescription(String permissionDescription);

    void deletePermissionByName(String name);

    List<Permission> findAllByNameIn(Set<String> permissionName);

    List<Permission> findAllByNameContainingIgnoreCase(String keyword);
}
