package com.clf.miniwechat.netty;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: clf
 * @Date: 2020-02-13
 * @Description: TODO
 */
@Data
public class DataContent implements Serializable {
    private static final long serialVersionUID = 104423417395945310L;

    private Integer action; //动作类型
    private ChatMsgNio chatMsgNio; //用户的聊天内容
    private String extend;//扩展字段
}
