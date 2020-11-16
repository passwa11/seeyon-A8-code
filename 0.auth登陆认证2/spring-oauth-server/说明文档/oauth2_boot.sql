/*
Navicat MySQL Data Transfer

Source Server         : a6v8sp1
Source Server Version : 50711
Source Host           : localhost:3306
Source Database       : oauth2_boot

Target Server Type    : MYSQL
Target Server Version : 50711
File Encoding         : 65001

Date: 2020-11-16 14:17:17
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for oauth_access_token
-- ----------------------------
DROP TABLE IF EXISTS `oauth_access_token`;
CREATE TABLE `oauth_access_token` (
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `token_id` varchar(255) DEFAULT NULL,
  `token` blob,
  `authentication_id` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  `authentication` blob,
  `refresh_token` varchar(255) DEFAULT NULL,
  UNIQUE KEY `authentication_id` (`authentication_id`),
  KEY `token_id_index` (`token_id`),
  KEY `authentication_id_index` (`authentication_id`),
  KEY `user_name_index` (`user_name`),
  KEY `client_id_index` (`client_id`),
  KEY `refresh_token_index` (`refresh_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of oauth_access_token
-- ----------------------------

-- ----------------------------
-- Table structure for oauth_client_details
-- ----------------------------
DROP TABLE IF EXISTS `oauth_client_details`;
CREATE TABLE `oauth_client_details` (
  `client_id` varchar(255) NOT NULL,
  `resource_ids` varchar(255) DEFAULT NULL,
  `client_secret` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `authorized_grant_types` varchar(255) DEFAULT NULL,
  `web_server_redirect_uri` varchar(255) DEFAULT NULL,
  `authorities` varchar(255) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` text,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `archived` tinyint(1) DEFAULT '0',
  `trusted` tinyint(1) DEFAULT '0',
  `autoapprove` varchar(255) DEFAULT 'false',
  `realsecret` varchar(255) DEFAULT NULL,
  `client_name` varchar(255) DEFAULT NULL,
  `realname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of oauth_client_details
-- ----------------------------
INSERT INTO `oauth_client_details` VALUES ('0091cc5cff0b4df8899b1c7c0ac05ac1', 'sos-resource', 'gg7bBKC52qspQzjV/rsq6z9RT74=', 'read', 'authorization_code', 'http://localhost:8081/client1/local', null, null, null, null, '2020-11-16 09:04:13', '0', '1', 'true', 'F1Sbgu80fhNwXEYd1RFGinZTPzt2R5w6', '测试获取人员信息', null);
INSERT INTO `oauth_client_details` VALUES ('918c92cd2b634a9bb256a9cc620b42bc', 'sos-resource', 'mC8lpXp4Kg5uE0kIynu7czpJSfY=', 'read', 'authorization_code', 'http://localhost:80/seeyon/kfqLogin', null, null, null, null, '2020-11-12 10:20:50', '0', '1', 'true', '6Syc9SEmPDXNxMGoCLicQ8iZzN8bTrsm', 'oa', null);

-- ----------------------------
-- Table structure for oauth_code
-- ----------------------------
DROP TABLE IF EXISTS `oauth_code`;
CREATE TABLE `oauth_code` (
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `code` varchar(255) DEFAULT NULL,
  `authentication` blob,
  KEY `code_index` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of oauth_code
-- ----------------------------

-- ----------------------------
-- Table structure for oauth_refresh_token
-- ----------------------------
DROP TABLE IF EXISTS `oauth_refresh_token`;
CREATE TABLE `oauth_refresh_token` (
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `token_id` varchar(255) DEFAULT NULL,
  `token` blob,
  `authentication` blob,
  KEY `token_id_index` (`token_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of oauth_refresh_token
-- ----------------------------

-- ----------------------------
-- Table structure for user_
-- ----------------------------
DROP TABLE IF EXISTS `user_`;
CREATE TABLE `user_` (
  `id` varchar(50) NOT NULL,
  `guid` varchar(255) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `archived` tinyint(1) DEFAULT '0',
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `default_user` tinyint(1) DEFAULT '0',
  `last_login_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `realsecret` varchar(255) DEFAULT NULL,
  `realname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `guid` (`guid`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_
-- ----------------------------
INSERT INTO `user_` VALUES ('7547111895052600818', '7547111895052600818', '2020-11-12 14:42:29', '0', null, 'dgPhRXPHVVyUPdQb+Z0/nY7vW58=', null, 'admin', '0', '2020-11-12 14:42:29', null, null);

-- ----------------------------
-- Table structure for user_privilege
-- ----------------------------
DROP TABLE IF EXISTS `user_privilege`;
CREATE TABLE `user_privilege` (
  `user_id` varchar(50) DEFAULT NULL,
  `privilege` varchar(255) DEFAULT NULL,
  KEY `user_id_index` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_privilege
-- ----------------------------
INSERT INTO `user_privilege` VALUES ('7547111895052600818', 'ADMIN');
