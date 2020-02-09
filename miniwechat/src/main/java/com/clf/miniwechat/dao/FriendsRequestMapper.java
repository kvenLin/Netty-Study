package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.FriendsRequest;
import java.util.List;

public interface FriendsRequestMapper {
    int deleteByPrimaryKey(String id);

    int insert(FriendsRequest record);

    FriendsRequest selectByPrimaryKey(String id);

    List<FriendsRequest> selectAll();

    int updateByPrimaryKey(FriendsRequest record);
}