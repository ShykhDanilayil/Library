package com.library.library.service.repository;

import com.library.library.service.model.Book;
import com.library.library.service.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Long> {
    Optional<Library> getLibraryByLibraryName(String nameLibrary);

    Optional<Set<Library>> getLibraryByBooks(Book book);

    Optional<Library> findByAddress(String address);

    boolean existsByAddress(String address);
}
