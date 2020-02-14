package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.ChatMsg;
import java.util.List;

public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    List<ChatMsg> selectAll();

    int updateByPrimaryKey(ChatMsg record);

    void batchUpdateMsgSigned(List<String> msgIdList);
}