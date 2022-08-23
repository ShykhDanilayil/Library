package com.library.library.controller.dto;

import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.PatchGroup;
import com.library.library.controller.validation.PhoneNumberValid;
import com.library.library.controller.validation.PostalCodeValid;
import com.library.library.controller.validation.UniqueEmail;
import com.library.library.controller.validation.UniqueNameLibrary;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.groups.Default;

@Data
@Builder
public class LibraryDto {
    @UniqueNameLibrary(groups = {PatchGroup.class, Default.class})
    @NotEmpty(message = "Library name may not be empty")
    private String name;
    @EmailValid(groups = {PatchGroup.class, Default.class})
    @UniqueEmail(groups = {PatchGroup.class, Default.class})
    private String email;
    @PhoneNumberValid(groups = {PatchGroup.class, Default.class})
    private String phone;
    @NotEmpty(message = "Library country may not be empty")
    private String country;
    @NotEmpty(message = "Library city may not be empty")
    private String city;
    @NotEmpty(message = "Library address may not be empty")
    private String address;
    @PostalCodeValid(groups = {PatchGroup.class, Default.class})
    @NotEmpty(message = "Library postal code may not be empty")
    private String postalCode;
}
