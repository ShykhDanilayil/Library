package com.library.library.service.impl;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.controller.dto.BookDto;
import com.library.library.service.AuthorService;
import com.library.library.service.mapper.AuthorMapper;
import com.library.library.service.mapper.BookMapper;
import com.library.library.service.model.Author;
import com.library.library.service.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepo;

    @Override
    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        Author newAuthor = authorRepo.save(AuthorMapper.INSTANCE.mapAuthor(authorDto));
        log.info("Author with nickname {} successfully created", authorDto.getNickname());
        return AuthorMapper.INSTANCE.mapAuthorDto(newAuthor);
    }

    @Override
    public Page<AuthorDto> getAllAuthors(Pageable pageable) {
        log.info("Get page authors");
        return authorRepo.findAll(pageable).map(this::mapAuthorDto);
    }

    @Override
    public Set<BookDto> getAuthorBooks(String nickname) {
        log.info("Get all books by author {}", nickname);
        Author author = authorRepo.findAuthorByNickname(nickname);
        return BookMapper.INSTANCE.mapBookDtos(author.getBooks());
    }

    @Override
    public boolean isNicknameAlreadyInUse(String nickname) {
        log.info("Checking nickname {}", nickname);
        return authorRepo.existsAuthorByNickname(nickname);
    }

    private AuthorDto mapAuthorDto(Author author) {
        return AuthorDto.builder()
                .name(author.getAuthorName())
                .nickname(author.getNickname())
                .build();
    }
}
