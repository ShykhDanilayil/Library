package com.library.library.controller.dto;

import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.PasswordValid;
import com.library.library.controller.validation.PatchGroup;
import com.library.library.controller.validation.PhoneNumberValid;
import com.library.library.controller.validation.PostalCodeValid;
import com.library.library.controller.validation.UniqueEmail;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.groups.Default;
import java.util.Date;

@Data
@Builder
public class UserDto {

    @NotEmpty(message = "Firstname may not be empty")
    private String firstName;
    @NotEmpty(message = "Lastname may not be empty")
    private String lastName;
    @UniqueEmail
    @EmailValid(groups = {PatchGroup.class, Default.class})
    @NotEmpty(message = "Email address may not be empty")
    private String email;
    @PasswordValid(groups = {PatchGroup.class, Default.class})
    @NotEmpty(message = "Please enter password")
    private String password;
    private Role role;
    private Boolean isAccountNonLocked;
    @PhoneNumberValid(groups = {PatchGroup.class, Default.class})
    @NotEmpty(message = "Phone number may not be empty")
    private String phone;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Past(groups = {PatchGroup.class, Default.class})
    @NotNull(message = "Date birthday may not be null")
    private Date birthday;
    @NotEmpty(message = "Country name may not be empty")
    private String country;
    @NotEmpty(message = "City name may not be empty")
    private String city;
    @NotEmpty(message = "Address may not be empty")
    private String address;
    @NotEmpty(message = "Postal code may not be empty")
    @PostalCodeValid(groups = {PatchGroup.class, Default.class})
    private String postalCode;
}
