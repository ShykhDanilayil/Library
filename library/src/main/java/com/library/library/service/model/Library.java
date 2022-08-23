package com.library.library.service.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String libraryName;
    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    private String country;
    private String city;
    @Column(nullable = false)
    private String address;
    private String postalCode;
    private Instant writtenOn;
    @ManyToMany(mappedBy = "libraries")
    private List<User> users = new ArrayList<>();
    @OneToMany(mappedBy = "library")
    private Set<Book> books = new HashSet<>();
}
