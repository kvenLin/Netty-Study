package com.clf.miniwechat.service.impl;

import com.clf.miniwechat.dao.UsersMapper;
import com.clf.miniwechat.domain.Users;
import com.clf.miniwechat.service.UserService;
import com.clf.miniwechat.utils.MD5Utils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: clf
 * @Date: 2020-02-08
 * @Description: TODO
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users result = usersMapper.selectOne(username);
        return result != null ? true : false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {
        Users users = usersMapper.selectOne(username);
        if(users != null) {
            if (users.getPassword().equals(password)) {
                return users;
            } else {
                return null;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) throws Exception {
        //注册
        user.setId(sid.nextShort());
        user.setNickname(user.getUsername());
        //TODO 为每个用户生成一个唯一的二维码
        user.setQrcode("");
        user.setFaceImage("");
        user.setFaceImageBig("");
        user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
        usersMapper.insert(user);
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKey(user);
        return queryUserById(user.getId());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserById(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }
}
