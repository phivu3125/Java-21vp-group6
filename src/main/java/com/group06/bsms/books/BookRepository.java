package com.group06.bsms.books;

import com.google.gson.JsonObject;
import com.group06.bsms.Repository;
import com.group06.bsms.authors.Author;
import com.group06.bsms.publishers.Publisher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.util.List;

public class BookRepository extends Repository<Book> implements BookDAO {
    public BookRepository(Connection db) {
        super(db, Book.class);
    }

    @Override
    public void update(Book book) throws Exception {
        try {
            db.setAutoCommit(false);

            var query = db.prepareStatement(
                    "UPDATE books SET authorId=?, publisherId=?, title=?, pageCount=?, publishDate=?, dimension=?, translatorName=?, overview=?, quantity=?, salePrice=?, hiddenParentCount=?, WHERE id=?");

            query.setInt(1, book.authorId);
            query.setInt(2, book.publisherId);
            query.setString(3, book.title);
            query.setInt(4, book.pageCount);
            query.setDate(5, book.publishDate);
            query.setString(6, book.dimension);
            query.setString(7, book.translatorName);
            query.setString(8, book.overview);
            query.setInt(9, book.quantity);
            query.setDouble(10, book.salePrice);
            query.setInt(11, book.hiddenParentCount);
            // query.setDouble(12, book.maxImportPrice);
            query.setInt(13, book.id);

            var result = query.executeUpdate();
            
            db.commit();

            if (result == 0) {
                throw new Exception("Entity not found");
            }

            // query = db.prepareStatement("DELETE * FROM BookCategory WHERE bookId = ?");
            // query.setInt(1, book.id);

            // result = query.executeUpdate();

            // if (result == 0) {
            // throw new Exception("Can't remove old book's category");
            // }
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    private Author getAuthor(int authorId)
            throws Exception {
        try {
            db.setAutoCommit(false);

            Author author;

            var query1 = db.prepareStatement("""
                        select * from author
                        where id = ?
                    """);
            query1.setInt(1, authorId);

            var result = query1.executeQuery();

            if (result.next()) {
                author = new Author(authorId, result.getString("name"), result.getString("overview"),
                        result.getBoolean("ishidden"));
            } else
                throw new Exception("Entity not found");

            db.commit();

            return author;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    private Publisher getPublisher(int publisherId)
            throws Exception {
        try {
            db.setAutoCommit(false);

            Publisher publisher;

            var query2 = db.prepareStatement("""
                        select * from publisher
                        where id = ?
                    """);

            query2.setInt(1, publisherId);

            var result2 = query2.executeQuery();

            if (result2.next()) {
                publisher = new Publisher(publisherId, result2.getString("name"), result2.getString("email"),
                        result2.getString("address"), result2.getBoolean("ishidden"));
            } else
                throw new Exception("Entity not found");

            return publisher;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public boolean existsBookById(int id)
            throws Exception {
        try {
            db.setAutoCommit(false);

            db.commit();

            return true;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public List<Book> selectAllBooks()
            throws Exception {
        try {
            db.setAutoCommit(false);

            var list = selectAll(
                    null,
                    0, 10,
                    "title", Sort.ASC,
                    "id", "authorid", "publisherid", "title", "pagecount",
                    "publishdate", "dimension", "translatorname",
                    "overview", "quantity", "saleprice",
                    "hiddenparentcount");

            for (var book : list) {
                book.author = getAuthor(book.authorId);
                book.publisher = getPublisher(book.publisherId);
            }

            db.commit();

            return list;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public List<Book> selectBook(String title)
            throws Exception {
        try {
            db.setAutoCommit(false);

            var jsonSearch = new JsonObject();
            jsonSearch.addProperty("title", title);

            System.out.println(jsonSearch.toString());

            var list = selectAll(
                    jsonSearch,
                    0, 10,
                    "title", Sort.ASC,
                    "id", "authorid", "publisherid", "title", "pagecount",
                    "publishdate", "dimension", "translatorname",
                    "overview", "quantity", "saleprice",
                    "hiddenparentcount");

            for (var book : list) {
                book.author = getAuthor(book.authorId);
                book.publisher = getPublisher(book.publisherId);
            }

            db.commit();

            return list;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    // post methods
    @Override
    public void createBook(Book book)
            throws Exception {
        try {
            db.setAutoCommit(false);

            db.commit();

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public void enableBook(int id)
            throws Exception {
        try {
            db.setAutoCommit(false);

            updateById(id, "ishidden", "false");

            db.commit();

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public void disableBook(int id)
            throws Exception {
        try {
            db.setAutoCommit(false);

            var book = selectById(id);

            if (book.hiddenParentCount > 0) {
                throw new Exception("Publisher and/or Author Hidden");
            }

            updateById(id, "ishidden", "true");

            db.commit();

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    /**
     * Given a book's ID and
     * 
     * @param id Book's ID
     */
    @Override
    public void updateHiddenParentCount(int id)
            throws Exception {
        try {
            db.setAutoCommit(false);

            Book book = selectById(id);
            Author author = getAuthor(book.authorId);
            Publisher publisher = getPublisher(book.publisherId);

            Integer hiddenParentCount = 0;
            if (author.isHidden)
                hiddenParentCount += 1;
            if (publisher.isHidden)
                hiddenParentCount += 1;

            updateById(id, "hiddenparentcount", hiddenParentCount.toString());

            db.commit();

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public void updateHiddenParentCountByAuthorId(int authorId)
            throws Exception {
        try {
            db.setAutoCommit(false);

            JsonObject json = new JsonObject();
            json.addProperty("authorid", authorId);

            List<Book> books = selectAll(json, 0, null, null, null, "id");

            for (Book book : books) {
                updateHiddenParentCount(book.id);
            }

            db.commit();

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public void updateHiddenParentCountByPublisherId(int publisherId)
            throws Exception {
        try {
            db.setAutoCommit(false);

            JsonObject json = new JsonObject();
            json.addProperty("authorid", publisherId);

            List<Book> books = selectAll(json, 0, null, null, null, "id");

            for (Book book : books) {
                updateHiddenParentCount(book.id);
            }

            db.commit();

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }
}
