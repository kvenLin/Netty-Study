package com.clf.miniwechat.service;

import com.clf.miniwechat.domain.ChatMsg;
import com.clf.miniwechat.domain.Users;
import com.clf.miniwechat.netty.ChatMsgNio;
import com.clf.miniwechat.vo.FriendRequestVO;
import com.clf.miniwechat.vo.MyFriendsVO;

import java.util.List;

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
    Users saveUser(Users user);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    Users updateUserInfo(Users user);

    /**
     * 搜索好友前置条件
     * @param myUserId
     * @param friendUsername
     * @return
     */
    Integer preSearchFriends(String myUserId, String friendUsername);

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    Users queryUserInfoByUsername(String username);

    /**
     * 添加好友请求记录保存到数据库
     * @param myUserId
     * @param friendUsername
     */
    void sendFriendRequest(String myUserId, String friendUsername);

    /**
     * 查询好友请求
     * @param acceptUserId
     * @return
     */
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    /**
     * 删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 通过好友请求
     * @param sendUserId
     * @param acceptUserId
     */
    void passFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 查询好友列表
     * @param userId
     * @return
     */
    List<MyFriendsVO> queryFriends(String userId);

    /**
     * 保存聊天消息到数据库
     * @param chatMsgNio
     * @return
     */
    String saveMsg(ChatMsgNio chatMsgNio);

    /**
     * 批量签收消息
     * @param msgIdList
     */
    void updateMsgSigned(List<String> msgIdList);

    /**
     * 获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    List<ChatMsg> getUnReadMsgList(String acceptUserId);
}
