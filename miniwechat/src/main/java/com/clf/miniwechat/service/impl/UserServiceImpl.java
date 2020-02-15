package com.clf.miniwechat.service.impl;

import com.clf.miniwechat.dao.ChatMsgMapper;
import com.clf.miniwechat.dao.FriendsRequestMapper;
import com.clf.miniwechat.dao.MyFriendsMapper;
import com.clf.miniwechat.dao.UsersMapper;
import com.clf.miniwechat.domain.ChatMsg;
import com.clf.miniwechat.domain.FriendsRequest;
import com.clf.miniwechat.domain.MyFriends;
import com.clf.miniwechat.domain.Users;
import com.clf.miniwechat.enums.MsgActionEnum;
import com.clf.miniwechat.enums.MsgSignFlagEnum;
import com.clf.miniwechat.enums.SearchFriendsStatusEnum;
import com.clf.miniwechat.netty.ChatMsgNio;
import com.clf.miniwechat.netty.DataContent;
import com.clf.miniwechat.netty.UserChannelRel;
import com.clf.miniwechat.service.UserService;
import com.clf.miniwechat.utils.*;
import com.clf.miniwechat.vo.FriendRequestVO;
import com.clf.miniwechat.vo.MyFriendsVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @Author: clf
 * @Date: 2020-02-08
 * @Description: TODO
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private QRCodeUtils qrCodeUtils;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private MyFriendsMapper myFriendsMapper;
    @Autowired
    private FriendsRequestMapper friendsRequestMapper;
    @Autowired
    private ChatMsgMapper chatMsgMapper;
    @Value("${tmpFilePath}")
    private String tmpFilePath;


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
    public Users saveUser(Users user) {
        //注册
        user.setId(sid.nextShort());
        user.setNickname(user.getUsername());
        //为每个用户生成一个唯一的二维码

        String qrCodePath = tmpFilePath + user.getId() + "qrcode.png";
        // miniwechat_qrcode:[username]
        qrCodeUtils.createQRCode(qrCodePath, "miniwechat_qrcode:" + user.getUsername());
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        } catch (IOException e) {
            //todo
        }
        user.setQrcode(qrCodeUrl);
        user.setFaceImage("");
        user.setFaceImageBig("");
        try {
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
        } catch (Exception e) {
            //todo
        }
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
    @Override
    public Integer preSearchFriends(String myUserId, String friendUsername) {
        //1.用户不存在,返回[用户不存在]
        Users user = queryUserInfoByUsername(friendUsername);
        if(user == null) {
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //2.搜索用户是自己,返回[不能添加自己]
        if(user.getId().equals(myUserId)) {
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //3.搜索账号已经是好友,返回[该用户已经是你的好友]
        MyFriends myFriends = myFriendsMapper.selectByMyUserIdAndFriendId(myUserId, user.getId());
        if(myFriends != null) {
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public Users queryUserInfoByUsername(String username) {
        return usersMapper.selectOne(username);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {
        //根据用户名获取朋友的信息
        Users friend = queryUserInfoByUsername(friendUsername);
        if(friend == null) {
            //TODO,抛异常处理
            log.error("friend is null");
        }

        //1.查询发送好友请求记录表
        FriendsRequest friendRequest = friendsRequestMapper.
                selectBySendUserIdAndAcceptUserId(myUserId, friend.getId());
        if(friendRequest == null) {
            //2.即该用户还不是你的好友,才进行添加操作
            String requestId = sid.nextShort();
            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapper.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        friendsRequestMapper.deleteBySendUserIdAndAcceptUserId(sendUserId, acceptUserId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        //1.保存好友
        saveFriends(sendUserId, acceptUserId);
        //2.逆向保存好友
        saveFriends(acceptUserId, sendUserId);
        //3.删除好友请求记录
        deleteFriendRequest(sendUserId, acceptUserId);

        Channel channel = UserChannelRel.get(sendUserId);
        if(channel != null) {
            //使用websocket主动推送消息到请求发起者, 更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            channel.writeAndFlush(
                    new TextWebSocketFrame(
                            JsonUtils.objectToJson(dataContent)));
        }
    }

    @Transactional(propagation =  Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryFriends(String userId) {
        return usersMapper.queryMyFriends(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsgNio chatMsgNio) {
        ChatMsg msg = new ChatMsg();
        msg.setId(sid.nextShort());
        msg.setSendUserId(chatMsgNio.getSenderId());
        msg.setAcceptUserId(chatMsgNio.getReceiverId());
        msg.setMsg(chatMsgNio.getMsg());
        msg.setCreateTime(new Date());
        msg.setSignFlag(MsgSignFlagEnum.unsign.type);
        chatMsgMapper.insert(msg);
        return msg.getId();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        chatMsgMapper.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ChatMsg> getUnReadMsgList(String acceptUserId) {
        return chatMsgMapper.selectByAcceptUserIdAndSignType(acceptUserId, MsgSignFlagEnum.unsign.type);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFriends(String sendUserId, String acceptUserId) {
        MyFriends myFriends = new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriendsMapper.insert(myFriends);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Users queryUserById(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }
}
