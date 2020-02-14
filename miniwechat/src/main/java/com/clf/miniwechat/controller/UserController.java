package com.clf.miniwechat.controller;

import com.clf.miniwechat.bo.UsersBO;
import com.clf.miniwechat.domain.Users;
import com.clf.miniwechat.enums.OperatorFriendRequestTypeEnum;
import com.clf.miniwechat.enums.SearchFriendsStatusEnum;
import com.clf.miniwechat.service.UserService;
import com.clf.miniwechat.utils.FastDFSClient;
import com.clf.miniwechat.utils.FileUtils;
import com.clf.miniwechat.utils.MD5Utils;
import com.clf.miniwechat.utils.MyJSONResult;
import com.clf.miniwechat.vo.MyFriendsVO;
import com.clf.miniwechat.vo.UsersVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: clf
 * @Date: 2020-02-08
 * @Description: TODO
 */
@RestController
@RequestMapping("u")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Value("${tmpFilePath}")
    private String tmpFilePath;
    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("/registerOrLogin")
    public MyJSONResult registerOrLogin(@RequestBody Users user) throws Exception {
        //判断用户名密码不能为空
        if(StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            return MyJSONResult.errorMsg("用户名或密码不能为空...");
        }
        Users userResult = null;
        if (userService.queryUsernameIsExist(user.getUsername())) {
            //登录
            userResult = userService.queryUserForLogin(user.getUsername(),
                    MD5Utils.getMD5Str(user.getPassword()));
            if(userResult == null) {
                return MyJSONResult.errorMsg("用户名或密码不正确...");
            }
        } else {
            //注册
            userResult = userService.saveUser(user);
        }
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);
        return MyJSONResult.ok(userVO);
    }

    @PostMapping("/uploadFaceBase64")
    public MyJSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception {
        //TODO,图片太大导致上传失败的错误提示
        //获取前端传过来的base64字符串,然后转换为文件对象再上传
        String base64Data = usersBO.getFaceData();
        String userFacePath = tmpFilePath + usersBO.getUserId() + "userFace.png";
        FileUtils.base64ToFile(userFacePath, usersBO.getFaceData());

        //上传文件到fastDFS
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        log.warn(url);

        //获取缩略图的url
        String thump = "_80x80.";
        String arr[] = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        //更新用户头像
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        user = userService.updateUserInfo(user);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        return MyJSONResult.ok(usersVO);
    }

    @PostMapping("/setNickname")
    public MyJSONResult setNickname(@RequestBody UsersBO usersBO) {
        if(StringUtils.isEmpty(usersBO.getNickname())) {
            return MyJSONResult.errorMsg("昵称不能为空");
        }
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickname());
        user = userService.updateUserInfo(user);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        return MyJSONResult.ok(usersVO);
    }

    /**
     * 搜索好友接口,根据账号做匹配查找
     * @param myUserId
     * @param friendUsername
     * @return
     */
    @PostMapping("/search")
    public MyJSONResult searchUser(String myUserId, String friendUsername) {
        if(StringUtils.isEmpty(myUserId) || StringUtils.isEmpty(friendUsername)) {
            return MyJSONResult.errorMsg("参数不能为空");
        }
        Integer status = userService.preSearchFriends(myUserId, friendUsername);
        if(SearchFriendsStatusEnum.SUCCESS.status == status) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return MyJSONResult.ok(userVO);
        } else {
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        }
    }

    @PostMapping("/addFriendRequest")
    public MyJSONResult addFriendRequest(String myUserId, String friendUsername) {
        if(StringUtils.isEmpty(myUserId) || StringUtils.isEmpty(friendUsername)) {
            return MyJSONResult.errorMsg("参数不能为空");
        }
        Integer status = userService.preSearchFriends(myUserId, friendUsername);
        if(SearchFriendsStatusEnum.SUCCESS.status == status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        }else {
            return MyJSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
        }
        return MyJSONResult.ok();
    }

    /**
     * 查询用户的好友申请
     * @param userId
     * @return
     */
    @PostMapping("/queryFriendRequests")
    public MyJSONResult queryFriendRequests(String userId) {
        if(StringUtils.isEmpty(userId)) {
            return MyJSONResult.errorMsg("用户不存在");
        }
        return MyJSONResult.ok(userService.queryFriendRequestList(userId));
    }

    /**
     * 对好友请求进行处理
     * @param acceptUserId
     * @param sendUserId
     * @param operType
     * @return
     */
    @PostMapping("/operFriendRequest")
    public MyJSONResult operFriendRequest(String acceptUserId, 
                                          String sendUserId,
                                          Integer operType) {
        if(StringUtils.isEmpty(acceptUserId) ||
                StringUtils.isEmpty(sendUserId) ||
                operType == null) {
            return MyJSONResult.errorMsg("参数不能为空");
        }
        if(StringUtils.isEmpty(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return MyJSONResult.errorMsg("操作类型不能为空");
        }

        if(OperatorFriendRequestTypeEnum.IGNORE.getType() == operType) {
            //1.忽略好友请求,删除请求记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if(OperatorFriendRequestTypeEnum.PASS.getType() == operType) {
            //2.通过请求,则增加好友关系,然后删除好友请求记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        //查询好友列表
        List<MyFriendsVO> myFriends = userService.queryFriends(acceptUserId);
        return MyJSONResult.ok(myFriends);
    }

    /**
     * 查询我的好友列表
     * @param userId
     * @return
     */
    @PostMapping("/myFriends")
    public MyJSONResult myFriends(String userId) {
        if(StringUtils.isEmpty(userId)) {
            return MyJSONResult.errorMsg("用户id不能为空");
        }
        List<MyFriendsVO> myFriends = userService.queryFriends(userId);
        return MyJSONResult.ok(myFriends);
    }
}
