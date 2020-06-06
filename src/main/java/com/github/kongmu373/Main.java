package com.github.kongmu373;

import com.github.kongmu373.dao.MybatisCrawelDao;

public class Main {
    public static void main(String[] args) {
        final MybatisCrawelDao dao = new MybatisCrawelDao();

        for (int i = 0; i < 10; i++) {
            new Crawel(dao).start();
        }
    }
}
