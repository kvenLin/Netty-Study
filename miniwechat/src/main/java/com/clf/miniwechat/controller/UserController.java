package com.clf.miniwechat.controller;

import com.clf.miniwechat.bo.UsersBO;
import com.clf.miniwechat.domain.Users;
import com.clf.miniwechat.service.UserService;
import com.clf.miniwechat.utils.FastDFSClient;
import com.clf.miniwechat.utils.FileUtils;
import com.clf.miniwechat.utils.MD5Utils;
import com.clf.miniwechat.utils.MyJSONResult;
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
        log.error("test...");
        //获取前端传过来的base64字符串,然后转换为文件对象再上传
        String base64Data = usersBO.getFaceData();
        String userFacePath = tmpFilePath + usersBO.getUserId() + "userFace.png";
        FileUtils.base64ToFile(userFacePath, usersBO.getFaceData());

        //上传文件到fastDFS
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        log.warn(url);

        //获取缩略图的url
        String thump = "_8080.";
        String arr[] = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        //更新用户头像
        Users user = new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        user = userService.updateUserInfo(user);
        return MyJSONResult.ok(user);
    }
}
