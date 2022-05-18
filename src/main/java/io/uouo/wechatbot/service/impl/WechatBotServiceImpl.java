package io.uouo.wechatbot.service.impl;

import com.alibaba.druid.pool.DruidDataSource;
import io.uouo.wechatbot.client.WechatBotClient;
import io.uouo.wechatbot.common.WechatBotCommon;
import io.uouo.wechatbot.domain.WechatMsg;
import io.uouo.wechatbot.service.WechatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-18 20:55
 * @Description: <  >
 */
@Service
public class WechatBotServiceImpl implements WechatBotService, WechatBotCommon {


    /**
     * 注入微信客户端
     */
    @Resource
    private WechatBotClient wechatBotClient;

    @Resource
    private DruidDataSource dataSource;

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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
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
        wechatBotClient.sendMsgUtil(wechatMsg);
    }

    @Override
    public List<Map<String, String>> getUnReplyMessage() {
        List<Map<String, String>> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("select id, content from message where reply_time is null");
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
}
