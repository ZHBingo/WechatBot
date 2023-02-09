package io.uouo.wechatbot.client;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.uouo.wechatbot.common.ApplicationListener;
import io.uouo.wechatbot.common.SpringContext;
import io.uouo.wechatbot.common.WechatBotCommon;
import io.uouo.wechatbot.domain.WechatMsg;
import io.uouo.wechatbot.domain.WechatReceiveMsg;
import io.uouo.wechatbot.service.WechatBotService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * websocket机器人客户端
 *
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-16 18:20
 * @Description: < 描述 >
 */
public class WechatBotClient extends WebSocketClient implements WechatBotCommon {

    private final WechatBotService wechatBotService;

    private final DruidDataSource dataSource;

    private final Map<String, String> nickNameMap = new HashMap<>();


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
        wechatBotService = SpringContext.getBean(WechatBotService.class);
        dataSource = SpringContext.getBean(DruidDataSource.class);
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
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        wechatBotService.getWeChatUserList();
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
        // 如果是群消息, 则只有@的时候才会回复
        // 如果是单发的消息, 则直接回复
        // 为了防止微信异常检测, 消息回复有 1 ~ 6秒的延迟
        // 如果是#号开头的消息, 存盘用于自定义接口回复
        // 非#开头的消息, 转接自动应答API
        WechatReceiveMsg wechatReceiveMsg = new WechatReceiveMsg();
        try {
            wechatReceiveMsg = JSONObject.parseObject(msg, WechatReceiveMsg.class);
        } catch (Exception e) {
            System.out.println("-> " + e.getMessage());
            System.out.println("=========================");
            System.out.println("message = " + msg);
            System.out.println("=========================");
            return;
        }

        if (!WechatBotCommon.HEART_BEAT.equals(wechatReceiveMsg.getType())) {
            System.out.println("微信中收到了消息:" + msg);
            String content = wechatReceiveMsg.getContent().trim();
            String wxid = wechatReceiveMsg.getWxid();

            if (WechatBotCommon.USER_LIST.equals(wechatReceiveMsg.getType())) {
                System.err.println("更新用户列表");
                nickNameMap.clear();
                try {
                    JSONObject obj = JSON.parseObject(msg);
                    JSONArray array = obj.getJSONArray("content");
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject user = array.getJSONObject(i);
                        nickNameMap.put(user.getString("wxid"), user.getString("name"));
                    }
                } catch (Exception e) {
                    System.err.println("解析用户信息异常, " + e.getMessage());
                    e.printStackTrace();
                }

                if (nickNameMap.isEmpty() == false) {
                    try (Connection conn = dataSource.getConnection()) {
                        PreparedStatement truncateUserList = conn.prepareStatement("truncate table userlist");
                        truncateUserList.execute();

                        PreparedStatement ps = conn.prepareStatement("insert into userlist(`wxid`,`name`) values(?,?)");

                        int i = 0;
                        for (String key : nickNameMap.keySet()) {
                            ps.setString(1, key);
                            ps.setString(2, nickNameMap.get(key));
                            ps.addBatch();
                            if (i++ > 500) {
                                i = 0;
                                ps.executeBatch();
                                ps.clearBatch();
                            }
                        }
                        if (i > 0) {
                            ps.executeBatch();
                            ps.clearBatch();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                System.err.println("更新用户列表成功");
                return;
            }

            // 收到发送消息回复报文, 更新回复状态
            if (content.startsWith("send") && "SERVER".equals(wechatReceiveMsg.getSender())) {
                String sql = "update message set reply_stat = ? where reply_id = ?";
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, wechatReceiveMsg.getStatus());
                    ps.setString(2, wechatReceiveMsg.getId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // bot只回复文字消息
            if (WechatBotCommon.RECV_TXT_MSG.equals(wechatReceiveMsg.getType())) {
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
                    // 如果是群，需要移除triggerText和一位占位符
                    // 如果群内只是at, 并没有跟其他消息文本, 下面这段逻辑会报错, 回复exception内的消息
                    String triggerText = "@狄思思";
                    if (isChatroom && content.toLowerCase().startsWith(triggerText)) {
                        content = content.toLowerCase().replace(triggerText, "");
                        content = content.substring(1);
                        content = content.trim();
                        needReply = true;
                    }

                    if (needReply) {
                        // 消息为空, 回复固定文本
                        if ("".equals(content)) {
                            wechatMsg.setContent("我不太明白你在说什么~");
                            new Thread(new DelaySendClient(wechatBotService, wechatMsg)).start();
                            return;
                        }

                        wechatMsg.setContent(this.handleReply(wxid, content, wechatReceiveMsg));
                    }
                } catch (Exception e) {
                    wechatMsg.setContent("我不太明白你在说什么~");
                }
                // 发送消息
                if (needReply) {
                    new Thread(new DelaySendClient(wechatBotService, wechatMsg)).start();
                }
            }
        }
    }

    final List<String> replyTemplate = new ArrayList<String>() {{
        add("收到，稍等[吃瓜]");
        add("收到，稍等[OK]");
        add("[OK]");
        add("[好的]");
        add("我记下来了");
        add("OK");
        add("你先忙，我待会告诉你[好的]");
        add("不出意外我待会告诉你");
        add("好的，老板，马上就好");
        add("我看看，待会告诉你");
        add("[嘿哈]");
    }};

    private static volatile short CHATGPT_COUNTER = 0;

    private String handleReply(String wxid, String content, WechatReceiveMsg wechatReceiveMsg) throws UnsupportedEncodingException {
        if (StringUtils.startsWithIgnoreCase(content, "#")) {
            if (content.equals("#")) {
                return "你想查询什么呀?";
            }
            content = content.substring(1).trim();
            // 校验合法性, 使用分词器来筛选出必要的主语, 比如库存等, 如果无法正常筛选, 则直接返回无法解析
            if (this.isQuestion(content) == false) {
                return "我没理解你的问题 -_-# ";
            }

            // 保存数据库
            try {
                this.saveMessage(content, wechatReceiveMsg);
                return replyTemplate.get((int) (replyTemplate.size() * Math.random()));
            } catch (Exception ex) {
                return "哎呀，我没有记住，可能是哪里出问题了吧~~";
            }
        } else {
            if (CHATGPT_COUNTER < 5) {
                synchronized (this) {
                    CHATGPT_COUNTER++;
                }
                synchronized (this) {
                    try {
                        // PARENT_MSG.put(wxid, content); 禁用上下文
                        Map<String, Object> promptMap = new HashMap<>();
                        promptMap.put("prompt", content);
                        promptMap.put("conversation_id", wxid);
                        String body = JSON.toJSONString(promptMap);
                        String chatGPT = "http://124.220.35.202:56790/chatgpt_for_wechat";
                        try (HttpResponse res = HttpUtil.createPost(chatGPT).body(body).header("Content-Type", "application/json;charset=UTF-8").execute(false)) {
                            String result = res.body();
                            Map<String, Object> temp = new HashMap<>();
                            temp.put("result", result);
                            String jsonResult = JSON.toJSONString(temp);
                            System.out.println("ChatGPT Response=" + jsonResult);
                            JSONObject obj = JSON.parseObject(jsonResult);
                            return obj.getString("result");
                        }
                    } catch (Exception e) {
                        return e.getMessage().substring(0, 50);
                    } finally {
                        CHATGPT_COUNTER--;
                    }
                }
            } else {
                content = URLEncoder.encode(content, "UTF-8");
                String res = HttpUtil.get("https://api.ownthink.com/bot?appid=xiaosi&spoken=" + content);
                JSONObject obj = JSON.parseObject(res);
                return obj.getJSONObject("data").getJSONObject("info").getString("text");
            }
        }
    }

    // 检测关键字
    private boolean isQuestion(String content) {
        if (content.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private void saveMessage(String content, WechatReceiveMsg wechatReceiveMsg) {
        String sql = "insert into wechat.message(id, wxid, content, recv_time, at_wxid, at_nickname) values (?, ?, ?, now(), ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, wechatReceiveMsg.getId());
            ps.setString(2, wechatReceiveMsg.getWxid());
            ps.setString(3, content);

            if (wechatReceiveMsg.getWxid().contains("@chatroom")) {
                ps.setString(4, wechatReceiveMsg.getId1());
                ps.setString(5, nickNameMap.getOrDefault(wechatReceiveMsg.getId1(), wechatReceiveMsg.getId1()));
            } else {
                ps.setString(4, null);
                ps.setString(5, null);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        System.out.println("已断开连接, code=" + i + ", reason=" + s + ", remote=" + b);
        ApplicationListener.connect();
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
        if (wechatMsg.getId() == null) {
            wechatMsg.setId(System.currentTimeMillis() + "_" + (int) (Math.random() * 1000));
        }
        // 发送消息
        String string = JSONObject.toJSONString(wechatMsg);
        System.err.println("send to wechat: " + string);
        send(JSONObject.toJSONString(wechatMsg));
    }
}
