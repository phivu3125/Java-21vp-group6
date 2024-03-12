package com.group06.bsms.books;

import java.util.List;

public interface BookDAO {
    //get 
    boolean existsBookById(int id) throws Exception;
    List<Book> selectAllBooks() throws Exception;
    List<Book> selectBook(String title) throws Exception;
    //post 
    void createBook(Book book) throws Exception;
    //put 
    void enableBook(int id) throws Exception;
    void disableBook(int id) throws Exception;
    void updateHiddenParentCount(int id) throws Exception;
    void updateHiddenParentCountByAuthorId(int authorId) throws Exception;
    void updateHiddenParentCountByPublisherId(int publisherId) throws Exception;
}
