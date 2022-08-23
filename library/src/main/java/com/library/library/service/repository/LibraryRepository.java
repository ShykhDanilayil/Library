package com.library.library.service.repository;

import com.library.library.service.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Long> {

    Library findLibraryByLibraryName(String libraryName);

    boolean existsLibraryByLibraryName(String libraryName);

    boolean existsLibrariesByEmail(String email);

}
