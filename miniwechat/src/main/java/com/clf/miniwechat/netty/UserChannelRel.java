package com.clf.miniwechat.netty;

import io.netty.channel.Channel;

import java.util.HashMap;

/**
 * @Author: clf
 * @Date: 2020-02-13
 * @Description: 用户id和channel的关联关系处理
 */
public class UserChannelRel {
    private static HashMap<String, Channel> manager = new HashMap<>();

    public static void put(String senderId, Channel channel) {
        manager.put(senderId, channel);
    }

    public static Channel get(String senderId) {
        return manager.get(senderId);
    }
}
