package com.library.library.service.impl;

import com.library.library.controller.dto.LibraryDto;
import com.library.library.service.LibraryService;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.LibraryAlreadyExistsException;
import com.library.library.service.mapper.LibraryMapper;
import com.library.library.service.model.Book;
import com.library.library.service.model.Library;
import com.library.library.service.repository.BookRepository;
import com.library.library.service.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepo;
    private final BookRepository bookRepo;

    @Override
    @Transactional
    public LibraryDto createLibrary(LibraryDto newLibrary) throws LibraryAlreadyExistsException {
        log.info("create Library with adress {}", newLibrary.getAddress());
        Library library = LibraryMapper.INSTANCE.mapLibrary(newLibrary);
        if (libraryRepo.existsByAddress(library.getAddress())) {
            throw new LibraryAlreadyExistsException(format("Library with name %s exists", library.getLibraryName()));
        }
        library = libraryRepo.save(library);
//        log.info("Library with name {} successfully created", library.getLibraryName());
        return LibraryMapper.INSTANCE.mapLibraryDto(library);
    }

    @Override
    public LibraryDto getLibrary(String address) {
        log.info("get library with address {}", address);
        Library library = libraryRepo.findByAddress(address).orElseThrow(() ->
                new EntityNotFoundException(format("Library with adress %s is not found", address)));
        log.info("Library with address {} successfully searched", address);
        return LibraryMapper.INSTANCE.mapLibraryDto(library);
    }

    @Override
    public List<LibraryDto> getAllLibraries() {
        log.info("get all library");
        Page<Library> allLibrary = libraryRepo.findAll(PageRequest.of(0, 12, Sort.by("libraryName").and(Sort.by("address"))));
        return LibraryMapper.INSTANCE.mapPageLibraryDto(allLibrary.getContent());
    }

    @Override
    @Transactional
    public void deleteLibrary(String address) {
        log.info("deleteUser with address {}", address);
        Library library = libraryRepo.findByAddress(address).orElseThrow(() ->
                new EntityNotFoundException(format("Library with adress %s is not found", address)));
        libraryRepo.delete(library);
        log.info("Library with address {} successfully deleted", address);
    }

    @Override
    @Transactional
    public Set<LibraryDto> getLibraryByBookTitle(String bookTitle) {
        log.info("get Library by book title {}", bookTitle);
        Book book = bookRepo.getByTitle(bookTitle).orElseThrow(() ->
                new EntityNotFoundException(format("The book with this title {} doesn't exist {}", bookTitle)));
        Set<Library> libraries = libraryRepo.getLibraryByBooks(book).orElseThrow(() ->
                new EntityNotFoundException(format("The book with this title {} doesn't exist in any library", bookTitle)));
        return LibraryMapper.INSTANCE.mapLibraryDtos(libraries);
    }

    @Override
    @Transactional
    public LibraryDto addBook(String libraryName, String bookTitle) {
        Library library = libraryRepo.getLibraryByLibraryName(libraryName).orElseThrow(() ->
                new EntityNotFoundException(format("The book with this title {} doesn't exist {}", bookTitle)));
        Book book = bookRepo.getByTitle(bookTitle).orElseThrow(() ->
                new EntityNotFoundException(format("The book with this title {} doesn't exist {}", bookTitle)));
        library.getBooks().add(book);
        book.getLibraries().add(library);
        libraryRepo.save(library);
        bookRepo.save(book);
        log.info("library {} add book {}", library.getLibraryName(), book.getTitle());
        return LibraryMapper.INSTANCE.mapLibraryDto(library);
    }
}
