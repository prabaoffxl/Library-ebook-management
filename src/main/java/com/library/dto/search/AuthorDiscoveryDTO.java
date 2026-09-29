package com.library.dto.search;

public class AuthorDiscoveryDTO {
    private String authorName;
    private long bookCount;

    public AuthorDiscoveryDTO() {
    }

    public AuthorDiscoveryDTO(String authorName, long bookCount) {
        this.authorName = authorName;
        this.bookCount = bookCount;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public long getBookCount() {
        return bookCount;
    }

    public void setBookCount(long bookCount) {
        this.bookCount = bookCount;
    }
}
