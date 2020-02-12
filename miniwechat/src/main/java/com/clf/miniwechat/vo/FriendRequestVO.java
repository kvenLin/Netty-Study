package com.clf.miniwechat.vo;

import lombok.Data;

@Data
public class FriendRequestVO {
    private String sendUserId;
    private String sendUsername;
    private String sendNickname;
    private String sendFaceImage;
}