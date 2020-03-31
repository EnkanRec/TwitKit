BEGIN;

DROP TABLE IF EXISTS enkan_config;
DROP TABLE IF EXISTS enkan_task;
DROP TABLE IF EXISTS enkan_translate;
DROP TABLE IF EXISTS enkan_twitter;

CREATE TABLE `enkan_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `namespace` varchar(191) NOT NULL DEFAULT '___DEFAULT___',
  `config_key` varchar(191) NOT NULL,
  `config_value` text,
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_namespace_key` (`namespace`,`config_key`),
  KEY `key_updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enkan_task` (
  `tid` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `status_id` varchar(191) NOT NULL COMMENT '推特生成的推文唯一id',
  `url` varchar(767) NOT NULL COMMENT '推文URL',
  `content` longtext NOT NULL COMMENT '推文内容',
  `media` text NOT NULL COMMENT '媒体地址',
  `published` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否已发布',
  `hided` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  `comment` varchar(1023) NOT NULL DEFAULT '' COMMENT '备注',
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `twitter_uid` varchar(191) DEFAULT NULL COMMENT '指向该推文主动用户',
  `ref_tid` int(11) DEFAULT NULL COMMENT '指向转发/引用的推文id\n',
  `pub_date` datetime DEFAULT NULL COMMENT 'API返回的发推时间',
  `extra` longtext COMMENT 'json储存动态字段，任意扩展，不需要索引的东西都可以丢进来\n\n\n\n',
  PRIMARY KEY (`tid`),
  UNIQUE KEY `key_uid_statusid` (`twitter_uid`,`status_id`) USING BTREE,
  KEY `key_newdate` (`newdate`),
  KEY `key_updatetime` (`updatetime`),
  KEY `key_url` (`url`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enkan_translate` (
  `zzid` int(11) NOT NULL AUTO_INCREMENT,
  `tid` int(11) NOT NULL COMMENT '推文id',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  `translation` longtext NOT NULL COMMENT '翻译内容',
  `img` varchar(2047) NOT NULL COMMENT '烤推机生成的图地址',
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`zzid`),
  UNIQUE KEY `key_tid_version` (`tid`,`version`),
  KEY `key_updatetime` (`updatetime`),
  KEY `key_newdate` (`newdate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enkan_twitter` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `twitter_uid` varchar(191) NOT NULL,
  `name` varchar(511) NOT NULL,
  `display` varchar(511) NOT NULL,
  `avatar` varchar(2047) NOT NULL,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `key_twitteruid` (`twitter_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `enkan_config` VALUES (1, '___DEFAULT___', 'test.default.yachiyo.love', 'iroha', '2020-02-07 00:12:56', '2020-02-07 00:12:56');
INSERT INTO `enkan_config` VALUES (2, '___DEFAULT___', 'test.default.rika', '五十铃怜', '2020-02-07 00:13:19', '2020-02-07 00:13:19');
INSERT INTO `enkan_config` VALUES (3, '___TEST_MB4___', 'test.mb4.emoji', '❤①+123AB', '2020-02-07 00:26:48', '2020-02-07 00:29:46');

INSERT INTO `enkan_task` VALUES (1000, '10000', 'URL_0', '内容0', '[]', 0, 0, '', '2020-02-07 00:02:21', '2020-03-01 12:27:41', '123000123', NULL, NULL, NULL);
INSERT INTO `enkan_task` VALUES (1001, '10001', 'URL_1', '内容1', '[]', 0, 0, '', '2020-02-07 00:02:41', '2020-03-01 12:27:42', '123000123', NULL, NULL, NULL);
INSERT INTO `enkan_task` VALUES (1002, '10002', 'URL_2', '内容2🍒', '[\"media_2\"]', 1, 0, '', '2020-02-07 16:12:41', '2020-03-01 12:27:43', '123000123', NULL, NULL, NULL);
INSERT INTO `enkan_task` VALUES (1003, '10003', 'URL_3', '内容3', '[]', 1, 0, '', '2020-02-07 16:59:03', '2020-03-01 12:27:43', '123000123', NULL, NULL, NULL);
INSERT INTO `enkan_task` VALUES (1004, '10004', 'URL_4_HIDED', '内容4', '[]', 0, 1, '', '2020-02-07 17:20:58', '2020-03-01 12:27:46', '123000123', NULL, NULL, NULL);

INSERT INTO `enkan_translate` VALUES (1, 1000, 0, '翻译A1', '[img1]', '2020-02-07 00:07:56', '2020-02-07 00:07:56');
INSERT INTO `enkan_translate` VALUES (3, 1000, 1, '翻译A2', '[img2]', '2020-02-07 00:08:18', '2020-02-07 00:08:47');
INSERT INTO `enkan_translate` VALUES (4, 1000, 2, '翻译A3', '[img3]', '2020-02-07 00:08:46', '2020-02-07 00:08:46');
INSERT INTO `enkan_translate` VALUES (5, 1001, 0, '翻译B1', '[img_21]', '2020-02-07 00:10:30', '2020-02-07 00:10:30');
INSERT INTO `enkan_translate` VALUES (6, 1001, 1, '翻译B2', '[img_22]', '2020-02-07 00:10:57', '2020-02-07 00:10:57');
INSERT INTO `enkan_translate` VALUES (8, 1002, 0, '翻译C1', '[img_31]', '2020-02-07 17:21:34', '2020-02-07 17:21:34');

INSERT INTO `enkan_twitter` VALUES (4, '123000123', 'enkanRecGLZ', '圆环记录攻略组', 'http://example.org/111.jpg', '2020-03-01 12:42:25', '2020-03-01 12:08:29');

COMMIT;
