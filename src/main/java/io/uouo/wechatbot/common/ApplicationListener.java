package io.uouo.wechatbot.common;

import io.uouo.wechatbot.client.WechatBotClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationListener implements ApplicationRunner {

    private static String wechatBotUrl;

    public static WechatBotClient BOT_CLIENT = null;

    @Override
    public void run(ApplicationArguments args) {
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
