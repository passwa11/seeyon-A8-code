/*
Navicat Oracle Data Transfer
Oracle Client Version : 11.2.0.1.0

Source Server         : 7.1sp1
Source Server Version : 110200
Source Host           : 127.0.0.1:1521
Source Schema         : A871SP1

Target Server Type    : ORACLE
Target Server Version : 110200
File Encoding         : 65001

Date: 2019-12-10 17:30:19
*/


-- ----------------------------
-- Table structure for Z_SURE_LOGIN
-- ----------------------------
DROP TABLE "A871SP1"."Z_SURE_LOGIN";
CREATE TABLE "A871SP1"."Z_SURE_LOGIN" (
"ID" NUMBER NOT NULL ,
"LOGINNAME" VARCHAR2(255 BYTE) NULL ,
"PASSWORD" VARCHAR2(255 BYTE) NULL ,
"ANSWER1" VARCHAR2(255 BYTE) NULL ,
"ANSWER2" VARCHAR2(255 BYTE) NULL ,
"ANSWER3" VARCHAR2(255 BYTE) NULL 
)
LOGGING
NOCOMPRESS
NOCACHE

;

-- ----------------------------
-- Indexes structure for table Z_SURE_LOGIN
-- ----------------------------

-- ----------------------------
-- Checks structure for table Z_SURE_LOGIN
-- ----------------------------
ALTER TABLE "A871SP1"."Z_SURE_LOGIN" ADD CHECK ("ID" IS NOT NULL);

-- ----------------------------
-- Primary Key structure for table Z_SURE_LOGIN
-- ----------------------------
ALTER TABLE "A871SP1"."Z_SURE_LOGIN" ADD PRIMARY KEY ("ID");
