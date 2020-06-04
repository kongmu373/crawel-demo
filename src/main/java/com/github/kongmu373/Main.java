package com.github.kongmu373;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:C:\\Users\\48011\\Desktop\\temp\\crawel-demo\\news");
        String link;
        while ((link = getLinkAndDeleteLink(connection)) != null) {
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            if (isVisitedLinkSearchFromDatabase(connection, link) || !isValidLink(link)) {
                continue;
            }

            Document document = parsePage(link, connection);
            storeIntoDatabaseIfItIsNewsPage(link, document);
            getLinksByParsePage(connection, document);
        }

    }

    private static String getLinkAndDeleteLink(Connection connection) throws SQLException {
        String link = getLinkByDatabase(connection, "select link from LINKS_TO_BE_PROCESSED limit 1;");
        if (!StringUtils.isEmpty(link)) {

            updateLinksByDatabase(connection, "delete from LINKS_TO_BE_PROCESSED where link = ?", link);
        }
        return link;
    }

    private static boolean isVisitedLinkSearchFromDatabase(Connection connection, String link) throws SQLException {
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

    private static void updateLinksByDatabase(Connection connection, String sql, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static String getLinkByDatabase(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static Document parsePage(String link, Connection connection) throws SQLException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36");
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            updateLinksByDatabase(connection, "insert into LINKS_ALREADY_PROCESSED VALUES ( ? );", link);
        }
    }

    private static void storeIntoDatabaseIfItIsNewsPage(String link, Document document) {
        if (linkContainValidDate(link)) {
            Elements articleTags = document.select("article");
            if (articleTags.isEmpty()) {
                return;
            }
            System.out.println(articleTags.first().child(0).text());
        }
    }

    private static void getLinksByParsePage(Connection con, Document document) throws SQLException {
        for (Element item : document.select("a")) {
            String href = item.attr("href");
            updateLinksByDatabase(con, "insert into LINKS_TO_BE_PROCESSED VALUES ( ? );", href);
        }
    }

    private static boolean isValidLink(String link) {
        return isSinaPage(link);
    }

    private static boolean isSinaPage(String link) {
        return link.contains("sina.cn") || link.contains("sina.com");
    }

    private static boolean linkContainValidDate(String link) {
        String pattern = "\\d{4}(-|/|.)\\d{1,2}\\1\\d{1,2}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(link);
        return m.find();
    }
}
