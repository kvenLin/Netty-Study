package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.MyFriends;
import java.util.List;

public interface MyFriendsMapper {
    int deleteByPrimaryKey(String id);

    int insert(MyFriends record);

    MyFriends selectByPrimaryKey(String id);

    List<MyFriends> selectAll();

    int updateByPrimaryKey(MyFriends record);
}