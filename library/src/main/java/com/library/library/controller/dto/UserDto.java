package com.library.library.controller.dto;

import com.library.library.controller.validation.EmailValid;
import com.library.library.service.model.Role;
import com.library.library.service.model.User;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Builder
public class UserDto {

    @NotNull
    private String firstName;
    private String lastName;
    @EmailValid
    private String email;
    private Role role;
    private String phone;
    private Date birthday;
    private String country;
    private String city;
    private String address;
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
