package com.github.kongmu373;

import com.github.kongmu373.pojo.New;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    private static void mockData(SqlSessionFactory sqlSessionFactory, int howMany) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {

            List<New> currentNew = session.selectList("com.github.kongmu373.MockMapper.selectNews");
            int count = howMany - currentNew.size();
            Random random = new Random();
            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNew.size());
                    New newTobeInstead = new New(currentNew.get(index));
                    Instant currentTime = newTobeInstead.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 60 * 365));
                    newTobeInstead.setCreatedAt(currentTime);
                    newTobeInstead.setModifiedAt(currentTime);
                    session.insert("com.github.kongmu373.MockMapper.insertNew", newTobeInstead);
                    System.out.println("Left:" + count);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            mockData(sqlSessionFactory, 100_0000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
