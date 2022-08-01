package com.library.library.service.repository;

import com.library.library.service.model.BookPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookPenaltyRepository extends JpaRepository<BookPenalty, Long> {
}
