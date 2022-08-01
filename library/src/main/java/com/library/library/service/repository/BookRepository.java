package com.library.library.service.repository;

import com.library.library.service.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book findDistinctFirstByTitle(String bookTitle);

    List<Book> findBookByTitleAndLibraryIsNull(String bookTitle);

    List<Book> findBookByTitleAndLibraryNotNull(String bookTitle);

    boolean existsBookByTitle(String title);
}

