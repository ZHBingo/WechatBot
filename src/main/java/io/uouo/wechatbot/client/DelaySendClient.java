package io.uouo.wechatbot.client;


import io.uouo.wechatbot.domain.WechatMsg;
import io.uouo.wechatbot.service.WechatBotService;

import java.util.concurrent.TimeUnit;

public class DelaySendClient implements Runnable {

    private final WechatBotService wechatBotService;

    private final WechatMsg wechatMsg;

    public DelaySendClient(WechatBotService wechatBotService, WechatMsg wechatMsg) {
        this.wechatBotService = wechatBotService;
        this.wechatMsg = wechatMsg;
    }

    @Override
    public void run() {
        double mil = 1000 + (5000 * Math.random());
        try {
            TimeUnit.MILLISECONDS.sleep((long) mil);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wechatBotService.sendTextMsg(wechatMsg);
    }
}
