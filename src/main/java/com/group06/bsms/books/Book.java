package com.group06.bsms.books;

import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.group06.bsms.authors.Author;
import com.group06.bsms.publishers.Publisher;
import java.sql.Date;
import java.util.List;

public class Book {

    public int id;
    public int authorId;
    public int publisherId;
    public String title;
    public int pageCount;
    public Date publishDate;
    public String dimension;
    public String translatorName;
    public String overview;
    public int quantity;
    public double salePrice;
    public boolean isHidden;
    public int hiddenParentCount;

    public Author author;
    public Publisher publisher;

    public List<Category> categories;

    // definition

    public Book() {
    }

    public Book(
            int authorId, int publisherId, String title, int pageCount,
            Date publishDate, String dimension, String translatorName,
            String overview, int quantity, double salePrice,
            int hiddenParentCount
    ) {
        this.authorId = authorId;
        this.publisherId = publisherId;
        this.title = title;
        this.pageCount = pageCount;
        this.publishDate = publishDate;
        this.dimension = dimension;
        this.translatorName = translatorName;
        this.overview = overview;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.hiddenParentCount = hiddenParentCount;
    }

    @Override
    public String toString() {
        return "Book{" + "id=" + id + ", authorId=" + authorId + ", publisherId=" + publisherId + ", title=" + title + ", pageCount=" + pageCount + ", publishDate=" + publishDate + ", dimension=" + dimension + ", translatorName=" + translatorName + ", overview=" + overview + ", quantity=" + quantity + ", salePrice=" + salePrice + ", isHidden=" + isHidden + ", hiddenParentCount=" + hiddenParentCount + ", author=" + author + ", publisher=" + publisher + '}';
    }
}
