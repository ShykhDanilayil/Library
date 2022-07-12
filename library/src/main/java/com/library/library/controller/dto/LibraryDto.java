package com.library.library.controller.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class LibraryDto {
    @NotEmpty(message = "Library authorName may not be empty")
    private String name;
    @NotBlank(message = "Library address may not be empty")
    private String address;
}
