package io.uouo.wechatbot.common;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteConnection {
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:wechatbot.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}
