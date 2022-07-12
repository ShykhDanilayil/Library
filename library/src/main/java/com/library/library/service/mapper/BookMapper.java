package com.library.library.service.mapper;

import com.library.library.controller.dto.BookDto;
import com.library.library.service.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface BookMapper {

    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    Set<BookDto> mapBookDtos(Set<Book> books);

    BookDto mapBookDto(Book books);

    Book mapBook(BookDto dto);
}
