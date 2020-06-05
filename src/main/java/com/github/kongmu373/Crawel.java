package com.github.kongmu373;

import com.github.kongmu373.dao.CrawelDao;
import com.github.kongmu373.dao.JDBCCrawelDao;
import com.suppresswarnings.things.SuppressWarnings;
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
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Crawel {

    private CrawelDao dao;

    public Crawel(CrawelDao dao) {
        this.dao = dao;
    }

    @SuppressWarnings("DMI_CONSTANT_DB_PASSWORD")
    public void run() throws SQLException {
        String link;
        while ((link = dao.getLinkAndDeleteLink()) != null) {
            if (dao.isVisitedLinkSearchFromDatabase(link)) {
                continue;
            }
            Document document = parsePage(link);
            storeIntoDatabaseIfItIsNewsPage(link, document);
            getLinksByParsePage(document);
        }

    }


    private Document parsePage(String link) {
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
            dao.insertLinkToProcessed(link);
        }
    }

    private void storeIntoDatabaseIfItIsNewsPage(String link, Document document) throws SQLException {
        if (linkContainValidDate(link)) {
            Elements articleTags = document.select("article");
            if (articleTags.isEmpty()) {
                return;
            }
            System.out.println(link);
            String title = articleTags.first().child(0).text();
            String content = articleTags.first().select("p").stream().map(Element::text).collect(Collectors.joining("\n"));

            dao.insertNewsIntoDatabase(link, title, content);
        }
    }

    private void getLinksByParsePage(Document document) {
        for (Element item : document.select("a")) {
            String href = item.attr("href");
            if (StringUtils.isBlank(href) || href.toLowerCase().startsWith("javascript") || !isValidLink(href)) {
                continue;
            }
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            dao.insertLinkToBeProcessed(href);
        }
    }

    private boolean isValidLink(String link) {
        return isSinaPage(link);
    }

    private boolean isSinaPage(String link) {
        return link.contains("sina.cn") || link.contains("sina.com");
    }

    private boolean linkContainValidDate(String link) {
        String pattern = "\\d{4}(-|/|.)\\d{1,2}\\1\\d{1,2}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(link);
        return m.find();
    }

    public static void main(String[] args) throws SQLException {
        new Crawel(new JDBCCrawelDao()).run();
    }
}
