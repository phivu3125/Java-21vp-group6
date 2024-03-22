package com.group06.bsms.books;

import java.util.List;
import java.util.Map;
import javax.swing.SortOrder;

public interface BookDAO {

    boolean existsBookById(int id) throws Exception;

    void insertBook(Book book) throws Exception;

    void showBook(int id) throws Exception;

    void hideBook(int id) throws Exception;

    void updateBookHiddenParentCount(int id) throws Exception;

    void updateBookHiddenParentCountByAuthorId(int authorId) throws Exception;

    void updateBookHiddenParentCountByPublisherId(int publisherId) throws Exception;

    List<Book> selectSearchSortFilterBooks(int offset, int limit, Map<Integer, SortOrder> sortValue,
            String searchString, String searchChoice,
            int authorId, int publisherId, Double minPrice, Double maxPrice,
            List<Integer> listBookCategoryId) throws Exception;

    void updateBookAttributeById(int bookId, String attr, Object value) throws Exception;
}
