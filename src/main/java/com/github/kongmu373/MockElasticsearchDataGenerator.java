package com.github.kongmu373;

import com.github.kongmu373.pojo.New;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockElasticsearchDataGenerator {

    public static void main(String[] args) {
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            List<New> news = getNewsFromMySQL(sqlSessionFactory);

            for (int i = 0; i < 10; i++) {
                new Thread(()->writeBulkToIndex(news)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeBulkToIndex(List<New> newsFromMySQL) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (int i = 0; i < 400; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (New news : newsFromMySQL) {
                    IndexRequest request = new IndexRequest("news");

                    Map<String, Object> data = new HashMap<>();

                    data.put("content", news.getContent().length() > 100 ? news.getContent().substring(0, 100) : news.getContent());
                    data.put("url", news.getUrl());
                    data.put("title", news.getTitle());
                    data.put("createdAt", news.getCreatedAt());
                    data.put("modifiedAt", news.getModifiedAt());

                    request.source(data, XContentType.JSON);

                    bulkRequest.add(request);
                }

                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

                System.out.println("Current thread: " + Thread.currentThread().getName() + " finishes " + i + ": " + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<New> getNewsFromMySQL(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.kongmu373.MockMapper.selectNews");
        }
    }
}
