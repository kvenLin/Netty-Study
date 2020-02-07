package com.clf.miniwechet.dao;

import com.clf.miniwechet.domain.ChatMsg;
import java.util.List;

public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    List<ChatMsg> selectAll();

    int updateByPrimaryKey(ChatMsg record);
}