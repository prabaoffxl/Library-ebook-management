package com.library.dto.report;

import com.library.dto.BookDTO;
import java.util.ArrayList;
import java.util.List;

public class BookReportDTO {

    private long totalBooks;
    private long availableBooks;
    private long unavailableBooks;
    private List<LabelValueDTO> booksByCategory = new ArrayList<>();
    private List<LabelValueDTO> booksByLanguage = new ArrayList<>();
    private List<LabelValueDTO> booksByPublicationYear = new ArrayList<>();
    private List<LabelValueDTO> booksByLibrary = new ArrayList<>();
    private List<BookDTO> recentlyAddedBooks = new ArrayList<>();
    private List<LabelValueDTO> mostViewedBooks = new ArrayList<>();
    private List<LabelValueDTO> mostDownloadedBooks = new ArrayList<>();

    public BookReportDTO() {
    }

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public long getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(long availableBooks) {
        this.availableBooks = availableBooks;
    }

    public long getUnavailableBooks() {
        return unavailableBooks;
    }

    public void setUnavailableBooks(long unavailableBooks) {
        this.unavailableBooks = unavailableBooks;
    }

    public List<LabelValueDTO> getBooksByCategory() {
        return booksByCategory;
    }

    public void setBooksByCategory(List<LabelValueDTO> booksByCategory) {
        this.booksByCategory = booksByCategory;
    }

    public List<LabelValueDTO> getBooksByLanguage() {
        return booksByLanguage;
    }

    public void setBooksByLanguage(List<LabelValueDTO> booksByLanguage) {
        this.booksByLanguage = booksByLanguage;
    }

    public List<LabelValueDTO> getBooksByPublicationYear() {
        return booksByPublicationYear;
    }

    public void setBooksByPublicationYear(List<LabelValueDTO> booksByPublicationYear) {
        this.booksByPublicationYear = booksByPublicationYear;
    }

    public List<LabelValueDTO> getBooksByLibrary() {
        return booksByLibrary;
    }

    public void setBooksByLibrary(List<LabelValueDTO> booksByLibrary) {
        this.booksByLibrary = booksByLibrary;
    }

    public List<BookDTO> getRecentlyAddedBooks() {
        return recentlyAddedBooks;
    }

    public void setRecentlyAddedBooks(List<BookDTO> recentlyAddedBooks) {
        this.recentlyAddedBooks = recentlyAddedBooks;
    }

    public List<LabelValueDTO> getMostViewedBooks() {
        return mostViewedBooks;
    }

    public void setMostViewedBooks(List<LabelValueDTO> mostViewedBooks) {
        this.mostViewedBooks = mostViewedBooks;
    }

    public List<LabelValueDTO> getMostDownloadedBooks() {
        return mostDownloadedBooks;
    }

    public void setMostDownloadedBooks(List<LabelValueDTO> mostDownloadedBooks) {
        this.mostDownloadedBooks = mostDownloadedBooks;
    }
}
