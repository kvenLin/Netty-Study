package com.clf.miniwechet.dao;

import com.clf.miniwechet.domain.users;
import java.util.List;

public interface usersMapper {
    int deleteByPrimaryKey(String id);

    int insert(users record);

    users selectByPrimaryKey(String id);

    List<users> selectAll();

    int updateByPrimaryKey(users record);
}