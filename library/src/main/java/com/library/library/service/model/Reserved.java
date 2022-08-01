package com.library.library.service.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Reserved {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Timestamp dated;
    private Date dueDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private Book book;
    @ManyToOne
    private Library library;
    @ManyToOne
    private User user;

    public Reserved(Date dueDate, Book book, User user, Library library) {
        this.dated = new Timestamp(System.currentTimeMillis());
        this.dueDate = dueDate;
        this.book = book;
        this.user = user;
        this.library = library;
    }
}
