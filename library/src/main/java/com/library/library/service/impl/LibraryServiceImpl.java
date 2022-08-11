package com.library.library.service.impl;

import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.service.LibraryService;
import com.library.library.service.exception.BookNotAvailableException;
import com.library.library.service.exception.BorrowedException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.ReservedException;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.mapper.LibraryMapper;
import com.library.library.service.model.Book;
import com.library.library.service.model.BookPenalty;
import com.library.library.service.model.Borrowed;
import com.library.library.service.model.Library;
import com.library.library.service.model.Reserved;
import com.library.library.service.model.User;
import com.library.library.service.repository.BookPenaltyRepository;
import com.library.library.service.repository.BookRepository;
import com.library.library.service.repository.BorrowedRepository;
import com.library.library.service.repository.LibraryRepository;
import com.library.library.service.repository.ReservedRepository;
import com.library.library.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final LibraryRepository libraryRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final ReservedRepository reservedRepo;
    private final BorrowedRepository borrowedRepo;
    private final BookPenaltyRepository penaltyRepo;

    private final int RESERVED_EXPIRATION_DAYS = 3;
    private final int BORROWED_EXPIRATION_DAYS = 10;

    @Override
    @Transactional
    public LibraryDto createLibrary(LibraryDto newLibrary) {
        log.info("create Library with name {}", newLibrary.getName());
        Library library = LibraryMapper.INSTANCE.mapLibrary(newLibrary);
        library.setWrittenOn(Instant.now());
        libraryRepo.save(library);
        log.info("Library with name {} successfully created", library.getLibraryName());
        return LibraryMapper.INSTANCE.mapLibraryDto(library);
    }

    @Override
    public LibraryDto getLibrary(String libraryName) {
        log.info("Get library with name {}", libraryName);
        return LibraryMapper.INSTANCE.mapLibraryDto(libraryRepo.findLibraryByLibraryName(libraryName));
    }

    @Override
    public Page<LibraryDto> getPageLibraries(Pageable pageable) {
        log.info("Get page libraries");
        return libraryRepo.findAll(pageable).map(this::mapLibraryDto);
    }

    @Override
    @Transactional
    public LibraryDto addBook(String libraryName, String bookTitle) {
        Book book = bookRepo.findBookByTitleAndLibraryIsNull(bookTitle).stream().findFirst().orElseThrow(() ->
                new BookNotAvailableException(format("The book with this title %s isn't available", bookTitle)));
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        book.setLibrary(library);
        libraryRepo.save(library);
        library.getBooks().add(book);
        log.info("library {} successfully added book {}", library.getLibraryName(), book.getTitle());
        return LibraryMapper.INSTANCE.mapLibraryDto(library);
    }

    @Override
    @Transactional
    public void addUser(String libraryName, String email) {
        log.info("Library with name {} add user with email {}", libraryName, email);
        User user = userRepo.findUserByEmail(email);
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        user.getLibraries().add(library);
        library.getUsers().add(user);
        log.info("User with email {} successfully added library with name {}", email, libraryName);
    }

    @Override
    @Transactional
    public Set<LibraryDto> getAllLibrariesByBookTitle(String bookTitle) {
        log.info("Get all Libraries by book title {}", bookTitle);
        List<Book> books = bookRepo.findBookByTitleAndLibraryNotNull(bookTitle);
        if (books.isEmpty()) {
            log.error("The book with this title {} isn't available", bookTitle);
            throw new BookNotAvailableException(format("The book with this title %s isn't available", bookTitle));
        }
        Set<Library> libraries = new HashSet<>();
        log.info("Search for available books by title {} in all libraries", bookTitle);
        for (Book book :
                books) {
            if (book.getStatus().equals(BookStatus.AVAILABLE) && Objects.nonNull(book.getLibrary())) {
                libraries.add(book.getLibrary());
            }
        }
        if (libraries.isEmpty()) {
            log.error("Available book with this title {} doesn't exist in any library", bookTitle);
            throw new EntityNotFoundException(format("Available book with this title %s doesn't exist in any library", bookTitle));
        }
        return LibraryMapper.INSTANCE.mapLibraryDtos(libraries);
    }

    @Override
    public Set<BookDto> getAllBooks(String libraryName) {
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        return BookMapper.INSTANCE.mapBookDtos(library.getBooks());
    }

    @Override
    @Transactional
    public void deleteLibrary(String libraryName) {
        log.info("Delete user with address {}", libraryName);
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        libraryRepo.delete(library);
        log.info("Library with name {} successfully deleted", libraryName);
    }

    @Override
    public boolean isNameAlreadyInUse(String name) {
        log.info("Checking name {}", name);
        return libraryRepo.existsLibraryByLibraryName(name);
    }

    @Override
    public boolean isEmailAlreadyInUse(String email) {
        log.info("Checking email {}", email);
        return libraryRepo.existsLibrariesByEmail(email);
    }

    @Override
    @Transactional
    public void reserveBook(String bookTitle, String userEmail, String libraryName) {
        log.info("User with email {} reserve book with title {} in library {}", userEmail, bookTitle, libraryName);
        User user = userRepo.findUserByEmail(userEmail);
        if (reservedRepo.existsReservedByUser(user)) {
            log.error("User with email {} already had a book reserved", userEmail);
            throw new ReservedException(format("User with email %s already had a book reserved", userEmail));
        }
        if (borrowedRepo.existsBorrowedByUser(user)) {
            log.error("User with email {} didn't return the last book", userEmail);
            throw new ReservedException(format("User with email %s didn't return the last book", userEmail));
        }
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        Book book = library.getBooks().stream().filter(b -> b.getTitle().equals(bookTitle) && b.getStatus().equals(BookStatus.AVAILABLE))
                .findFirst().orElseThrow(() -> new BookNotAvailableException(format("Available book with this title %s doesn't exist in library with this name %s", bookTitle, libraryName)));
        book.setStatus(BookStatus.RESERVED);
        reservedRepo.save(new Reserved(expiration(RESERVED_EXPIRATION_DAYS).getTime(), book, user, library));
        log.info("Book with title {} successfully reserved for {} days", bookTitle, RESERVED_EXPIRATION_DAYS);
    }

    @Override
    @Transactional
    public void borrowBook(String bookTitle, String userEmail, String libraryName) {
        log.info("User with email {} borrow book with title {} in library {}", userEmail, bookTitle, libraryName);
        User user = userRepo.findUserByEmail(userEmail);
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        Reserved reserved = reservedRepo.findByUserAndLibrary(user, library).orElseThrow(() ->
                new ReservedException(format("Reserved with book %s is not found", bookTitle)));
        Book book = reserved.getBook();
        book.setStatus(BookStatus.BORROWED);
        reservedRepo.delete(reserved);
        borrowedRepo.save(new Borrowed(expiration(BORROWED_EXPIRATION_DAYS).getTime(), book, user, library));
        log.info("Book with title {} successfully borrowed for {} days", bookTitle, BORROWED_EXPIRATION_DAYS);
    }

    @Override
    public void returnBook(String bookTitle, String userEmail, String libraryName, HttpServletRequest request) {
        returnBooktoLib(bookTitle, userEmail, libraryName, request);
    }

    private void returnBooktoLib(String bookTitle, String userEmail, String libraryName, HttpServletRequest request) {
        log.info("User with email {} return book with title {} in library {}", userEmail, bookTitle, libraryName);
        User user = userRepo.findUserByEmail(userEmail);
        Library library = libraryRepo.findLibraryByLibraryName(libraryName);
        Borrowed borrowed = borrowedRepo.findBorrowedByUserAndLibrary(user, library).orElseThrow(() ->
                new BorrowedException(format("Borrowed with book %s is not found", bookTitle)));
        Book book = borrowed.getBook();
        book.setStatus(BookStatus.AVAILABLE);
        borrowedRepo.delete(borrowed);
        log.info("Book with title {} successfully returned to library {}", bookTitle, libraryName);

        addUserPenalty(borrowed.getDueDate(), book, library, user, request);
    }

    private void addUserPenalty(Date dueDate, Book book, Library library, User user, HttpServletRequest request) {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        if (currentDate.after(dueDate)) {
            BookPenalty penalty = new BookPenalty(dueDate, currentDate, book, library, user);
            penaltyRepo.save(penalty);
            if (penaltyRepo.countAllByUser(user) >= 5) {
                user.setIsAccountNonLocked(false);
                userRepo.save(user);
                if (Objects.nonNull(request)) {
                    new SecurityContextLogoutHandler().logout(request, null, null);
                }
                log.info("User with email {} has been locked account", user.getEmail());
            }
            log.info("User with email {} gets fined", user.getEmail());
        }
    }

    private Calendar expiration(int dueDay) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, dueDay);
        return cal;
    }

    private LibraryDto mapLibraryDto(Library library) {
        return LibraryDto.builder()
                .name(library.getLibraryName())
                .email(library.getEmail())
                .phone(library.getPhone())
                .country(library.getCountry())
                .city(library.getCity())
                .address(library.getAddress())
                .postalCode(library.getPostalCode())
                .build();
    }
}
