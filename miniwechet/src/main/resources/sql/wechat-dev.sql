/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50728
Source Host           : localhost:3306
Source Database       : wechat-dev

Target Server Type    : MYSQL
Target Server Version : 50728
File Encoding         : 65001

Date: 2020-02-07 15:10:06
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `chat_msg`
-- ----------------------------
DROP TABLE IF EXISTS `chat_msg`;
CREATE TABLE `chat_msg` (
  `id` varchar(64) NOT NULL,
  `send_user_id` varchar(64) NOT NULL,
  `accept_user_id` varchar(64) NOT NULL,
  `msg` varchar(255) NOT NULL,
  `sign_flag` int(1) NOT NULL COMMENT '签收的状态(未读,已读)',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chat_msg
-- ----------------------------

-- ----------------------------
-- Table structure for `friends_request`
-- ----------------------------
DROP TABLE IF EXISTS `friends_request`;
CREATE TABLE `friends_request` (
  `id` varchar(64) NOT NULL,
  `send_user_id` varchar(64) NOT NULL,
  `accept_user_id` varchar(64) NOT NULL,
  `request_date_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of friends_request
-- ----------------------------

-- ----------------------------
-- Table structure for `my_friends`
-- ----------------------------
DROP TABLE IF EXISTS `my_friends`;
CREATE TABLE `my_friends` (
  `id` varchar(64) NOT NULL,
  `my_user_id` varchar(64) NOT NULL,
  `my_friend_user_id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of my_friends
-- ----------------------------

-- ----------------------------
-- Table structure for `users`
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` varchar(64) NOT NULL,
  `username` varchar(20) NOT NULL,
  `password` varchar(64) NOT NULL,
  `face_image` varchar(255) NOT NULL,
  `face_iamge_big` varchar(255) NOT NULL,
  `nickname` varchar(20) NOT NULL,
  `qrcode` varchar(255) NOT NULL,
  `cid` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of users
-- ----------------------------
