package com.library.library.controller.dto;

import com.library.library.controller.validation.EmailValid;
import com.library.library.controller.validation.PhoneNumber;
import com.library.library.controller.validation.PostalCode;
import com.library.library.controller.validation.UniqueEmail;
import com.library.library.service.model.Role;
import com.library.library.service.model.User;
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
    @PhoneNumber
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
    @PostalCode
    private String postalCode;

    public UserDto(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.phone = user.getPhone();
        this.birthday = user.getBirthday();
        this.country = user.getCountry();
        this.city = user.getCity();
        this.address = user.getAddress();
        this.postalCode = user.getPostalCode();
    }
}
