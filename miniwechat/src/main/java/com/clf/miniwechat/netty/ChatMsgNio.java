package com.clf.miniwechat.netty;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChatMsgNio implements Serializable {
    private static final long serialVersionUID = 1977173192352517065L;
    private String senderId;//发送者的用户id
    private String receiverId;//接受者的用户id
    private String msg;//聊天内容
    private String msgId;//用于消息的签收
}