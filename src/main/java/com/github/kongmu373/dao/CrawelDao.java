package com.github.kongmu373.dao;

import java.sql.SQLException;

public interface CrawelDao {
    String getLinkAndDeleteLink() throws SQLException;

    boolean isVisitedLinkSearchFromDatabase(String link) throws SQLException;

    void updateLinksByDatabase(String sql, String link) throws SQLException;

    String getLinkByDatabase(String sql) throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;

}
