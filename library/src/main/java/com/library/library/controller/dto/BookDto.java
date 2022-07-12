package com.library.library.controller.dto;

import com.library.library.controller.validation.ValidDescription;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;


@Data
@Builder
public class BookDto {

    @NotBlank
    public String title;

    @ValidDescription
    public String description;

    @Positive
    public int pages;

    @Positive
    public int publicationYear;

    @Size(min = 1)
    public Genre genre;
}
