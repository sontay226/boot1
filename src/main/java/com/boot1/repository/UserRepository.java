package com.boot1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boot1.Entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByFirstNameContaining(String firstName);

    List<User> findByLastNameContaining(String lastName);

    List<User> findByFirstNameAndLastName(String firstName, String lastName);
}
