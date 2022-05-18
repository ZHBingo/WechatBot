package io.uouo.wechatbot.controller;

import io.uouo.wechatbot.common.util.AjaxResult;
import io.uouo.wechatbot.domain.WechatMsg;
import io.uouo.wechatbot.domain.WechatReplyMsg;
import io.uouo.wechatbot.service.WechatBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-16 19:52
 * @Description: < 描述 >
 * @Document <a href="https://docs.apipost.cn/view/94356b050fc22d34">...</a>
 */
@RestController
public class WechatBotController {

    @Autowired
    private WechatBotService wechatBotService;

    @GetMapping("/hello")
    public Map<String, Object> Hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("time", new Date().getTime());
        return result;
    }


    /**
     * 描述: 通用请求接口
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    // @PostMapping("/wechatCommon")
    public AjaxResult wechatCommon(@RequestBody WechatMsg wechatMsg) {
        wechatBotService.wechatCommon(wechatMsg);
        return AjaxResult.success();
    }


    /**
     * 描述: 发送文本消息
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    @PostMapping("/send_text_msg")
    public AjaxResult sendTextMsg(@RequestBody WechatMsg wechatMsg) {
        if (wechatMsg.isValid()) {
            wechatBotService.sendTextMsg(wechatMsg);
            return AjaxResult.success();
        } else {
            return AjaxResult.error(null);
        }
    }

    /**
     * 描述: 发送图片消息
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    @PostMapping("/send_img_msg")
    public AjaxResult sendImgMsg(@RequestBody WechatMsg wechatMsg) {
        // 发送消息
        if (wechatMsg.isValid()) {
            wechatBotService.sendImgMsg(wechatMsg);
            return AjaxResult.success();
        } else {
            return AjaxResult.error(null);
        }
    }

    /**
     * 描述: 群组内发送@指定人消息(dll 3.1.0.66版本不可用)
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    @PostMapping("/send_at_msg")
    public AjaxResult sendATMsg(@RequestBody WechatMsg wechatMsg) {
        if (wechatMsg.isValid()) {
            wechatBotService.sendATMsg(wechatMsg);
            return AjaxResult.success();
        } else {
            return AjaxResult.error(null);
        }
    }

    /**
     * 描述: 发送附件
     *
     * @param wechatMsg
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-26
     */
    @PostMapping("/send_annex")
    public AjaxResult sendAnnex(@RequestBody WechatMsg wechatMsg) {
        if (wechatMsg.isValid()) {
            wechatBotService.sendAnnex(wechatMsg);
            return AjaxResult.success();
        } else {
            return AjaxResult.error(null);
        }
    }

    /**
     * 获取待回复的消息内容
     */
    @PostMapping("/get_unreply_message")
    public List<Map<String, String>> getUnReplyMessage(@RequestBody WechatMsg wechatMsg) {
        if (wechatMsg.isValid()) {
            return wechatBotService.getUnReplyMessage();
        } else {
            return new ArrayList<>();
        }
    }

    @PostMapping("/reply_message")
    public AjaxResult replMessage(@RequestBody WechatReplyMsg wechatReplyMsg) {
        if (wechatReplyMsg.isValid()) {
            return wechatBotService.replMessage(wechatReplyMsg);
        } else {
            return AjaxResult.error(null);
        }
    }

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 获取信息 ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    /**
     * 描述: 获取微信群组,联系人列表
     *
     * @param
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-29
     */
    // @GetMapping("/getWeChatUserList")
    public AjaxResult getWeChatUserList() {
        wechatBotService.getWeChatUserList();
        return AjaxResult.success();
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
    // @GetMapping("/getChatroomMemberNick/{roomid}/{wxid}")
    public AjaxResult getChatroomMemberNick(@PathVariable("roomid") String roomid, @PathVariable("wxid") String wxid) {
        wechatBotService.getChatroomMemberNick(roomid, wxid);
        return AjaxResult.success();
    }

    /**
     * 描述: 获取个人详细信息 3.2.2.121版本dll 未提供该接口
     *
     * @param
     * @return io.uouo.wechatbot.common.util.AjaxResult
     * @Author 青衫 [2940500@qq.com]
     * @Date 2021-3-29
     */
    // @GetMapping("/getPersonalDetail/{wxid}")
    public AjaxResult getPersonalDetail(@PathVariable("wxid") String wxid) {
        wechatBotService.getPersonalDetail(wxid);
        return AjaxResult.success();
    }
}
