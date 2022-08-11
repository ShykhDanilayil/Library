package com.library.library.service.repository;

import com.library.library.service.model.Library;
import com.library.library.service.model.Reserved;
import com.library.library.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservedRepository extends JpaRepository<Reserved, Long> {

    Optional<Reserved> findByUserAndLibrary(User user, Library library);

    boolean existsReservedByUser(User user);
}
