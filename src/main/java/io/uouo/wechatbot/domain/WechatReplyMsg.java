package io.uouo.wechatbot.domain;

/**
 * @author: [青衫] 'QSSSYH@QQ.com'
 * @Date: 2021-03-16 18:48
 * @Description: < 描述 >
 */
public class WechatReplyMsg {
    private String token;
    private String id;
    private String content;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isValid() {
        return WechatMsg._token.equals(this.getToken());
    }
}
