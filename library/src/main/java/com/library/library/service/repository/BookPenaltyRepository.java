package com.library.library.service.repository;

import com.library.library.service.model.BookPenalty;
import com.library.library.service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookPenaltyRepository extends JpaRepository<BookPenalty, Long> {

    int countAllByUser(User user);
}
