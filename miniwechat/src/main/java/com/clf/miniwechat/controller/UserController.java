package com.clf.miniwechat.controller;

import com.clf.miniwechat.domain.Users;
import com.clf.miniwechat.service.UserService;
import com.clf.miniwechat.utils.MD5Utils;
import com.clf.miniwechat.utils.MyJSONResult;
import com.clf.miniwechat.vo.UsersVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: clf
 * @Date: 2020-02-08
 * @Description: TODO
 */
@RestController
@RequestMapping("u")
public class UserController {

    @Autowired
    private UserService userService;

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
}
