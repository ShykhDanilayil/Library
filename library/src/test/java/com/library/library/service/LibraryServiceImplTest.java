package com.library.library.service;

import com.library.library.controller.dto.BookDto;
import com.library.library.controller.dto.BookStatus;
import com.library.library.controller.dto.LibraryDto;
import com.library.library.controller.dto.Role;
import com.library.library.controller.dto.UserDto;
import com.library.library.service.exception.BookNotAvailableException;
import com.library.library.service.exception.BorrowedException;
import com.library.library.service.exception.EntityNotFoundException;
import com.library.library.service.exception.LibraryException;
import com.library.library.service.exception.ReservedException;
import com.library.library.service.impl.LibraryServiceImpl;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.mapper.LibraryMapper;
import com.library.library.service.mapper.UserMapper;
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
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceImplTest {

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Mock
    private LibraryRepository libraryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservedRepository reservedRepository;
    @Mock
    private BorrowedRepository borrowedRepository;
    @Mock
    private BookPenaltyRepository penaltyRepository;

    private final LibraryDto libraryDto = getLibraryDto();
    private final Library library = getLibrary();
    private final BookDto bookDto = getBookDto();
    private final Book book = getBook();
    private final UserDto userDto = getUserDto();
    private final User user = getUser();

    @Test
    public void createLibraryTest() {
        //given
        when(libraryRepository.save(isA(Library.class))).thenReturn(library);

        //when
        LibraryDto actual = libraryService.createLibrary(libraryDto);

        //then
        assertEquals(libraryDto, actual);
    }

    @Test
    public void updateLibraryNameAndAddressAndPhoneTest() {
        //given
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(libraryRepository.save(isA(Library.class))).thenReturn(library);

        //when
        LibraryDto actual = libraryService.updateLibrary(libraryDto.getName(), libraryDto);

        //then
        assertEquals(libraryDto, actual);
    }

    @Test
    public void updateLibraryTest() {
        LibraryDto updateLibDto = libraryDto;
        updateLibDto.setName(null);
        updateLibDto.setAddress(null);
        updateLibDto.setPhone(null);
        updateLibDto.setEmail("email@test.com");
        updateLibDto.setCity("test");
        updateLibDto.setCountry("test");
        updateLibDto.setCountry("test");
        updateLibDto.setPostalCode("12345");
        Library updateLib = LibraryMapper.INSTANCE.mapLibrary(updateLibDto);
        //given
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(updateLib);
        when(libraryRepository.save(isA(Library.class))).thenReturn(updateLib);

        //when
        LibraryDto actual = libraryService.updateLibrary(libraryDto.getName(), libraryDto);

        //then
        assertEquals(updateLibDto, actual);
    }

    @Test
    void getLibraryTest() {
        //given
        when(libraryRepository.findLibraryByLibraryName(library.getLibraryName())).thenReturn(library);

        //when
        LibraryDto actual = libraryService.getLibrary(library.getLibraryName());

        //then
        assertEquals(libraryDto, actual);
    }


    @Test
    void getPageLibrariesTest() {
        Pageable pageable = PageRequest.of(0, 12);

        List<Library> libraries = Collections.singletonList(library);
        Page<Library> libraryPage = new PageImpl<>(libraries, pageable, libraries.size());
        //given
        when(libraryRepository.findAll(pageable)).thenReturn(libraryPage);

        //when
        Page<LibraryDto> actualPage = libraryService.getPageLibraries(pageable);

        //then
        List<LibraryDto> libraryDtos = Collections.singletonList(libraryDto);
        Page<LibraryDto> libraryDtoPage = new PageImpl<>(libraryDtos, pageable, libraryDtos.size());
        assertEquals(libraryDtoPage, actualPage);
    }

    @Test
    void addBookTest() {
        //given
        when(bookRepository.findBookByTitleAndLibraryIsNull(bookDto.getTitle())).thenReturn(Collections.singletonList(book));
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(libraryRepository.save(library)).thenReturn(library);

        //when
        LibraryDto actual = libraryService.addBook(libraryDto.getName(), bookDto.getTitle());

        //then
        assertEquals(libraryDto, actual);
    }

    @Test
    void addBookExceptionTest() {
        //given
        when(bookRepository.findBookByTitleAndLibraryIsNull(bookDto.getTitle())).thenReturn(Collections.emptyList());
        //when
        assertThrows(BookNotAvailableException.class,
                () -> libraryService.addBook(libraryDto.getName(), bookDto.getTitle()));
        //then
        verify(bookRepository, only()).findBookByTitleAndLibraryIsNull(bookDto.getTitle());
        verify(libraryRepository, never()).findLibraryByLibraryName(any());
        verify(libraryRepository, never()).save(any());
    }

    @Test
    void addUserTest() {
        user.setLibraries(new ArrayList<>());
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);

        //when
        libraryService.addUser(libraryDto.getName(), userDto.getEmail());

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
    }

    @Test
    void addUserLibraryExceptionTest() {
        user.setLibraries(Collections.singletonList(library));
        library.setUsers(Collections.singletonList(user));
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);

        //when
        assertThrows(LibraryException.class,
                () -> libraryService.addUser(libraryDto.getName(), userDto.getEmail()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
    }

    @Test
    void deleteUserTest() {
        user.setLibraries(new ArrayList<>(Collections.singletonList(library)));
        library.setUsers(new ArrayList<>(Collections.singletonList(user)));
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);

        //when
        libraryService.deleteUser(libraryDto.getName(), userDto.getEmail());

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
    }

    @Test
    void deleteUserLibraryExceptionTest() {
        user.setLibraries(new ArrayList<>());
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);

        //when
        assertThrows(LibraryException.class,
                () -> libraryService.deleteUser(libraryDto.getName(), userDto.getEmail()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
    }

    @Test
    void getAllLibrariesByBookTitleTest() {
        book.setLibrary(library);
        //given
        when(bookRepository.findBookByTitleAndLibraryNotNull(bookDto.getTitle())).thenReturn(Collections.singletonList(book));

        //when
        Set<LibraryDto> actual = libraryService.getAllLibrariesByBookTitle(bookDto.getTitle());

        //then
        Set<LibraryDto> expected = new HashSet<>();
        expected.add(LibraryMapper.INSTANCE.mapLibraryDto(book.getLibrary()));
        assertEquals(expected, actual);
    }

    @Test
    void getAllLibrariesByBookTitleEntityNotFoundExceptionTest() {
        //given
        book.setLibrary(null);
        when(bookRepository.findBookByTitleAndLibraryNotNull(bookDto.getTitle())).thenReturn(Collections.singletonList(book));
        //when
        assertThrows(EntityNotFoundException.class,
                () -> libraryService.getAllLibrariesByBookTitle(book.getTitle()));
        //then
        verify(bookRepository, only()).findBookByTitleAndLibraryNotNull(bookDto.getTitle());
    }

    @Test
    void getAllLibrariesByBookTitleEmptyListTest() {
        //given
        book.setLibrary(null);
        when(bookRepository.findBookByTitleAndLibraryNotNull(bookDto.getTitle())).thenReturn(Collections.emptyList());
        //when
        assertThrows(BookNotAvailableException.class,
                () -> libraryService.getAllLibrariesByBookTitle(book.getTitle()));
        //then
        verify(bookRepository, only()).findBookByTitleAndLibraryNotNull(bookDto.getTitle());
    }

    @Test
    void getAllLibrariesByBookTitleReservedTest() {
        //given
        book.setLibrary(library);
        book.setStatus(BookStatus.RESERVED);
        when(bookRepository.findBookByTitleAndLibraryNotNull(bookDto.getTitle())).thenReturn(Collections.singletonList(book));
        //when
        assertThrows(EntityNotFoundException.class,
                () -> libraryService.getAllLibrariesByBookTitle(book.getTitle()));
        //then
        verify(bookRepository, only()).findBookByTitleAndLibraryNotNull(bookDto.getTitle());
        book.setStatus(BookStatus.AVAILABLE);
    }

    @Test
    void getAllBooksTest() {
        //given
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        //when
        Set<BookDto> actual = libraryService.getAllBooks(libraryDto.getName());
        //then
        Set<BookDto> expected = BookMapper.INSTANCE.mapBookDtos(library.getBooks());
        assertEquals(expected, actual);
    }

    @Test
    void deleteLibraryTest() {
        //given
        when(libraryRepository.findLibraryByLibraryName(library.getLibraryName())).thenReturn(library);
        doNothing().when(libraryRepository).delete(library);

        //when
        libraryService.deleteLibrary(library.getLibraryName());

        //then
        verify(libraryRepository, times(1)).findLibraryByLibraryName(library.getLibraryName());
        verify(libraryRepository, times(1)).delete(library);
    }

    @Test
    void isNameAlreadyInUseTest() {
        //given
        when(libraryRepository.existsLibraryByLibraryName(library.getLibraryName())).thenReturn(true);

        //when
        boolean actual = libraryService.isNameAlreadyInUse(library.getLibraryName());

        //then
        assertTrue(actual);
    }

    @Test
    void isEmailAlreadyInUseTest() {
        when(libraryRepository.existsLibrariesByEmail(library.getEmail())).thenReturn(false);
        assertFalse(libraryService.isEmailAlreadyInUse(library.getEmail()));
    }

    @Test
    void reserveBookTest() {
        library.setBooks(Collections.singleton(book));
        user.setIsAccountNonLocked(true);
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(reservedRepository.existsReservedByUser(user)).thenReturn(false);
        when(borrowedRepository.existsBorrowedByUser(user)).thenReturn(false);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(reservedRepository.save(isA(Reserved.class))).thenReturn(new Reserved());

        //when
        libraryService.reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(reservedRepository, times(1)).existsReservedByUser(user);
        verify(borrowedRepository, times(1)).existsBorrowedByUser(user);
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(reservedRepository, times(1)).save(isA(Reserved.class));
    }

    @Test
    void reserveBookNotAvailableExceptionTest() {
        book.setStatus(BookStatus.RESERVED);
        library.setBooks(Collections.singleton(book));
        user.setIsAccountNonLocked(true);
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(reservedRepository.existsReservedByUser(user)).thenReturn(false);
        when(borrowedRepository.existsBorrowedByUser(user)).thenReturn(false);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);

        //when
        assertThrows(BookNotAvailableException.class,
                () -> libraryService.reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(reservedRepository, times(1)).existsReservedByUser(user);
        verify(borrowedRepository, times(1)).existsBorrowedByUser(user);
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(reservedRepository, never()).save(any());
    }

    @Test
    void reserveBookReservedExceptionTest() {
        book.setStatus(BookStatus.RESERVED);
        library.setBooks(Collections.singleton(book));
        user.setIsAccountNonLocked(true);
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(reservedRepository.existsReservedByUser(user)).thenReturn(true);

        //when
        assertThrows(ReservedException.class,
                () -> libraryService.reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(reservedRepository, times(1)).existsReservedByUser(user);
        verify(borrowedRepository, never()).existsBorrowedByUser(any());
        verify(libraryRepository, never()).findLibraryByLibraryName(any());
        verify(reservedRepository, never()).save(any());
    }

    @Test
    void reserveBookExceptionTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(reservedRepository.existsReservedByUser(user)).thenReturn(false);
        when(borrowedRepository.existsBorrowedByUser(user)).thenReturn(true);

        //when
        assertThrows(ReservedException.class,
                () -> libraryService.reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(reservedRepository, times(1)).existsReservedByUser(user);
        verify(borrowedRepository, times(1)).existsBorrowedByUser(user);
        verify(libraryRepository, never()).findLibraryByLibraryName(any());
        verify(reservedRepository, never()).save(any());
    }

    @Test
    void reserveBookNotEqualsTitleTest34() {
        Book newBook = book;
        newBook.setTitle("new Title");
        library.setBooks(Collections.singleton(newBook));
        user.setIsAccountNonLocked(true);
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(reservedRepository.existsReservedByUser(user)).thenReturn(false);
        when(borrowedRepository.existsBorrowedByUser(user)).thenReturn(false);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);

        //when
        assertThrows(BookNotAvailableException.class,
                () -> libraryService.reserveBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName()));

        //then
        verify(userRepository).findUserByEmail(userDto.getEmail());
        verify(reservedRepository, times(1)).existsReservedByUser(user);
        verify(borrowedRepository).existsBorrowedByUser(user);
        verify(libraryRepository).findLibraryByLibraryName(libraryDto.getName());
        verify(reservedRepository, never()).save(isA(Reserved.class));
    }

    @Test
    void borrowBookTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(reservedRepository.findByUserAndLibrary(user, library)).thenReturn(Optional.of(getReserved()));
        doNothing().when(reservedRepository).delete(isA(Reserved.class));
        when(borrowedRepository.save(isA(Borrowed.class))).thenReturn(new Borrowed());

        //when
        libraryService.borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName());

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(reservedRepository, times(1)).findByUserAndLibrary(user, library);
        verify(reservedRepository, times(1)).delete(isA(Reserved.class));
        verify(borrowedRepository, times(1)).save(isA(Borrowed.class));
    }

    @Test
    void borrowBookReservedExceptionTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(reservedRepository.findByUserAndLibrary(user, library)).thenReturn(Optional.empty());

        //when
        assertThrows(ReservedException.class,
                () -> libraryService.borrowBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(reservedRepository, times(1)).findByUserAndLibrary(user, library);
        verify(reservedRepository, never()).delete(isA(Reserved.class));
        verify(borrowedRepository, never()).save(isA(Borrowed.class));
    }

    @Test
    void returnBookTest() throws Exception {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(borrowedRepository.findBorrowedByUserAndLibrary(user, library)).thenReturn(Optional.of(getBorrowed()));
        doNothing().when(borrowedRepository).delete(isA(Borrowed.class));

        //when
        libraryService.returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(borrowedRepository, times(1)).findBorrowedByUserAndLibrary(user, library);
        verify(borrowedRepository, times(1)).delete(isA(Borrowed.class));
        verify(penaltyRepository, never()).save(any());
    }

    @Test
    void returnBookPenaltyTest() throws Exception {
        Borrowed borrowed = getBorrowed();
        borrowed.setDueDate(Calendar.getInstance().getTime());
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(borrowedRepository.findBorrowedByUserAndLibrary(user, library)).thenReturn(Optional.of(borrowed));
        doNothing().when(borrowedRepository).delete(borrowed);
        when(penaltyRepository.countAllByUser(user)).thenReturn(4);
        when(penaltyRepository.save(isA(BookPenalty.class))).thenReturn(new BookPenalty());

        //when
        libraryService.returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(borrowedRepository, times(1)).findBorrowedByUserAndLibrary(user, library);
        verify(borrowedRepository, times(1)).delete(borrowed);
        verify(penaltyRepository, times(1)).save(isA(BookPenalty.class));
    }

    @Test
    void returnBookSetBlockTest() {
        Borrowed borrowed = getBorrowed();
        borrowed.setDueDate(Calendar.getInstance().getTime());
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(borrowedRepository.findBorrowedByUserAndLibrary(user, library)).thenReturn(Optional.of(borrowed));
        doNothing().when(borrowedRepository).delete(borrowed);
        when(penaltyRepository.countAllByUser(user)).thenReturn(6);
        when(penaltyRepository.save(isA(BookPenalty.class))).thenReturn(new BookPenalty());

        //when
        libraryService.returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), new Request(new Connector()));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(borrowedRepository, times(1)).findBorrowedByUserAndLibrary(user, library);
        verify(borrowedRepository, times(1)).delete(borrowed);
        verify(penaltyRepository, times(1)).save(isA(BookPenalty.class));
    }

    @Test
    void returnBookSetBlockNullRequestTest() {
        Borrowed borrowed = getBorrowed();
        borrowed.setDueDate(Calendar.getInstance().getTime());
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(borrowedRepository.findBorrowedByUserAndLibrary(user, library)).thenReturn(Optional.of(borrowed));
        doNothing().when(borrowedRepository).delete(borrowed);
        when(penaltyRepository.countAllByUser(user)).thenReturn(6);
        when(penaltyRepository.save(isA(BookPenalty.class))).thenReturn(new BookPenalty());

        //when
        libraryService.returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null);

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(borrowedRepository, times(1)).findBorrowedByUserAndLibrary(user, library);
        verify(borrowedRepository, times(1)).delete(borrowed);
        verify(penaltyRepository, times(1)).save(isA(BookPenalty.class));
    }

    @Test
    void returnBookBorrowedExceptionTest() {
        //given
        when(userRepository.findUserByEmail(userDto.getEmail())).thenReturn(user);
        when(libraryRepository.findLibraryByLibraryName(libraryDto.getName())).thenReturn(library);
        when(borrowedRepository.findBorrowedByUserAndLibrary(user, library)).thenReturn(Optional.empty());

        //when
        assertThrows(BorrowedException.class,
                () -> libraryService.returnBook(bookDto.getTitle(), userDto.getEmail(), libraryDto.getName(), null));

        //then
        verify(userRepository, times(1)).findUserByEmail(userDto.getEmail());
        verify(libraryRepository, times(1)).findLibraryByLibraryName(libraryDto.getName());
        verify(borrowedRepository, times(1)).findBorrowedByUserAndLibrary(user, library);
        verify(borrowedRepository, never()).delete(any());
        verify(penaltyRepository, never()).save(any());
    }

    private LibraryDto getLibraryDto() {
        return LibraryDto.builder()
                .name("TEST LIB")
                .address("LVIV")
                .phone("0934343436")
                .build();
    }

    private Library getLibrary() {
        Library newLib = LibraryMapper.INSTANCE.mapLibrary(libraryDto);
        return newLib;
    }

    private BookDto getBookDto() {
        return BookDto.builder()
                .title("TEST TITLE")
                .description("TEST DESCRIPTION")
                .status(BookStatus.AVAILABLE)
                .build();
    }

    private Book getBook() {
        Book book = BookMapper.INSTANCE.mapBook(bookDto);
        return book;
    }

    private UserDto getUserDto() {
        return UserDto.builder()
                .firstName("Petro")
                .lastName("Smikh")
                .email("string@test.com")
                .role(Role.USER)
                .isAccountNonLocked(true)
                .postalCode("12345")
                .build();
    }

    private User getUser() {
        return UserMapper.INSTANCE.mapUser(userDto);
    }

    private Reserved getReserved() {
        return Reserved.builder()
                .book(book)
                .user(user)
                .library(library)
                .build();
    }

    private Borrowed getBorrowed() {
        return Borrowed.builder()
                .dueDate(expiration().getTime())
                .book(book)
                .user(user)
                .library(library)
                .build();
    }

    private Calendar expiration() {
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, 2);
        return calendar;
    }
}
