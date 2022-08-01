package com.library.library.controller.dto;

import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.PhoneNumberValid;
import com.library.library.controller.validation.PostalCodeValid;
import com.library.library.controller.validation.UniqueEmail;
import com.library.library.controller.validation.UniqueNameLibrary;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class LibraryDto {
    @UniqueNameLibrary
    @NotEmpty(message = "Library name may not be empty")
    private String name;
    @EmailValid
    @UniqueEmail
    private String email;
    @PhoneNumberValid
    private String phone;
    @NotEmpty(message = "Library country may not be empty")
    private String country;
    @NotEmpty(message = "Library city may not be empty")
    private String city;
    @NotEmpty(message = "Library address may not be empty")
    private String address;
    @PostalCodeValid
    @NotEmpty(message = "Library postal code may not be empty")
    private String postalCode;
}
