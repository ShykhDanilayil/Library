package com.library.library.service.mapper;

import com.library.library.controller.dto.LibraryDto;
import com.library.library.service.model.Library;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper
public interface LibraryMapper {
    LibraryMapper INSTANCE = Mappers.getMapper(LibraryMapper.class);

    @Mapping(target = "name", source = "libraryName")
    LibraryDto mapLibraryDto(Library library);

    @Mapping(target = "libraryName", source = "name")
    Library mapLibrary(LibraryDto libraryDto);

    @Mapping(target = "name", source = "libraryName")
    Set<LibraryDto> mapLibraryDtos(Set<Library> libraries);

    @Mapping(target = "name", source = "libraryName")
    List<LibraryDto> mapPageLibraryDto(List<Library> libraries);
}
