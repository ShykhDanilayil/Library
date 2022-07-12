package com.library.library.service.mapper;

import com.library.library.controller.dto.AuthorDto;
import com.library.library.service.model.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthorMapper {

    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    @Mapping(target = "name", source = "authorName")
    AuthorDto mapAuthorDto(Author author);

    @Mapping(target = "authorName", source = "name")
    Author mapAuthor(AuthorDto authorDto);
}
