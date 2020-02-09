package com.clf.miniwechat.service;

import com.clf.miniwechat.domain.Users;

/**
 * @Author: clf
 * @Date: 2020-02-08
 * @Description: TODO
 */
public interface UserService {
    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    boolean queryUsernameIsExist(String username);

    /**
     * 查询
     * @param username
     * @param password
     * @return
     */
    Users queryUserForLogin(String username, String password);

    /**
     * 用户注册
     * @param user
     * @return
     */
    Users saveUser(Users user) throws Exception;
}
