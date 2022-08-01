package com.library.library.service.repository;

import com.library.library.service.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    boolean existsAuthorByNickname(String nickname);

    Author findAuthorByNickname(String nickname);
}
