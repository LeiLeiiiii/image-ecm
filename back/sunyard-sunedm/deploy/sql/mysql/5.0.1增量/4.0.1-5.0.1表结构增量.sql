-- ----------------------------
-- Table structure for doc_bs_collection
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_collection`;
CREATE TABLE `doc_bs_collection`  (
  `collection_id` bigint NOT NULL COMMENT '收藏id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `doc_id` bigint NOT NULL COMMENT '文档id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`collection_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_doc_flow
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_doc_flow`;
CREATE TABLE `doc_bs_doc_flow`  (
  `flow_id` bigint NOT NULL COMMENT '主键id',
  `doc_id` bigint NOT NULL COMMENT '关联文档id',
  `flow_type` int NULL DEFAULT NULL COMMENT '流转名称  1:上传，2:上传审核，3:修改，4:下架，5:重新上架',
  `flow_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转描述',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人id',
  `flow_date` datetime NULL DEFAULT NULL COMMENT '流转时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`flow_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文档流转表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_doc_rel
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_doc_rel`;
CREATE TABLE `doc_bs_doc_rel`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `doc_id` bigint NULL DEFAULT NULL COMMENT '关联文档id',
  `rel_id` bigint NULL DEFAULT NULL COMMENT '关联id（文档id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `doc_id`(`doc_id`) USING BTREE,
  INDEX `rel_id`(`rel_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文档关联的文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_document
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_document`;
CREATE TABLE `doc_bs_document`  (
  `bus_id` bigint NOT NULL COMMENT '主键id',
  `house_id` bigint NULL DEFAULT NULL COMMENT '所属文档库id',
  `doc_seq` bigint NULL DEFAULT NULL COMMENT '顺序号',
  `type` int NULL DEFAULT NULL COMMENT '类型：0:文件夹；1:文档；2:附件',
  `folder_level` int NULL DEFAULT NULL COMMENT '层级',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '直接父级id',
  `doc_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件夹or文档名称',
  `doc_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件or文档描述',
  `rel_doc` bigint NULL DEFAULT NULL COMMENT '附件关联文档id',
  `doc_suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件后缀，例子：.doc/.pdf',
  `doc_size` bigint NULL DEFAULT NULL COMMENT '文件夹或者文档的大小',
  `doc_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '主文件url',
  `file_id` bigint NULL DEFAULT NULL COMMENT '主文件上传的文件id',
  `doc_status` int NULL DEFAULT NULL COMMENT '文档状态，0:未上架，1:待上架 2:已上架 3:已下架',
  `doc_type` int NULL DEFAULT NULL COMMENT '类型：0:企业，1:个人',
  `doc_owner` bigint NULL DEFAULT NULL COMMENT '所有者',
  `doc_creator` bigint NULL DEFAULT NULL COMMENT '创建人',
  `recycle_date` date NULL DEFAULT NULL COMMENT '回收截止时间',
  `recycle_status` int NULL DEFAULT NULL COMMENT '回收状态 0:正常，1:已回收',
  `folder_id` bigint NULL DEFAULT NULL COMMENT '文件夹id',
  `upload_time` datetime NULL DEFAULT NULL COMMENT '上传时间',
  `lower_time` datetime NULL DEFAULT NULL COMMENT '下架时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`bus_id`) USING BTREE,
  INDEX `folder_id`(`folder_id`) USING BTREE,
  INDEX `rel_doc`(`rel_doc`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件夹、文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_document_tree
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_document_tree`;
CREATE TABLE `doc_bs_document_tree`  (
  `id` bigint NOT NULL COMMENT '主键',
  `doc_id` bigint NOT NULL COMMENT '本级文件夹',
  `father_id` bigint NOT NULL COMMENT '父级文件夹',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件夹层级的关联关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_document_user
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_document_user`;
CREATE TABLE `doc_bs_document_user`  (
  `id` bigint NOT NULL COMMENT '主键',
  `doc_id` bigint NOT NULL COMMENT '文件夹id',
  `rel_id` bigint NOT NULL COMMENT '用户id/部门id/机构id/团队id',
  `type` int NOT NULL COMMENT '关联的类型，0:用户、1:机构、2:部门、3:团队',
  `permiss_type` int NULL DEFAULT NULL COMMENT '权限，0:可查看，1:可编辑，2：可管理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件、文档和用户关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_message
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_message`;
CREATE TABLE `doc_bs_message`  (
  `message_id` bigint NOT NULL COMMENT '消息id',
  `user_id` bigint NOT NULL COMMENT '通知人id',
  `message_status` int NOT NULL DEFAULT 0 COMMENT '阅读状态（0：未读    1：已读）',
  `message_type` int NULL DEFAULT NULL COMMENT '消息类型',
  `message_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '消息标题',
  `message_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '消息体',
  `doc_folder` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文档目录（仅用于新文档上架提醒）',
  `doc_house_id` bigint NULL DEFAULT NULL COMMENT '文档库ID（仅用于新文档上架提醒）',
  `doc_parent_id` bigint NULL DEFAULT NULL COMMENT '文档父级目录id（仅用于新文档上架提醒）',
  `inform_time` datetime NOT NULL COMMENT '通知时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`message_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '消息通知记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_message_range
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_message_range`;
CREATE TABLE `doc_bs_message_range`  (
  `range_id` bigint NOT NULL COMMENT '范围id',
  `range_key` int NOT NULL COMMENT '范围key',
  `user_id` bigint NOT NULL COMMENT '通知人id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`range_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '消息通知接收范围表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_recently_document
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_recently_document`;
CREATE TABLE `doc_bs_recently_document`  (
  `recently_id` bigint NOT NULL COMMENT '最近预览id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `doc_id` bigint NOT NULL COMMENT '文档id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`recently_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '最近打开文档表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_recycle
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_recycle`;
CREATE TABLE `doc_bs_recycle`  (
  `recycle_id` bigint NOT NULL COMMENT '主键',
  `doc_id` bigint NULL DEFAULT NULL COMMENT '关联的文档或者文件夹id',
  `recycle_date` datetime NULL DEFAULT NULL COMMENT '回收截止时间',
  `del_date` datetime NULL DEFAULT NULL COMMENT '删除时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`recycle_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '回收表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_shape
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_shape`;
CREATE TABLE `doc_bs_shape`  (
  `shape_id` bigint NOT NULL COMMENT '分享id',
  `shape_user_id` bigint NOT NULL COMMENT '分享者id',
  `shape_type` int NOT NULL COMMENT '分享类别（0内部 1外部）',
  `shape_section` int NOT NULL COMMENT '分享区间（0：3天 1：7天 2：永久 3：单次）',
  `shape_preview` int NOT NULL DEFAULT 0 COMMENT '分享预览次数',
  `doc_id` bigint NOT NULL COMMENT '文档id',
  `invalid_time` datetime NOT NULL COMMENT '到期时间',
  `shape_time` datetime NOT NULL COMMENT '分享时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`shape_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '分享表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_shape_inside_user
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_shape_inside_user`;
CREATE TABLE `doc_bs_shape_inside_user`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `accept_type` int NOT NULL COMMENT '接受者类别（（0：用户，1：机构，2：部门，3：团队））',
  `accept_id` bigint NOT NULL COMMENT '接收者id',
  `shape_id` bigint NOT NULL COMMENT '分享id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '内部分享-用户关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_shape_outside_link
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_shape_outside_link`;
CREATE TABLE `doc_bs_shape_outside_link`  (
  `link_id` bigint NOT NULL COMMENT '链接id',
  `link_pwd` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分享链接访问pwd',
  `link_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分享链接url',
  `link_type` int NOT NULL COMMENT '分享外链类别 （0公开 1密码）',
  `share_id` bigint NOT NULL COMMENT '分享id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`link_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '外部分享-外联关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_tag_document
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_tag_document`;
CREATE TABLE `doc_bs_tag_document`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `doc_id` bigint NOT NULL COMMENT '文档id',
  `tag_id` bigint NOT NULL COMMENT '标签id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '标签 文档关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_task
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_task`;
CREATE TABLE `doc_bs_task`  (
  `id` bigint NOT NULL COMMENT '主键',
  `task_type` int NULL DEFAULT NULL COMMENT '类型0:删除存储任务',
  `rel_id` bigint NULL DEFAULT NULL COMMENT '关联id',
  `task_status` int NULL DEFAULT NULL COMMENT '执行状态1:完成，0:未完成,2:异常',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '处理异常日志',
  `task_time` datetime NULL DEFAULT NULL COMMENT '执行完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_bs_unhandle
-- ----------------------------
DROP TABLE IF EXISTS `doc_bs_unhandle`;
CREATE TABLE `doc_bs_unhandle`  (
  `unhandle_id` bigint NOT NULL COMMENT '待办id',
  `initiate_time` datetime NOT NULL COMMENT '发起时间',
  `initiate_user_id` bigint NOT NULL COMMENT '发起人id',
  `unhandle_type` int NOT NULL COMMENT '待办类型',
  `handle_user_id` bigint NOT NULL COMMENT '处理人id',
  `apply_id` bigint NOT NULL COMMENT '申请记录id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`unhandle_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '待办事项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_announ
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_announ`;
CREATE TABLE `doc_sys_announ`  (
  `ananoun_id` bigint NOT NULL COMMENT '公告id',
  `ananoun_title` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '公告标题',
  `ananoun_content` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '公告内容',
  `status` int NOT NULL COMMENT '公开状态 公开状态 0：未公开，1:公开',
  `release_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`ananoun_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_announ_user
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_announ_user`;
CREATE TABLE `doc_sys_announ_user`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `ananoun_id` bigint NOT NULL COMMENT '公告id',
  `rel_id` bigint NOT NULL COMMENT '关联id',
  `type` int NOT NULL COMMENT '关联的类型，0:用户、1:机构、2:部门、3:团队',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '公告发布对象表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_house
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_house`;
CREATE TABLE `doc_sys_house`  (
  `house_id` bigint NOT NULL COMMENT '主键id',
  `house_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '文档库名称',
  `house_seq` bigint NULL DEFAULT NULL COMMENT '顺序号',
  `house_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文档库描述',
  `house_file` bigint NULL DEFAULT NULL COMMENT '文档库容量大小',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`house_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文档库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_house_user
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_house_user`;
CREATE TABLE `doc_sys_house_user`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `house_id` bigint NOT NULL COMMENT '文档库id',
  `rel_id` bigint NOT NULL COMMENT '用户id/部门id/机构id/团队id',
  `type` int NOT NULL COMMENT '关联的类型，0:用户、1:机构、2:部门、3:团队',
  `permiss_type` int NULL DEFAULT NULL COMMENT '权限，0:可查看，1:可编辑，2：可管理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文档库和用户关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_tag
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_tag`;
CREATE TABLE `doc_sys_tag`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `tag_id` bigint NOT NULL COMMENT '标签id 本级',
  `tag_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标签code',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标签name',
  `tag_level` int NOT NULL COMMENT '标签的层级',
  `tag_sequen` bigint NULL DEFAULT NULL COMMENT '同一层级排序',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_team
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_team`;
CREATE TABLE `doc_sys_team`  (
  `team_id` bigint NOT NULL COMMENT '主键id',
  `team_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '团队名称',
  `team_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`team_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '团队表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doc_sys_team_user
-- ----------------------------
DROP TABLE IF EXISTS `doc_sys_team_user`;
CREATE TABLE `doc_sys_team_user`  (
  `id` bigint NOT NULL COMMENT '主键',
  `team_id` bigint NOT NULL COMMENT '团队id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '团队用户关联表' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `es_upload_record`;
CREATE TABLE `es_upload_record`  (
  `id` bigint NOT NULL COMMENT '主键',
  `bus_id` bigint NULL DEFAULT NULL COMMENT '文档id',
  `index_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '索引',
  `is_succeed` int NULL DEFAULT NULL COMMENT '是否上传成功1成功0失败',
  `exception_msg` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '异常信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'es上传记录记录表' ROW_FORMAT = DYNAMIC;