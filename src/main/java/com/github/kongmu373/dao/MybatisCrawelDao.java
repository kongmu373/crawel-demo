package com.github.kongmu373.dao;

import com.github.kongmu373.pojo.New;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawelDao implements CrawelDao {
    SqlSessionFactory sqlSessionFactory;

    public MybatisCrawelDao() {
        this("development");
    }

    public MybatisCrawelDao(String env) {
        String resource = "db/mybatis/mybatis-config.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, env);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized String getLinkAndDeleteLink() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("selectTobeProcessedLink");
            if (link != null) {
                session.delete("deleteLinkInTobeProcessed", link);
            }
            return link;
        }
    }

    @Override
    public boolean isVisitedLinkSearchFromDatabase(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("countAlreadyProcessedLink", link);
            return count > 0;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String link, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("insertNew", new New(title, link, content));
        }
    }

    @Override
    public void insertLinkToProcessed(String link) {
        insertLink("LINKS_ALREADY_PROCESSED", link);
    }

    @Override
    public void insertLinkToBeProcessed(String href) {
        insertLink("LINKS_TO_BE_PROCESSED", href);
    }

    private void insertLink(String tableName, String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            Map<String, String> map = new HashMap<>();
            map.put("tablename", tableName);
            map.put("link", link);
            session.insert("insertLink", map);
        }
    }

}
