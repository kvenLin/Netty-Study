package com.clf.miniwechat.dao;

import com.clf.miniwechat.domain.Users;
import java.util.List;

public interface UsersMapper {
    int deleteByPrimaryKey(String id);

    int insert(Users record);

    Users selectByPrimaryKey(String id);

    List<Users> selectAll();

    Users selectOne(String username);

    int updateByPrimaryKey(Users record);

}