BEGIN;

DROP TABLE IF EXISTS enkan_config;
DROP TABLE IF EXISTS enkan_task;

CREATE TABLE `enkan_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `namespace` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '___DEFAULT___',
  `config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_namespace_key` (`namespace`,`config_key`),
  KEY `key_updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `enkan_task` (
  `tid` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务id',
  `url` varchar(767) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '推文URL',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '推文内容',
  `media` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '媒体地址',
  `published` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否已发布',
  `hided` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  `comment` varchar(1023) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`tid`),
  UNIQUE KEY `key_url` (`url`) USING BTREE,
  KEY `key_newdate` (`newdate`),
  KEY `key_updatetime` (`updatetime`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `enkan_translate` (
  `zzid` int(11) NOT NULL AUTO_INCREMENT,
  `tid` int(11) NOT NULL COMMENT '推文id',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  `translation` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '翻译内容',
  `img` varchar(2047) COLLATE utf8mb4_general_ci NOT NULL COMMENT '烤推机生成的图地址',
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`zzid`),
  UNIQUE KEY `key_tid_version` (`tid`,`version`),
  KEY `key_updatetime` (`updatetime`),
  KEY `key_newdate` (`newdate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

COMMIT;
