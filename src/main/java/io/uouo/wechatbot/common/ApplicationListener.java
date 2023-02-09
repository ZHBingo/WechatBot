package io.uouo.wechatbot.common;

import io.uouo.wechatbot.client.WechatBotClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;

@Component
public class ApplicationListener implements ApplicationRunner {

    private static String wechatBotUrl;

    public static WechatBotClient BOT_CLIENT = null;

    @Override
    public void run(ApplicationArguments args) {
        // 检查database是否存在
        try {
            String sql = "create table if not exists userlist (wxid TEXT, name TEXT)";
            try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.execute();
            }
            sql = "create table if not exists message " +
                    "(id TEXT, wxid TEXT, content TEXT, recv_time TEXT, at_wxid TEXT, at_nickname TEXT, reply_id TEXT, reply_content TEXT, reply_time TEXT, reply_stat TEXT)";
            try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        connect();
    }

    @Value("${wechatBot.url}")
    private void setWechatBotUrl(String url) {
        wechatBotUrl = url;
    }

    public static void connect() {
        try {
            BOT_CLIENT = new WechatBotClient(wechatBotUrl);
            // 建立连接
            BOT_CLIENT.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
