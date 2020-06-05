package com.github.kongmu373.dao;

import java.sql.SQLException;

public interface CrawelDao {
    String getLinkAndDeleteLink() throws SQLException;

    boolean isVisitedLinkSearchFromDatabase(String link) throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;

    void insertLinkToProcessed(String link);

    void insertLinkToBeProcessed(String href);
}
