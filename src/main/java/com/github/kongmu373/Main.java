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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        List<String> links = new LinkedList<>();
        Set<String> visitedLinks = new HashSet<>();

        String firstLink = "https://sina.cn";
        links.add(firstLink);
        while (!links.isEmpty()) {
            String link = links.remove(0);
            if (link.startsWith("//")) {
                link = "https:" + link;
            }
            if (StringUtils.isEmpty(link) || visitedLinks.contains(link) || !isValidLink(link)) {
                continue;
            }

            Document document = parsePage(link, visitedLinks);
            storeIntoDatabaseIfItIsNewsPage(link, document);
            getLinksByParsePage(links, document);
        }

    }

    private static Document parsePage(String link, Set<String> visitedLinks) {
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
            visitedLinks.add(link);
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

    private static void getLinksByParsePage(List<String> links, Document document) {
        document.select("a").stream().map(item -> item.attr("href")).forEach(links::add);
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
