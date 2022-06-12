package com.linkedinjms.chapter4.pojos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {
	private final String bookId;
	private final String title;
	
	@JsonCreator
    public Book(@JsonProperty("bookId") String bookId, @JsonProperty("title")String title) {
        this.bookId = bookId;
        this.title = title;
    }

	public String getTitle() {
		return title;
	}

	public String getBookId() {
		return bookId;
	}
}
