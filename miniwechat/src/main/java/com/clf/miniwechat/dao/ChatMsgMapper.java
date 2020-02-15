package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.ChatMsg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    List<ChatMsg> selectAll();

    int updateByPrimaryKey(ChatMsg record);

    void batchUpdateMsgSigned(List<String> msgIdList);

    List<ChatMsg> selectByAcceptUserIdAndSignType(@Param("acceptUserId") String acceptUserId, @Param("type") Integer type);
}