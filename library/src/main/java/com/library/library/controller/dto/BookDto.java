package com.library.library.controller.dto;

import com.library.library.controller.validation.DescriptionValid;
import com.library.library.controller.validation.PatchGroup;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.groups.Default;


@Data
@Builder
public class BookDto {

    @NotEmpty(message = "Book title may not be empty")
    private String title;
    @DescriptionValid(groups = {PatchGroup.class, Default.class})
    @NotEmpty(message = "Book description may not be empty")
    private String description;
    @Positive(message = "Book pages must be than 0")
    private int pages;
    @Positive(message = "Publication year must be than 0")
    @Min(value = 0, groups = {PatchGroup.class})
    private int publicationYear;
    private Genre genre;
    private BookStatus status;
}
