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

COMMIT;
