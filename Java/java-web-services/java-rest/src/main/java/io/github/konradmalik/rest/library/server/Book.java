package io.github.konradmalik.rest.library.server;

import java.util.List;

public class Book implements java.io.Serializable {
    private String isbn;
    private String title;
    private String publisher;
    private String pubYear;
    private List<Author> authors;

    public Book() {
    } // Constructor and class must be public for instances to
    // be treated as beans.

    public Book(String isbn, String title, String publisher, String pubYear,
                List<Author> authors) {
        setISBN(isbn);
        setTitle(title);
        setPublisher(publisher);
        setPubYear(pubYear);
        setAuthors(authors);
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public String getISBN() {
        return isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getPubYear() {
        return pubYear;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void setISBN(String isbn) {
        this.isbn = isbn;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPubYear(String pubYear) {
        this.pubYear = pubYear;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}