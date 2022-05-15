package io.uouo.wechatbot.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.uouo.wechatbot.WechatBotApplication;
import io.uouo.wechatbot.common.WechatBotCommon;
import io.uouo.wechatbot.common.WechatBotConfig;
import io.uouo.wechatbot.domain.WechatMsg;
import io.uouo.wechatbot.domain.WechatReceiveMsg;
import io.uouo.wechatbot.service.WechatBotService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * websocket机器人客户端
 *
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-16 18:20
 * @Description: < 描述 >
 */
public class WechatBotClient extends WebSocketClient implements WechatBotCommon {

    @Autowired
    private WechatBotService wechatBotService;


    /**
     * 描述: 构造方法创建 WechatBotClient对象
     *
     * @param url WebSocket链接地址
     * @return
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    public WechatBotClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    /**
     * 描述: 在websocket连接开启时调用
     *
     * @param serverHandshake
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.err.println("已发送尝试连接到微信客户端请求");
    }

    /**
     * 描述: 方法在接收到消息时调用
     *
     * @param msg
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    // https://api.ownthink.com/bot?appid=xiaosi&spoken=%E5%95%8A
    // {
    //    "message": "success",
    //    "data": {
    //        "type": 5000,
    //        "info": {
    //            "text": "你会说哦吗？"
    //        }
    //    }
    //}
    @Override
    public void onMessage(String msg) {
        // 由于我的机器人是放在某个小服务器上的, 就将接收数据后的处理交给了另外一个服务器(看群里好多群友也这么干的)所以我这里就加了这几行代码,这根据自己的想法进行自定义
        // 这里也可以不进行转换 直接将微信中接收到的消息交给服务端, 提高效率,但是浪费在网络通信上的资源相对来说就会变多(根据自己需求自信来写没什么特别的)
        WechatReceiveMsg wechatReceiveMsg = JSONObject.parseObject(msg, WechatReceiveMsg.class);
        if (!WechatBotCommon.HEART_BEAT.equals(wechatReceiveMsg.getType())) {
            System.out.println("微信中收到了消息:" + msg);
            String content = wechatReceiveMsg.getContent();
            String wxid = wechatReceiveMsg.getWxid();

            WechatMsg wechatMsg = new WechatMsg();
            wechatMsg.setWxid(wxid);
            boolean needReply = false;
            try {
                if (wxid == null) {
                    return;
                }
                boolean isChatroom = wxid.contains("@chatroom");

                // 如果不是群, 则需要回复
                needReply = isChatroom == false;

                // 如果是群, 并且包含triggerText, 也需要回复
                String triggerText = "@狄思思";
                if (isChatroom && content != null && content.toLowerCase().contains(triggerText)) {
                    needReply = true;
                }

                if (needReply) {
                    // 消息为空, 回复固定文本
                    if (content == null || "".equals(content.trim())) {
                        wechatMsg.setContent("我不太明白你在说什么~");
                        wechatBotService.sendTextMsg(wechatMsg);
                        return;
                    }

                    // 如果是群，需要移除triggerText和一位占位符
                    // 如果群内只是at, 并没有跟其他消息文本, 下面这段逻辑会报错, 回复exception内的消息
                    if (isChatroom) {
                        content = content.toLowerCase().replace(triggerText, "");
                        content = content.substring(1);
                    }

                    wechatMsg.setContent(this.handleReply(content));
                }
            } catch (Exception e) {
                wechatMsg.setContent("我不太明白你在说什么~");
            }
            // 发送消息
            if (needReply) {
                wechatBotService.sendTextMsg(wechatMsg);
            }
        }

        // 是否开启远程处理消息功能
        if (WechatBotConfig.wechatMsgServerIsOpen) {
            // 不等于心跳包
            if (!WechatBotCommon.HEART_BEAT.equals(wechatReceiveMsg.getType())) {
                HttpUtil.post(WechatBotConfig.wechatMsgServerUrl, msg);
            }
        }
    }

    private String handleReply(String content) throws UnsupportedEncodingException {
        content = URLEncoder.encode(content, "UTF-8");
        String res = HttpUtil.get("https://api.ownthink.com/bot?appid=xiaosi&spoken=" + content);
        JSONObject obj = JSON.parseObject(res);
        return obj.getJSONObject("data").getJSONObject("info").getString("text");
    }

    /**
     * 描述: 方法在连接断开时调用
     *
     * @param i
     * @param s
     * @param b
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("已断开连接... ");
    }

    /**
     * 描述: 方法在连接出错时调用
     *
     * @param e
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-16
     */
    @Override
    public void onError(Exception e) {
        System.err.println("通信连接出现异常:" + e.getMessage());
    }

    /**
     * 描述: 发送消息工具 (其实就是把几行常用代码提取出来 )
     *
     * @param wechatMsg 消息体
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-18
     */
    public void sendMsgUtil(WechatMsg wechatMsg) {
        if (!StringUtils.hasText(wechatMsg.getExt())) {
            wechatMsg.setExt(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getNickname())) {
            wechatMsg.setNickname(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getRoomid())) {
            wechatMsg.setRoomid(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getContent())) {
            wechatMsg.setContent(NULL_MSG);
        }
        if (!StringUtils.hasText(wechatMsg.getWxid())) {
            wechatMsg.setWxid(NULL_MSG);
        }
        // 消息Id
        wechatMsg.setId(String.valueOf(System.currentTimeMillis()));
        // 发送消息
        String string = JSONObject.toJSONString(wechatMsg);
        System.err.println(":" + string);
        send(JSONObject.toJSONString(wechatMsg));
    }
}