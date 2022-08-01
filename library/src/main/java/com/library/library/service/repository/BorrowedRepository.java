package com.library.library.service.repository;

import com.library.library.service.model.Borrowed;
import com.library.library.service.model.Library;
import com.library.library.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowedRepository extends JpaRepository<Borrowed, Long> {

    Optional<Borrowed> findBorrowedByUserAndLibrary(User user, Library library);

    boolean existsBorrowedByUser(User user);
}
