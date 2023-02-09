package io.uouo.wechatbot.service.impl;

import io.uouo.wechatbot.common.ApplicationListener;
import io.uouo.wechatbot.common.SQLiteConnection;
import io.uouo.wechatbot.common.WechatBotCommon;
import io.uouo.wechatbot.common.util.AjaxResult;
import io.uouo.wechatbot.domain.WechatMsg;
import io.uouo.wechatbot.domain.WechatReplyMsg;
import io.uouo.wechatbot.service.WechatBotService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-18 20:55
 * @Description: <  >
 */
@Service
public class WechatBotServiceImpl implements WechatBotService, WechatBotCommon {
    /**
     * 描述: 发送文字消息
     *
     * @param wechatMsg 微信消息体
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-18
     */
    @Override
    public void wechatCommon(WechatMsg wechatMsg) {
        // 消息类型
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    /**
     * 描述: 发送文字消息
     *
     * @param wechatMsg 微信消息体
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-18
     */
    @Override
    public void sendTextMsg(WechatMsg wechatMsg) {
        wechatMsg.setType(TXT_MSG);
        // 消息类型
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    /**
     * 描述: 发送图片消息
     *
     * @param wechatMsg 微信消息体
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-18
     */
    @Override
    public void sendImgMsg(WechatMsg wechatMsg) {
        wechatMsg.setType(PIC_MSG);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    /**
     * 描述: 群组内发送@指定人消息
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    @Override
    public void sendATMsg(WechatMsg wechatMsg) {
        wechatMsg.setType(AT_MSG);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }


    /**
     * 描述: 发送附件
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    @Override
    public void sendAnnex(WechatMsg wechatMsg) {
        wechatMsg.setType(ATTATCH_FILE);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }


    /**
     * 描述: 获取微信群组,联系人列表
     *
     * @param
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-29
     * @see WechatBotCommon#USER_LIST 发起后会收到一条type类型是该常量值消息
     */
    @Override
    public void getWeChatUserList() {
        WechatMsg wechatMsg = new WechatMsg();
        wechatMsg.setType(USER_LIST);
        wechatMsg.setContent(CONTACT_LIST);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    /**
     * 描述:获取指定联系人的详细信息
     *
     * @param wxid 被获取详细信息的人的 微信id
     * @return void
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-29
     */
    @Override
    public void getPersonalDetail(String wxid) {
        WechatMsg wechatMsg = new WechatMsg();
        wechatMsg.setType(PERSONAL_DETAIL);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    /**
     * 描述: 获取群组里指定联系人的详细信息
     *
     * @param roomid 群组id
     * @param wxid   指定用户id
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-5-6
     */
    @Override
    public void getChatroomMemberNick(String roomid, String wxid) {
        WechatMsg wechatMsg = new WechatMsg();
        wechatMsg.setRoomid(roomid);
        wechatMsg.setWxid(roomid);
        wechatMsg.setType(CHATROOM_MEMBER_NICK);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    @Override
    public List<Map<String, String>> getUnReplyMessage() {
        List<Map<String, String>> result = new ArrayList<>();
        String sql = "select id, content from message where (reply_stat != 'SUCCSESSED' or reply_stat is null)";
        try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put("id", rs.getString("id"));
                map.put("content", rs.getString("content"));
                result.add(map);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getUserList() {
        List<Map<String, String>> result = new ArrayList<>();
        String sql = "select wxid, name from userlist";
        try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put("wxid", rs.getString("wxid"));
                map.put("name", rs.getString("name"));
                result.add(map);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    @Override
    public AjaxResult replMessage(WechatReplyMsg wechatReplyMsg) {
        // 检查这个消息是否已经回复, 如果已经回复了就没必要再回复了
        Map<String, String> message = new HashMap<>();
        String sql = "select wxid, at_wxid, at_nickname from message where id = ? and (reply_stat != 'SUCCSESSED' or reply_stat is null)";
        try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wechatReplyMsg.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                message.put("wxid", rs.getString("wxid"));
                message.put("at_wxid", rs.getString("at_wxid"));
                message.put("at_nickname", rs.getString("at_nickname"));
            } else {
                return AjaxResult.error("无效ID");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        // 无效消息或已经回复, 返回无效ID
        if (message.isEmpty()) {
            return AjaxResult.error("无效ID");
        }

        // 如果这个消息是在群中提出来的, 那么需要发送at消息
        // 否则发送私聊消息

        // 私聊
        String replyID = System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
        if (message.get("at_wxid") == null || message.get("at_wxid").trim().length() == 0) {
            WechatMsg wechatMsg = new WechatMsg();
            wechatMsg.setId(replyID);
            wechatMsg.setWxid(message.get("wxid"));
            wechatMsg.setContent(wechatReplyMsg.getContent());
            this.sendTextMsg(wechatMsg);
        } else {
            WechatMsg wechatMsg = new WechatMsg();
            wechatMsg.setId(replyID);
            wechatMsg.setWxid(message.get("at_wxid"));
            wechatMsg.setRoomid(message.get("wxid"));
            wechatMsg.setNickname(message.get("at_nickname"));
            wechatMsg.setContent(wechatReplyMsg.getContent());
            this.sendATMsg(wechatMsg);
        }

        // 更新发送状态
        sql = "update message set reply_id = ?, reply_time = ?, reply_content = ? where id = ?";
        try (Connection conn = SQLiteConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, replyID);
            ps.setString(2, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            ps.setString(3, wechatReplyMsg.getContent());
            ps.setString(4, wechatReplyMsg.getId());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return AjaxResult.success();
    }

    @Override
    public void sendBase64ImgMsg(WechatMsg wechatMsg) {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String dir = "C:/screenshot/" + date + "/";

        File dirFile = new File(dir);
        if (dirFile.isDirectory() == false) {
            dirFile.mkdirs();
        }
        String suffix = wechatMsg.getNickname();
        if ("gif".equals(suffix)) {
            suffix = "gif";
        } else {
            suffix = "png";
        }
        String path = dir + new Date().getTime() + "_" + ((int) (Math.random() * 1000)) + "." + suffix;
        this.decryptByBase64(wechatMsg.getContent(), path);
        wechatMsg.setContent(path);
        wechatMsg.setType(PIC_MSG);
        ApplicationListener.BOT_CLIENT.sendMsgUtil(wechatMsg);
    }

    public void decryptByBase64(String base64, String filePath) {
        try {
            Files.write(Paths.get(filePath), Base64.getDecoder().decode(base64), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
