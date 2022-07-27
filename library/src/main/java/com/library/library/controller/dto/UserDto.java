package com.library.library.controller.dto;

import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.PhoneNumberValid;
import com.library.library.controller.validation.PostalCodeValid;
import com.library.library.controller.validation.UniqueEmail;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;

@Data
@Builder
public class UserDto {

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @UniqueEmail
    @EmailValid
    private String email;
    private Role role;
    @PhoneNumberValid
    private String phone;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Past
    @NotNull
    private Date birthday;
    @NotEmpty
    private String country;
    @NotEmpty
    private String city;
    @NotEmpty
    private String address;
    @PostalCodeValid
    private String postalCode;
}
