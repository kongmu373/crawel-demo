package com.github.kongmu373.dao;

import com.suppresswarnings.things.SuppressWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("DMI_CONSTANT_DB_PASSWORD")
public class JDBCCrawelDao implements CrawelDao {
    public static final String USER = "root";
    public static final String PASSWORD = "root";
    private final Connection connection;

    public JDBCCrawelDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:C:\\Users\\48011\\Desktop\\temp\\crawel-demo\\news", USER, PASSWORD);
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    public String getLinkAndDeleteLink() throws SQLException {
        String link = getLinkByDatabase("select link from LINKS_TO_BE_PROCESSED limit 1;");
        if (link != null) {
            updateLinksByDatabase("delete from LINKS_TO_BE_PROCESSED where link = ?", link);
        }
        return link;
    }

    public boolean isVisitedLinkSearchFromDatabase(String link) throws SQLException {
        String sql = "select count(*) from LINKS_ALREADY_PROCESSED where link = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    final int i = resultSet.getInt(1);
                    return i > 0;
                }
            }
        }
        return false;
    }

    private void updateLinksByDatabase(String sql, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private String getLinkByDatabase(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    public void insertNewsIntoDatabase(String link, String title, String content) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) values ( ?, ?, ?, NOW(), NOW())")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, link);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void insertLinkToProcessed(String link) {
        try {
            updateLinksByDatabase("insert into LINKS_ALREADY_PROCESSED (link) values (?)", link);
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String href) {
        try {
            updateLinksByDatabase("insert into LINKS_TO_BE_PROCESSED (link) values (?)", href);
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }
}
