package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.MyFriends;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MyFriendsMapper {
    int deleteByPrimaryKey(String id);

    int insert(MyFriends record);

    MyFriends selectByPrimaryKey(String id);

    List<MyFriends> selectAll();

    int updateByPrimaryKey(MyFriends record);

    MyFriends selectByMyUserIdAndFriendId(@Param("myUserId") String myUserId,
                                          @Param("friendUserId") String friendUserId);
}