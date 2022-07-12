package com.library.library.service.repository;

import com.library.library.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(nativeQuery = true)
    Optional<User> findUserByEmail(String email);

    boolean existsUserByEmail(String email);
}