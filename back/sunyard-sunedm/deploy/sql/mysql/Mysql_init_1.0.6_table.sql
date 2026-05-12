SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bs_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc`;
CREATE TABLE `bs_arc`  (
  `arc_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号',
  `reference_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文号/项目编号',
  `arc_type_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案分类id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名',
  `arc_level` int(11) NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `secret_level` int(11) NOT NULL COMMENT '档案密级:0公开 1内部 2工作秘密',
  `arc_shape` int(11) NULL DEFAULT NULL COMMENT '档案形态:0纸质及电子 1 纸质档案 2 电子档案 3实物件 ',
  `arc_lock` int(11) NULL DEFAULT 0 COMMENT '档案标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定',
  `status` int(11) NULL DEFAULT 0 COMMENT '档案状态:0收集中 1待归档 2已归档 3已销毁 4已遗失 5部门移交 6已删除',
  `keep_year` int(11) NULL DEFAULT NULL COMMENT '保管年限999永久 10保存10年 30保存30年',
  `check_date` datetime(0) NULL DEFAULT NULL COMMENT '归档日期',
  `page_no` int(11) NULL DEFAULT NULL COMMENT '页数',
  `create_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '建档机构',
  `create_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '建档部门',
  `create_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '建档收集人',
  `create_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '建档日期',
  `arc_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '年度',
  `store_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '保管部门',
  `store_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '保管人',
  `store_date` datetime(0) NULL DEFAULT NULL COMMENT '保管终止日期',
  `manage_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '所属机构 档案业务发生机构',
  `manage_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '所属部门 档案业务发生部门',
  `manage_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '责任人    档案业务责任人',
  `archives_date` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '成文日期 档案业务形成日期',
  `check_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '归档检查人',
  `third_param` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方参数（影像参数）',
  `archives_source` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案来源 ：AM档案系统 TM,OA',
  `external_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '外部接入的档案唯一性记录',
  `placecode_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '位置码id',
  `box_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '档案盒id',
  `box_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '装盒人',
  `box_time` datetime(0) NULL DEFAULT NULL COMMENT '装盒时间',
  `box_num` int(11) NULL DEFAULT NULL COMMENT '序号 装盒',
  `folder_id` bigint(20) unsigned UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '案卷id',
  `folder_no` int(11) NULL DEFAULT NULL COMMENT '件号 组卷',
  `folder_time` datetime(0) NULL DEFAULT NULL COMMENT '组卷时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
   `is_material` int(11) NULL DEFAULT NULL COMMENT '是否维护材料清单 0是 1否',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '是否删除:0否-1是',
  PRIMARY KEY (`arc_id`) USING BTREE,
  INDEX `idx_arc_no`(`arc_no`) USING BTREE,
  INDEX `idx_title`(`title`) USING BTREE,
  INDEX `idx_manage_user`(`manage_user`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_external_id`(`external_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE,
INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_attachment
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_attachment`;
CREATE TABLE `bs_arc_attachment`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名称',
  `md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '文件md5',
  `primary_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '关联档案临时表',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_credentials_detail
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_credentials_detail`;
CREATE TABLE `bs_arc_credentials_detail`  (
  `cred_detail_id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `abstracts` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '摘要',
  `account_num` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计科目编号',
  `account_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计科目内容',
  `borrower` decimal(24, 2) NULL DEFAULT NULL COMMENT '借方',
  `lender` decimal(24, 2) NULL DEFAULT NULL COMMENT '贷方',
  `primary_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '关联档案临时表',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`cred_detail_id`) USING BTREE,
  INDEX `idx_account_num`(`account_num`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '第三方导入凭证明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_file
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_file`;
CREATE TABLE `bs_arc_file`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0,
  `parent_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '关联id，关联档案id和案卷id',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名称',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案文件和案卷Pdf的url地址',
  `file_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件编号',
  `type` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '地址类型（档案资料：0，案卷PDF：1）',
  `file_suffix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '后缀',
  `size` bigint(20) unsigned NULL DEFAULT 0,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_file_name`(`file_name`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_file_no`(`file_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_flow`;
CREATE TABLE `bs_arc_flow`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案id',
  `flow_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '流转名称',
  `flow_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转备注',
  `flow_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '流转时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_flow_user`(`flow_user`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案流程记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_middle
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_middle`;
CREATE TABLE `bs_arc_middle`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `primary_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '第三方唯一编号',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '基础内容',
  `extend` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '扩展内容',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '状态: 未上传文件，已上传文件，已归档到档案表',
  `file_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '文件列表',
  `system_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '系统名称',
  `system_type` int(11) NOT NULL DEFAULT 0 COMMENT '0办公，1财务，2业务，3人事',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '是否删除(0否，1是)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_primary_key`(`primary_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '第三方上传中间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_rule
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_rule`;
CREATE TABLE `bs_arc_rule`  (
  `rule_id` bigint(20) unsigned NOT NULL,
  `rule_way` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号生成规则方式',
  `create_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`rule_id`) USING BTREE,
  INDEX `idx_rule_way`(`rule_way`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案号生成规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_tem
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_tem`;
CREATE TABLE `bs_arc_tem`  (
  `tem_id` bigint(20) unsigned NOT NULL COMMENT '扩展字段id',
  `arc_type_id` bigint(20) unsigned NOT NULL COMMENT '档案类型ID',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `is_primary` int(11) NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `is_show` int(11) NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `is_sign` int(11) NOT NULL DEFAULT 0 COMMENT '是否显示在影像页面 0：不显示，1：显示',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `index_no` int(11) NOT NULL DEFAULT 0,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`tem_id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '扩展字段表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_tem_content`;
CREATE TABLE `bs_arc_tem_content`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `arc_id` bigint(20) unsigned NOT NULL COMMENT '档案id',
  `tem_id` bigint(20) unsigned NOT NULL COMMENT '扩展字段ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展字段内容',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `is_show` int(11) NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `is_primary` int(11) NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_tem_id`(`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '扩展字段详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_type
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_type`;
CREATE TABLE `bs_arc_type`  (
  `arc_type_id` bigint(20) unsigned NOT NULL COMMENT '档案分类id',
  `arc_type_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案分类号',
  `name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `parent_id` bigint(20) unsigned UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '0:办公、1：项目、2：业务、3：人事',
  `newlevel` int(11) NULL DEFAULT 0 COMMENT '分类级别',
  `status` int(11) NOT NULL DEFAULT 0 COMMENT '状态',
  `secrets` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '密级',
  `keep_year` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '999' COMMENT '保管年限 10：保管10年、30：保管30年、999永久保存',
  `shapes` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '档案形态0纸质及电子、1纸质、2电子、3实物件',
  `rules` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '#全宗号#-#年份#-#顺序号#' COMMENT '档案号拼接规则',
  `index_no` int(11) NOT NULL DEFAULT 0 COMMENT '分类序号',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  `is_material` int(11) NOT NULL COMMENT '是否维护材料清单 0是 1否',
  `ele_arc_menu` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '电子档目录来源 0档案系统 1影像系统',
  PRIMARY KEY (`arc_type_id`) USING BTREE,
  INDEX `idx_arc_type_no`(`arc_type_no`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_type_box
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_type_box`;
CREATE TABLE `bs_arc_type_box`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_type_id` bigint(20) unsigned NOT NULL COMMENT '档案类型Id',
  `box_id` bigint(20) unsigned NOT NULL COMMENT '档案盒Id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案类型-档案盒关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_arc_type_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_type_folder`;
CREATE TABLE `bs_arc_type_folder`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_type_id` bigint(20) unsigned NOT NULL COMMENT '档案类型Id',
  `folder_id` bigint(20) unsigned NOT NULL COMMENT '案卷Id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案类型-案卷关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_borrow
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow`;
CREATE TABLE `bs_borrow`  (
  `borrow_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `borrow_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '借阅机构',
  `borrow_no` int(11) NOT NULL COMMENT '借阅编号',
  `borrow_way` int(11) NOT NULL COMMENT '借阅方式（0：电子阅览  1：电子下载  2：电子打印  3：纸质原件  4：纸质复印  5：纸质阅览）',
  `borrow_per` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '借阅人',
  `per_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '证件/员工编号',
  `per_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '联系电话',
  `borrow_reason` int(11) NULL DEFAULT NULL COMMENT '借阅原因（0：编史修志 1：工作查考  2：学术研究  3：经济建设  4：宣传教育  5：其他）',
  `reason_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '原因描述',
  `borrow_day` int(11) NOT NULL COMMENT '借阅天数',
  `borrow_submitter` bigint(20) unsigned NOT NULL COMMENT '申请提交人',
  `borrow_status` int(11) NOT NULL DEFAULT 0 COMMENT '借阅状态 （0： 待审批  1：审批不通过   2：审批通过  3：确认借出 ）',
  `examine_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批备注',
  `return_status` int(11) NULL DEFAULT NULL COMMENT '借阅有效状态 （0：待归还  1：部分归还  2：已归还  3：有效   4：失效  5: 部分有效）',
  `examine_time` datetime(0) NULL DEFAULT NULL COMMENT '审批时间',
  `expire_time` datetime(0) NULL DEFAULT NULL COMMENT '到期时间',
  `return_time` datetime(0) NULL DEFAULT NULL COMMENT '归还时间',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `third_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `examine_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '借阅登记人',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`borrow_id`) USING BTREE,
  INDEX `idx_borrow_inst`(`borrow_inst`) USING BTREE,
  INDEX `idx_borrow_per`(`borrow_per`) USING BTREE,
  INDEX `idx_borrow_no`(`borrow_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_borrow_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_content`;
CREATE TABLE `bs_borrow_content`  (
  `bor_content_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键 （借阅内容id） ',
  `borrow_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '借阅id',
  `arc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名',
  `content_id` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '借阅内容详细id（以逗号分隔）',
  `borrow_type` int(11) NULL DEFAULT NULL COMMENT '借阅类型 （ 0:整件档案借阅:  1:部分文件资料借阅）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `placecode_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '库房位置码id',
  `content_status` int(11) NULL DEFAULT 0 COMMENT '归还状态（0:  待确认 1：待归还  2：已归还  3:  有效   4：失效）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`bor_content_id`) USING BTREE,
  INDEX `idx_borrow_id`(`borrow_id`) USING BTREE,
  INDEX `idx_content_id`(`content_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_title`(`title`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅申请内容表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_borrow_content_materials
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_content_materials`;
CREATE TABLE `bs_borrow_content_materials`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `bor_content_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '借阅内容id',
  `materials_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '材料id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bor_content_id`(`bor_content_id`) USING BTREE,
  INDEX `idx_materials_id`(`materials_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅内容对应资料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_borrow_examine
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_examine`;
CREATE TABLE `bs_borrow_examine`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `examine_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '审批人id',
  `examine_time` datetime(0) NULL DEFAULT NULL COMMENT '审批时间',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `examine_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_remakrs` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批备注',
  `examine_result` int(11) NULL DEFAULT NULL COMMENT '审批结果 1不同意 2同意',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_process_instance_id`(`process_instance_id`) USING BTREE,
  INDEX `idx_examine_user`(`examine_user`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅审批记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_borrow_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_flow`;
CREATE TABLE `bs_borrow_flow`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `borrow_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案id',
  `flow_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转名称',
  `flow_user` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '批审备注',
  `flow_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '流转时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_borrow_id`(`borrow_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅流程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_borrow_urge
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_urge`;
CREATE TABLE `bs_borrow_urge`  (
  `urge_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `borrow_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '借阅机构',
  `borrow_per` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '借阅人',
  `borrow_way` int(11) NOT NULL COMMENT '借阅方式（0：电子阅览  1：电子下载  2：电子打印  3：纸质原件  4：纸质复印  5：纸质阅览）',
  `borrow_time` datetime(0) NOT NULL COMMENT '借阅日期',
  `expire_time` datetime(0) NOT NULL COMMENT '到期时间',
  `urge_way` int(11) NOT NULL COMMENT '催还方式（0:消息提醒）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`urge_id`) USING BTREE,
  INDEX `idx_borrow_per`(`borrow_per`) USING BTREE,
  INDEX `idx_borrow_inst`(`borrow_inst`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅催还记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_box
-- ----------------------------
DROP TABLE IF EXISTS `bs_box`;
CREATE TABLE `bs_box`  (
  `box_id` bigint(20) unsigned NOT NULL COMMENT '档案盒主键',
  `box_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案盒号',
  `box_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案盒题名',
  `box_type` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案盒类型',
  `box_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创键人',
  `box_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '所属部门ID',
  `year` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '年份',
  `case_num` int(11) NOT NULL DEFAULT 0 COMMENT '档案件数',
  `update_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '最近修改人',
  `placecode_str` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '库房位置码',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创键时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`box_id`) USING BTREE,
  INDEX `idx_box_no`(`box_no`) USING BTREE,
  INDEX `idx_box_title`(`box_title`) USING BTREE,
  INDEX `idx_box_type`(`box_type`) USING BTREE,
  INDEX `idx_box_user`(`box_user`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案盒表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_box_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_box_folder`;
CREATE TABLE `bs_box_folder`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `box_id` bigint(20) unsigned NOT NULL COMMENT '档案盒Id',
  `folder_id` bigint(20) unsigned NOT NULL COMMENT '案卷Id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案盒-案卷关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_destroy
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy`;
CREATE TABLE `bs_destroy`  (
  `destroy_id` bigint(20) unsigned NOT NULL COMMENT '销毁id',
  `destroy_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销毁编号',
  `destroy_shape` int(11) NULL DEFAULT NULL COMMENT '销毁形态 0纸质 1电子 2纸质及电子',
  `destroy_type` int(11) NULL DEFAULT NULL COMMENT '销毁方式',
  `destroy_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '销毁机构',
  `destroy_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '销毁部门',
  `destroy_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '销毁人',
  `monitor_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '监销机构',
  `monitor_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '监销部门',
  `monitor_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '监销人',
  `destroy_time` datetime(0) NULL DEFAULT NULL COMMENT '销毁时间',
  `state` int(11) NULL DEFAULT NULL COMMENT '状态  0:待销毁 1:已销毁',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销毁原因',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创键时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`destroy_id`) USING BTREE,
  INDEX `idx_destroy_no`(`destroy_no`) USING BTREE,
  INDEX `idx_destroy_shape`(`destroy_shape`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销毁记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_destroy_apply
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_apply`;
CREATE TABLE `bs_destroy_apply`  (
  `apply_id` bigint(20) unsigned NOT NULL COMMENT '申请id',
  `apply_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申请编号',
  `apply_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '申请机构',
  `apply_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '申请部门',
  `apply_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '申请人',
  `apply_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申请原因',
  `apply_opera` int(11) NULL DEFAULT NULL COMMENT '申请操作',
  `apply_state` int(11) NULL DEFAULT NULL COMMENT '申请（审批） 0待审批 1审批中 2已审批 ',
  `apply_time` datetime(0) NULL DEFAULT NULL COMMENT '申请时间',
  `examine_time` datetime(0) NULL DEFAULT NULL COMMENT '审批时间',
  `examine_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '审批人',
  `examine_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_result` int(11) NULL DEFAULT NULL COMMENT '审批结果： 0同意 1不同意',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `handle_result` int(11) NULL DEFAULT 0 COMMENT '处理结果 0未销毁，1已销毁',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`apply_id`) USING BTREE,
  INDEX `idx_apply_no`(`apply_no`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销毁申请记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_destroy_apply_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_apply_arc`;
CREATE TABLE `bs_destroy_apply_arc`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `apply_id` bigint(20) unsigned NOT NULL COMMENT '申请id',
  `arc_id` bigint(20) unsigned NOT NULL COMMENT '档案id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_apply_id`(`apply_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '申请-档案关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_destroy_examine
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_examine`;
CREATE TABLE `bs_destroy_examine`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `examine_time` datetime(0) NULL DEFAULT NULL COMMENT '审批时间',
  `examine_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '审批人id',
  `examine_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_result` int(11) NULL DEFAULT NULL COMMENT '审批结果： 0同意 1不同意',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_examine_user`(`examine_user`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销毁审批记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_destroy_identify
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_identify`;
CREATE TABLE `bs_destroy_identify`  (
  `identify_id` bigint(20) unsigned NOT NULL COMMENT '鉴定id',
  `identify_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '鉴定机构',
  `identify_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '鉴定编号',
  `identify_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '鉴定日期',
  `identify_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定意见',
  `identify_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定人',
  `identify_result` int(11) NULL DEFAULT NULL COMMENT '鉴定结果 0:保留 1:销毁',
  `identify_arc` bigint(20) unsigned NULL DEFAULT NULL COMMENT '鉴定档案id',
  `identify_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '鉴定申请部门',
  `state` int(11) NULL DEFAULT NULL COMMENT '状态 0:待鉴定 1:已鉴定',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定申请原因',
  `handle_state` int(11) NULL DEFAULT NULL COMMENT '处理状态',
  `identify_report` int(11) NULL DEFAULT NULL COMMENT '鉴定报告状态 0已上传 1未上传',
  `report_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定报告本机地址',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `third_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定材料上传影像地址',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`identify_id`) USING BTREE,
  INDEX `idx_identify_result`(`identify_result`) USING BTREE,
  INDEX `idx_handle_state`(`handle_state`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案鉴定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder`;
CREATE TABLE `bs_folder`  (
  `folder_id` bigint(20) unsigned NOT NULL COMMENT '案卷主键',
  `folder_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '案卷号',
  `folder_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷标题',
  `folder_type_id` bigint(20) unsigned NOT NULL COMMENT '案卷类型ID',
  `folder_inst` bigint(20) unsigned NULL DEFAULT NULL COMMENT '所属机构id',
  `folder_dept` bigint(20) unsigned NULL DEFAULT NULL COMMENT '所属部门id',
  `folder_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '立卷人',
  `year` int(11) NULL DEFAULT NULL COMMENT '年度',
  `case_num` int(11) NOT NULL DEFAULT 0 COMMENT '件数',
  `page_num` int(11) NOT NULL DEFAULT 0 COMMENT '页数',
  `update_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '最近修改人',
  `placecode_str` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '库房位置码',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`folder_id`) USING BTREE,
  INDEX `idx_folder_no`(`folder_no`) USING BTREE,
  INDEX `idx_folder_title`(`folder_title`) USING BTREE,
  INDEX `idx_folder_type_id`(`folder_type_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder_autorule
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_autorule`;
CREATE TABLE `bs_folder_autorule`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `type_id` bigint(20) unsigned NOT NULL COMMENT '分类id',
  `method` int(11) NOT NULL COMMENT '组卷方式 0:手动，1:自动',
  `rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '档案号生成规则',
  `title_rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题名生成规则id',
  `system_code` int(11) NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_id`(`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '自动组卷配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_flow`;
CREATE TABLE `bs_folder_flow`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `folder_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '案卷id',
  `flow_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '流转名称',
  `flow_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转备注',
  `flow_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '流转时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷流程记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder_rule
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_rule`;
CREATE TABLE `bs_folder_rule`  (
  `rule_id` bigint(20) unsigned NOT NULL,
  `rule_way` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '档案号生成规则方式',
  `type` int(11) NOT NULL COMMENT '会计系统-类型1:自动组卷所用的档案规则，2:自动组卷中的档案提名规则，默认档案号',
  `create_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`rule_id`) USING BTREE,
  INDEX `idx_rule_way`(`rule_way`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '档案号生成规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder_tem
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_tem`;
CREATE TABLE `bs_folder_tem`  (
  `folder_tem_id` bigint(20) unsigned NOT NULL COMMENT '扩展字段id',
  `folder_type_id` bigint(20) unsigned NOT NULL COMMENT '案卷类型ID',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `is_primary` int(11) NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `is_show` int(11) NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `index_no` int(11) NOT NULL DEFAULT 0 COMMENT '序号',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`folder_tem_id`) USING BTREE,
  INDEX `idx_folder_type_id`(`folder_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷扩展字段表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_tem_content`;
CREATE TABLE `bs_folder_tem_content`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `folder_id` bigint(20) unsigned NULL DEFAULT NULL,
  `tem_id` bigint(20) unsigned NULL DEFAULT NULL,
  `tem_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE,
  INDEX `idx_tem_id`(`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷类型扩展字段内容表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_folder_type
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_type`;
CREATE TABLE `bs_folder_type`  (
  `folder_type_id` bigint(20) unsigned NOT NULL COMMENT '案卷分类主键',
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '父级分类',
  `folder_type_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '案卷分类号',
  `name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `rules` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷号自动拼接规则',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `index_no` int(11) NOT NULL DEFAULT 0 COMMENT '分类序号',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`folder_type_id`) USING BTREE,
  INDEX `idx_folder_type_no`(`folder_type_no`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_inout_storage
-- ----------------------------
DROP TABLE IF EXISTS `bs_inout_storage`;
CREATE TABLE `bs_inout_storage`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `placecode_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '位置码id',
  `arc_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案id',
  `operation_type` int(11) NULL DEFAULT NULL COMMENT '操作类型（0：入库   1：出库）',
  `operation_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `store_inst` bigint(20) unsigned NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '出入库记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_ldap_middle
-- ----------------------------
DROP TABLE IF EXISTS `bs_ldap_middle`;
CREATE TABLE `bs_ldap_middle`  (
  `id` bigint(20) unsigned NOT NULL,
  `type` int(11) NULL DEFAULT NULL COMMENT '类型 0组织 1用户',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数体',
  `inst_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '机构id',
  `dept_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '部门id',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创键时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'LDAP中间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_materials
-- ----------------------------
DROP TABLE IF EXISTS `bs_materials`;
CREATE TABLE `bs_materials`  (
  `materials_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '材料id',
  `materials_type_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '材料类型id',
  `arc_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '材料名称',
  `shape` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '材料形态 0 实物及电子 1实物  2 电子',
  `page` int(11) NULL DEFAULT NULL COMMENT '页数',
  `form_time` datetime(0) NULL DEFAULT NULL COMMENT '形成时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`materials_id`) USING BTREE,
  INDEX `idx_materials_type_id`(`materials_type_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '材料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_materials_type
-- ----------------------------
DROP TABLE IF EXISTS `bs_materials_type`;
CREATE TABLE `bs_materials_type`  (
  `materials_type_id` bigint(20) unsigned NOT NULL COMMENT '材料分类id',
  `materials_type_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '材料分类号',
  `arc_type_id` bigint(20) unsigned NOT NULL COMMENT '档案分类id',
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '材料分类id-父级',
  `index_no` int(11) NOT NULL DEFAULT 0 COMMENT '材料序号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '材料分类名称',
  `status` int(11) NOT NULL DEFAULT 0 COMMENT '状态（暂不用）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`materials_type_id`) USING BTREE,
  INDEX `idx_materials_type_no`(`materials_type_no`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '材料类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_message
-- ----------------------------
DROP TABLE IF EXISTS `bs_message`;
CREATE TABLE `bs_message`  (
  `message_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `user_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '提醒用户id',
  `message_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '消息标题',
  `message_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '消息内容',
  `message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '消息类型',
  `message_status` int(11) NULL DEFAULT 0 COMMENT '阅读状态（0：未读    1：已读）',
  `inform_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '通知时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '消息提示表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_permiss
-- ----------------------------
DROP TABLE IF EXISTS `bs_permiss`;
CREATE TABLE `bs_permiss`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `confid_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '保密级别名称',
  `confid_code` int(11) NULL DEFAULT NULL COMMENT '保密code',
  `dept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '部门权限\n\n',
  `rolelist` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '角色权限list',
  `system_type` int(11) NULL DEFAULT NULL COMMENT '系统类别',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_confid_name`(`confid_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_perwaitfor
-- ----------------------------
DROP TABLE IF EXISTS `bs_perwaitfor`;
CREATE TABLE `bs_perwaitfor`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `init_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '申请Id',
  `flow_per` bigint(20) unsigned NULL DEFAULT NULL COMMENT '流程当前处理人id',
  `wait_type` int(11) NULL DEFAULT NULL COMMENT '代办事项类型(0：借阅申请待审批   1：销毁申请待审批)',
  `init_user` bigint(20) unsigned NULL DEFAULT NULL COMMENT '发起人',
  `init_dpt` bigint(20) unsigned NULL DEFAULT NULL COMMENT '发起部门',
  `init_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发起时间',
  `top_status` int(11) NULL DEFAULT 0 COMMENT '置顶状态  （0：不置顶  1：置顶）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flow_per`(`flow_per`) USING BTREE,
  INDEX `idx_init_id`(`init_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '待办事项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_placecode
-- ----------------------------
DROP TABLE IF EXISTS `bs_placecode`;
CREATE TABLE `bs_placecode`  (
  `placecode_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '位置码id',
  `warehouse_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '库房id',
  `placecode_no` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '位置码-数字',
  `placecode_str` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '位置码-字符串',
  `use_state` int(11) NULL DEFAULT 0 COMMENT '使用状态(未使用:0,已使用:1)',
  `capacity_state` int(11) NULL DEFAULT 1 COMMENT '容量状态(未满:0,空闲:1,已满:2) ',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`placecode_id`) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id`) USING BTREE,
  INDEX `idx_placecode_no`(`placecode_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '位置码表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_transfer
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer`;
CREATE TABLE `bs_transfer`  (
  `transfer_id` bigint(20) unsigned NOT NULL COMMENT '移交id',
  `transfer_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '移交编号',
  `transfer_user` bigint(20) unsigned NOT NULL COMMENT '移交人id',
  `transfer_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '移交人名',
  `transfer_org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '移交组织名',
  `transfer_org` bigint(20) unsigned NULL DEFAULT NULL COMMENT '移交组织',
  `transfer_date` datetime(0) NULL DEFAULT NULL COMMENT '移交时间',
  `transfer_type` int(11) NOT NULL COMMENT '移交类型 0 档案移交 1材料移交',
  `transfer_state` int(11) NULL DEFAULT NULL COMMENT '移交状态 0 移交中（待接收） 1已接收 2移交退回',
  `receive_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '通知人id',
  `receive_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '通知人名',
  `receive_org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '接收组织名',
  `receive_org` bigint(20) unsigned NULL DEFAULT NULL COMMENT '接收组织',
  `receive_date` datetime(0) NULL DEFAULT NULL COMMENT '接收时间',
  `re_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退回原因或者接收原因',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `receiver` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '接收人',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创键时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`transfer_id`) USING BTREE,
  INDEX `idx_transfer_no`(`transfer_no`) USING BTREE,
  INDEX `idx_transfer_org_name`(`transfer_org_name`) USING BTREE,
  INDEX `idx_receive_date`(`receive_date`) USING BTREE,
  INDEX `idx_receive_org_name`(`receive_org_name`) USING BTREE,
  INDEX `idx_transfer_date`(`transfer_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '移交记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_transfer_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_arc`;
CREATE TABLE `bs_transfer_arc`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `transfer_id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `arc_id` bigint(20) unsigned NOT NULL COMMENT '档案id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transfer_id`(`transfer_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '移交档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_transfer_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_folder`;
CREATE TABLE `bs_transfer_folder`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `transfer_id` bigint(20) unsigned NOT NULL COMMENT '移交Id',
  `folder_id` bigint(20) unsigned NOT NULL COMMENT '案卷id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transfer_id`(`transfer_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '移交案卷表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_transfer_remove
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_remove`;
CREATE TABLE `bs_transfer_remove`  (
  `remove_id` bigint(20) unsigned NOT NULL COMMENT '转递id',
  `remove_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '转递编号',
  `remove_inst` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '转递机构',
  `remove_time` datetime(0) NULL DEFAULT NULL COMMENT '转递日期',
  `remove_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '转递联系人',
  `remove_opearetor` bigint(20) unsigned NULL DEFAULT NULL COMMENT '转递操作人',
  `arc_num` int(11) NULL DEFAULT NULL COMMENT '档案件数',
  `arc_page` int(11) NULL DEFAULT NULL COMMENT '档案页数',
  `storage_day` int(11) NULL DEFAULT NULL COMMENT '电子保管天数',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `transfer_org` bigint(20) unsigned NULL DEFAULT NULL COMMENT '被转递组织',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`remove_id`) USING BTREE,
  INDEX `idx_remove_no`(`remove_no`) USING BTREE,
  INDEX `idx_remove_inst`(`remove_inst`) USING BTREE,
  INDEX `idx_remove_time`(`remove_time`) USING BTREE,
  INDEX `idx_remove_opearetor`(`remove_opearetor`) USING BTREE,
  INDEX `idx_remove_user`(`remove_user`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '转递记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_transfer_remove_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_remove_arc`;
CREATE TABLE `bs_transfer_remove_arc`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `remove_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '主键',
  `arc_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '档案id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '转递档案' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bs_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `bs_warehouse`;
CREATE TABLE `bs_warehouse`  (
  `warehouse_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '库房id',
  `inst_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '机构id',
  `warehouse_code` int(11) NOT NULL DEFAULT 0 COMMENT '库房编号',
  `warehouse_no` varchar(20) NULL COMMENT '库房位置编码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '库房名称',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '库房类型(自有:0,外包:1)',
  `area` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '库房面积',
  `location` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '库房位置',
  `amount` int(11) NULL DEFAULT 0 COMMENT '库柜数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`warehouse_id`) USING BTREE,
  INDEX `idx_warehouse_code`(`warehouse_code`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '库房表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_api
-- ----------------------------
DROP TABLE IF EXISTS `sys_api`;
CREATE TABLE `sys_api`  (
  `id` bigint(20) unsigned NOT NULL,
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `public_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `system_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `system_type` int(11) NULL DEFAULT NULL COMMENT '0办公，1财务，2业务，3人事',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_app_id`(`app_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '对外api接口表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_api_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_api_log`;
CREATE TABLE `sys_api_log`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `user_id` bigint(20) unsigned NULL DEFAULT 0 COMMENT '用户id',
  `request_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-ip',
  `request_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-接口url',
  `request_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-接口-中文功能描述',
  `request_params` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-入参',
  `response_code` int(11) NOT NULL DEFAULT 0 COMMENT '日志状态(成功:0,失败:1,异常:2)',
  `exception_msg` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_request_ip`(`request_ip`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '第三方操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_approval_act
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_act`;
CREATE TABLE `sys_approval_act`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `inst_id` bigint(20) unsigned NULL DEFAULT 0 COMMENT '机构id',
  `approval_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '审批类型',
  `act_key` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '流程定义中的key',
  `parameters` varchar(9999) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '定义的参数,用以判断是否要更新-流程定义模板',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '关联表(审批类型-工作流定义名)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config_login
-- ----------------------------
DROP TABLE IF EXISTS `sys_config_login`;
CREATE TABLE `sys_config_login`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT 'id',
  `source_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '数据源名称',
  `source_describe` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '数据源描述',
  `server_url` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'ip地址',
  `server_dn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '服务器Dn',
  `base` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '组织架构',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `login_type` bigint(20) unsigned NULL DEFAULT 0 COMMENT '登录方式 0：本地 1：ldap   ',
  `is_activate` int(11) NOT NULL DEFAULT 0 COMMENT '是否激活(否:0,是:1)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config_watermark
-- ----------------------------
DROP TABLE IF EXISTS `sys_config_watermark`;
CREATE TABLE `sys_config_watermark`  (
  `id` bigint(20) unsigned NOT NULL,
  `content` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '水印内容',
  `transparency` int(11) NOT NULL DEFAULT 0 COMMENT '透明度',
  `font` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字体',
  `font_size` int(11) NOT NULL DEFAULT 10 COMMENT '字体大小',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '颜色',
  `is_activate` int(11) NOT NULL DEFAULT 0 COMMENT '是否启用(否:0,是:1)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '水印配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `dept_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '部门id',
  `dept_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '部门号',
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '部门id-父级',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门名称',
  `name_level` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门名称-递归显示',
  `newlevel` int(11) NULL DEFAULT 0 COMMENT '层级',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_ldap_id`(`ldap_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept_middle
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept_middle`;
CREATE TABLE `sys_dept_middle`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT 'id',
  `no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '部门编号',
  `parent_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '上级部门编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '部门名称',
  `inst_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '机构编号',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '处理状态',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dept_third
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept_third`;
CREATE TABLE `sys_dept_third`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT 'id',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '参数类型',
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '参数类型',
  `dept_id` bigint(20) unsigned NOT NULL COMMENT '部门id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表第三方参数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_group`;
CREATE TABLE `sys_group`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '用户组编号',
  `group_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '用户组名',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '备注',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态，0:未启用，1:启用',
  `system_type` int(11) NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `permiss_level` int(11) NULL DEFAULT NULL COMMENT '组织权限类型 0:指定部门可看，1：档案所属部门可看，2：全部部门可看',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '组织表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_inst
-- ----------------------------
DROP TABLE IF EXISTS `sys_inst`;
CREATE TABLE `sys_inst`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `inst_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '机构id',
  `inst_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '机构号',
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '机构id-父级',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '机构名称',
  `name_level` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '机构名称-递归显示',
  `newlevel` int(11) NULL DEFAULT 0 COMMENT '层级',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '全总号',
  `identifier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '全宗名称',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `ldap_query_dn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'ldap查询dn',
  `ldap_query_user_txt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'ldap查询用户的条件',
  `ldap_query_dpt_txt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'ldap查询部门的条件',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '机构表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '主键',
  `user_id` bigint(20) unsigned NULL DEFAULT 0 COMMENT '用户id',
  `request_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-ip',
  `request_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-接口url',
  `request_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-接口-中文功能描述',
  `request_params` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-入参',
  `response_code` int(11) NOT NULL DEFAULT 0 COMMENT '日志状态(成功:0,失败:1,异常:2)',
  `exception_msg` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_response_code`(`response_code`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '菜单id',
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '父菜单id',
  `menu_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '菜单名称',
  `perms` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '权限标识',
  `path` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '接口地址',
  `component` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '前端组件路径',
  `menu_type` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '菜单类型(菜单:M,目录:D,按钮:B)',
  `is_frame` int(11) NULL DEFAULT 1 COMMENT '是否为外链(否:0,是:1)',
  `is_cache` int(11) NULL DEFAULT 0 COMMENT '是否缓存(不缓存:0,缓存:1)',
  `icon` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `status` int(11) NULL DEFAULT 0 COMMENT '启用状态(显示:0,停用:1)',
  `visible` int(11) NULL DEFAULT 0 COMMENT '隐藏状态(显示:0,隐藏:1)',
  `order_num` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `remark` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '备注',
  `permissions_type` int(11) NOT NULL DEFAULT 0 COMMENT '默认0 超级管理员9',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`menu_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_param
-- ----------------------------
DROP TABLE IF EXISTS `sys_param`;
CREATE TABLE `sys_param`  (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '0:固定参数',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_permiss
-- ----------------------------
DROP TABLE IF EXISTS `sys_permiss`;
CREATE TABLE `sys_permiss`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `confid_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '保密级别名称',
  `confid_code` int(11) NULL DEFAULT NULL COMMENT '保密code',
  `newlevel` int(11) NULL DEFAULT 0 COMMENT '分类级别',
  `system_type` int(11) NULL DEFAULT NULL COMMENT '系统类别',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_permiss_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_permiss_group`;
CREATE TABLE `sys_permiss_group`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `group_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '用户组id',
  `permiss` int(11) NULL DEFAULT NULL COMMENT '权限code',
  `dept_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '部门id',
  `type_id` bigint(20) unsigned NULL DEFAULT NULL COMMENT '类型id',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '备注',
  `permiss_level` int(11) NULL DEFAULT NULL COMMENT '组织权限类型 0:指定部门可看，1：档案所属部门可看，2：全部部门可看',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id`(`group_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_type_id`(`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '组织权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(20) unsigned NOT NULL DEFAULT 1 COMMENT '角色id',
  `inst_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '机构id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '角色名称',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '描述',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '启用状态(启用:0,停用:1)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`role_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `role_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '角色id',
  `menu_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '菜单id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '关联表(角色-菜单)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_user`;
CREATE TABLE `sys_role_user`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键id',
  `role_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '角色id',
  `user_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '用户id',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '关联表(角色用户)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '用户id',
  `inst_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '机构id',
  `dept_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '部门id',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '登录名',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '用户工号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '姓名',
  `pwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `sex` int(11) NULL DEFAULT 0 COMMENT '性别(女:0,男:1)',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '联系方式',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '电子邮箱',
  `state` int(11) NOT NULL DEFAULT 0 COMMENT '账号状态(未启用:0,启用:1,注销:2,锁定:3)',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '账号类型(普通:0,系统管理员:1)',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `is_scan` int(11) NULL DEFAULT 1 COMMENT '是否有扫描仪权限 0是 1否',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_code`(`code`) USING BTREE,
  INDEX `idx_name`(`name`) USING BTREE,
  INDEX `idx_state`(`state`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_admin`;
CREATE TABLE `sys_user_admin`  (
  `user_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '用户id',
  `inst_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '机构id',
  `dept_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '部门id',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '登录名',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '用户工号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '姓名',
  `pwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `sex` int(11) NULL DEFAULT 0 COMMENT '性别(女:0,男:1)',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '联系方式',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '电子邮箱',
  `state` int(11) NOT NULL DEFAULT 0 COMMENT '账号状态(未启用:0,启用:1,注销:2,锁定:3)',
  `type` int(11) NOT NULL DEFAULT 0 COMMENT '账号类型(普通:0,系统管理员:1)',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `is_scan` int(11) NULL DEFAULT 1 COMMENT '是否有扫描仪权限 0是 1否',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_group`;
CREATE TABLE `sys_user_group`  (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `group_id` bigint(20) unsigned NOT NULL COMMENT '用户组id',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
   PRIMARY KEY (`id`) USING BTREE,
   INDEX `idx_group_id`(`group_id`) USING BTREE,
   INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '用户组织表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_param
-- ----------------------------
DROP TABLE IF EXISTS `sys_param`;
CREATE TABLE `sys_param`  (
  `id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `type` int(0) NOT NULL DEFAULT 0 COMMENT '0:固定参数',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统参数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_no_rule
-- ----------------------------
DROP TABLE IF EXISTS `sys_no_rule`;
CREATE TABLE `sys_no_rule`  (
  `type_id` bigint(20) unsigned NOT NULL COMMENT 'id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '号名称',
  `rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '号规则',
  `no_type` int(11) NULL DEFAULT NULL COMMENT '号类型 1：档案号 2：盒号 3：案卷号',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创键时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '修改时间',
  `is_deleted` int(11) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统标识号生成规则表' ROW_FORMAT = Dynamic;


SET FOREIGN_KEY_CHECKS = 1;
