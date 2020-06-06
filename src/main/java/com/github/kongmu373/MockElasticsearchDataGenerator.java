package com.github.kongmu373;

import com.github.kongmu373.pojo.New;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.github.kongmu373.CopyDataToES.getNewsFromMySQL;
import static com.github.kongmu373.CopyDataToES.writeDataToESFromMySQL;

public class MockElasticsearchDataGenerator {

    public static void main(String[] args) {
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            List<New> news = getNewsFromMySQL(sqlSessionFactory);
            for (int i = 0; i < 10; i++) {
                new Thread(() -> writeBulkToIndex(news)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeBulkToIndex(List<New> newsFromMySQL) {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (int i = 0; i < 400; i++) {
                BulkResponse bulkResponse = writeDataToESFromMySQL(newsFromMySQL, client);

                System.out.println("Current thread: " + Thread.currentThread().getName() + " finishes " + i + ": " + bulkResponse.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
