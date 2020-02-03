DROP TABLE IF EXISTS enkan_config;
DROP TABLE IF EXISTS enkan_task;

CREATE TABLE `enkan_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `namespace` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '___DEFAULT___',
  `config_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_namespace_key` (`namespace`,`config_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `enkan_task` (
  `tid` int(11) NOT NULL AUTO_INCREMENT,
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号',
  `url` varchar(767) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '推文URL',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '推文内容',
  `media` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '媒体地址',
  `published` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否已发布',
  `comment` varchar(1023) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  `newdate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`tid`),
  UNIQUE KEY `key_url_version` (`url`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
