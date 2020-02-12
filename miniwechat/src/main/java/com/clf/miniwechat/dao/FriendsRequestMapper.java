package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.FriendsRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FriendsRequestMapper {
    int deleteByPrimaryKey(String id);

    int insert(FriendsRequest record);

    FriendsRequest selectByPrimaryKey(String id);

    List<FriendsRequest> selectAll();

    int updateByPrimaryKey(FriendsRequest record);

    FriendsRequest selectBySendUserIdAndAcceptUserId(@Param("sendUserId") String sendUserId,
                                                     @Param("acceptUserId") String acceptUserId);
}