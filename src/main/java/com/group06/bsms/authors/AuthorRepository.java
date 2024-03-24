package com.group06.bsms.authors;

import java.sql.Connection;

import com.group06.bsms.Repository;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuthorRepository extends Repository<Author> implements AuthorDAO {

    public AuthorRepository(Connection db) {
        super(db, Author.class);
    }

    @Override
    public List<Author> selectAllAuthorNames() throws Exception {
        try {
            db.setAutoCommit(false);

            var authors = selectAll(
                    null,
                    0, null,
                    "name", Sort.ASC,
                    "name"
            );

            db.commit();

            return authors;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    @Override
    public int insertAuthorIfNotExists(String authorName) throws Exception {
        try {
            db.setAutoCommit(false);

            var query = db.prepareStatement(
                    "SELECT id FROM Author WHERE name = ?");

            query.setString(1, authorName);

            var result = query.executeQuery();

            int authorId = -1;

            if (result.next()) {
                authorId = result.getInt("id");
            } else {
                var insertQuery = db.prepareStatement(
                        "INSERT INTO Author (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                insertQuery.setString(1, authorName);
                insertQuery.executeUpdate();

                var generatedKeys = insertQuery.getGeneratedKeys();
                if (generatedKeys.next()) {
                    authorId = generatedKeys.getInt(1);
                }
            }
            db.commit();

            return authorId;
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }

    public Author selectAuthor(int id) throws Exception {
        try {
            Author author = selectById(id);
            if (author == null) {
                throw new Exception("Author not found");
            }

            db.commit();

            return author;

        } catch (Exception e) {
            db.rollback();
            throw e;
        }
    }
}
