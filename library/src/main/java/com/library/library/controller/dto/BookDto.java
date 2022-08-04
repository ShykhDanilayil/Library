package com.library.library.controller.dto;

import com.library.library.controller.validation.DescriptionValid;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;


@Data
@Builder
public class BookDto {

    @NotEmpty(message = "Book title may not be empty")
    private String title;
    @DescriptionValid
    @NotEmpty(message = "Book description may not be empty")
    private String description;
    @Positive(message = "Book pages must be than 0")
    @NotNull(message = "Book pages may not be null")
    private int pages;
    @Positive(message = "Publication year must be than 0")
    private int publicationYear;
    private Genre genre;
    private BookStatus status;
}
