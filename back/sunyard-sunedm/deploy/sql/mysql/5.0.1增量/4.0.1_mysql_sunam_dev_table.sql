/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : 127.0.0.1:3307
 Source Schema         : sunam_dev

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 08/12/2022 09:47:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bs_acc_inout_storage
-- ----------------------------
DROP TABLE IF EXISTS `bs_acc_inout_storage`;
CREATE TABLE `bs_acc_inout_storage`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `placecode_id` bigint NULL DEFAULT NULL COMMENT '位置码id',
  `folder_id` bigint NULL DEFAULT NULL COMMENT '案卷号',
  `operation_type` int NULL DEFAULT NULL COMMENT '操作类型（0：入库   1：出库）',
  `operation_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `store_inst` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '出入库记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc`;
CREATE TABLE `bs_arc`  (
  `arc_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_no` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号',
  `reference_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文号/项目编号',
  `arc_type_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案分类id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名',
  `arc_level` int NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `secret_level` int NULL DEFAULT NULL COMMENT '档案密级:0公开 1内部 2工作秘密',
  `arc_shape` int NULL DEFAULT NULL COMMENT '档案形态:0纸质及电子 1 纸质档案 2 电子档案 3实物件 ',
  `arc_lock` int NULL DEFAULT 0 COMMENT '档案标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定',
  `status` int NULL DEFAULT 0 COMMENT '档案状态:0收集中 1待归档 2已归档 3已销毁 4已遗失 5部门移交 6已删除',
  `keep_year` int NULL DEFAULT NULL COMMENT '保管年限999永久 10保存10年 30保存30年',
  `check_date` datetime NULL DEFAULT NULL COMMENT '归档日期',
  `page_no` int NULL DEFAULT NULL COMMENT '页数',
  `create_inst` bigint NULL DEFAULT NULL COMMENT '建档机构',
  `create_dept` bigint NULL DEFAULT NULL COMMENT '建档部门',
  `create_user` bigint NULL DEFAULT NULL COMMENT '建档收集人',
  `create_date` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档日期',
  `arc_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '年度',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `store_user` bigint NULL DEFAULT NULL COMMENT '保管人',
  `store_date` datetime NULL DEFAULT NULL COMMENT '保管终止日期',
  `manage_inst` bigint NULL DEFAULT NULL COMMENT '所属机构 档案业务发生机构',
  `manage_dept` bigint NULL DEFAULT NULL COMMENT '所属部门 档案业务发生部门',
  `manage_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '责任人    档案业务责任人',
  `archives_date` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成文日期 档案业务形成日期',
  `check_user` bigint NULL DEFAULT NULL COMMENT '归档检查人',
  `third_param` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方参数（影像参数）',
  `archives_source` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案来源 ：AM档案系统 TM,OA',
  `external_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '外部接入的档案唯一性记录',
  `placecode_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '位置码id',
  `box_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '档案盒id',
  `box_user` bigint NULL DEFAULT NULL COMMENT '装盒人',
  `box_time` datetime NULL DEFAULT NULL COMMENT '装盒时间',
  `box_num` int NULL DEFAULT NULL COMMENT '序号 装盒',
  `folder_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '案卷id',
  `folder_no` int NULL DEFAULT NULL COMMENT '件号 组卷',
  `folder_time` datetime NULL DEFAULT NULL COMMENT '组卷时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `is_material` int NULL DEFAULT NULL COMMENT '是否维护材料清单 0是 1否',
  `file_num` int NULL DEFAULT NULL COMMENT '关联文件数量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除:0否-1是',
  PRIMARY KEY (`arc_id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_external_id`(`external_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_acc_audit
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_acc_audit`;
CREATE TABLE `bs_arc_acc_audit`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_id` bigint NOT NULL DEFAULT 0 COMMENT '档案ID',
  `sub_user` bigint NULL DEFAULT NULL COMMENT '发起资料审核人(提交人)',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '会计档案-审核流程关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_attach
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_attach`;
CREATE TABLE `bs_arc_attach`  (
  `id` bigint NOT NULL COMMENT '主键',
  `arc_id` bigint NULL DEFAULT NULL COMMENT '关联资料ID',
  `file_id` bigint NULL DEFAULT NULL COMMENT '关联附件ID',
  `attach_relation` int NOT NULL COMMENT '关联方式  0：自动匹配 1：手工匹配',
  `sort` int NULL DEFAULT NULL COMMENT '关联附件排序',
  `file_num` int NULL DEFAULT NULL COMMENT '附件件数',
  `have_finish` int NOT NULL DEFAULT 0 COMMENT '是否同步0未同步1同步',
  `relation_state` int NOT NULL DEFAULT 0 COMMENT '关联状态 0：有效 1：无效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_file_id`(`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '凭证与附件关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_attachment
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_attachment`;
CREATE TABLE `bs_arc_attachment`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名称',
  `md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '文件md5',
  `primary_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '关联档案临时表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案附件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_credentials_detail
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_credentials_detail`;
CREATE TABLE `bs_arc_credentials_detail`  (
  `cred_detail_id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `abstracts` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '摘要',
  `account_num` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计科目编号',
  `account_content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计科目内容',
  `borrower` decimal(24, 2) NULL DEFAULT NULL COMMENT '借方',
  `lender` decimal(24, 2) NULL DEFAULT NULL COMMENT '贷方',
  `primary_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '关联档案临时表',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`cred_detail_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '第三方导入凭证明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_file
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_file`;
CREATE TABLE `bs_arc_file`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联id，关联档案id和案卷id',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名称',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案文件和案卷Pdf的url地址',
  `file_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件编号',
  `type` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '地址类型（档案资料：0，案卷PDF：1）',
  `file_suffix` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '文件后缀',
  `size` bigint NULL DEFAULT 0 COMMENT '文件大小',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案资料和案卷PDFURL存放表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_flow`;
CREATE TABLE `bs_arc_flow`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案id',
  `flow_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '流转名称',
  `flow_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转备注',
  `flow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流转时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案流程记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_folder_acc
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_folder_acc`;
CREATE TABLE `bs_arc_folder_acc`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_id` bigint UNSIGNED NOT NULL COMMENT '档案盒Id',
  `folder_id` bigint UNSIGNED NOT NULL COMMENT '案卷Id',
  `folder_shape` int NOT NULL DEFAULT 0 COMMENT '会计系统-案卷形态，0:电子，1：纸质',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '会计-档案-案卷关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_middle
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_middle`;
CREATE TABLE `bs_arc_middle`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `primary_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '第三方唯一编号',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '基础内容',
  `extend` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '扩展内容',
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '状态: 未上传文件，已上传文件，已归档到档案表',
  `file_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '文件列表',
  `system_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '系统名称',
  `system_type` int NULL DEFAULT 0 COMMENT '0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除(0否，1是)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '第三方上传中间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_rule
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_rule`;
CREATE TABLE `bs_arc_rule`  (
  `rule_id` bigint UNSIGNED NOT NULL,
  `rule_way` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号生成规则方式',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案号生成规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_tem
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_tem`;
CREATE TABLE `bs_arc_tem`  (
  `tem_id` bigint UNSIGNED NOT NULL COMMENT '扩展字段id',
  `arc_type_id` bigint UNSIGNED NOT NULL COMMENT '档案类型ID',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `is_primary` int NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `is_show` int NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `is_sign` int NOT NULL DEFAULT 0 COMMENT '是否显示在影像页面 0：不显示，1：显示',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框 3:年月框',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `index_no` int NOT NULL DEFAULT 0,
  `is_list` int NULL DEFAULT NULL COMMENT '是否显示在列表  0:不显示，1：显示',
  `is_folder` int NULL DEFAULT NULL COMMENT '是否同时是案卷类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`tem_id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '扩展字段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_tem_content`;
CREATE TABLE `bs_arc_tem_content`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `relation_id` bigint UNSIGNED NOT NULL COMMENT '档案id',
  `tem_id` bigint UNSIGNED NOT NULL COMMENT '扩展字段ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展字段内容',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `is_show` int NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `is_primary` int NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`relation_id`) USING BTREE,
  INDEX `idx_tem_id`(`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '扩展字段详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_type
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_type`;
CREATE TABLE `bs_arc_type`  (
  `arc_type_id` bigint UNSIGNED NOT NULL COMMENT '档案分类id',
  `arc_type_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案分类号',
  `name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类',
  `type` int NOT NULL DEFAULT 0 COMMENT '0:办公、1：项目、2：业务、3：人事',
  `newlevel` int NULL DEFAULT 0 COMMENT '分类级别',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态',
  `secrets` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '密级',
  `keep_year` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '999' COMMENT '保管年限 10：保管10年、30：保管30年、999永久保存',
  `shapes` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '档案形态0纸质及电子、1纸质、2电子、3实物件',
  `rules` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '#全宗号#-#年份#-#顺序号#' COMMENT '档案号拼接规则',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '分类序号',
  `is_material` int NULL DEFAULT NULL COMMENT '是否维护材料清单 0是 1否',
  `ele_arc_menu` int NULL DEFAULT NULL COMMENT '电子档目录来源 0档案系统 1影像系统',
  `acc_period_type` int NULL DEFAULT NULL COMMENT '会计期间 0:月度；1：季度；2：半年度；3:年度',
  `acc_flag` int NULL DEFAULT NULL COMMENT '分类标记',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `folder_show` int NULL DEFAULT NULL COMMENT '是否在案卷中展示,0:不展示，1:展示',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  `acc_ecm_collection` int NULL DEFAULT NULL COMMENT '电子影像收集 0附件关联 1影像上传',
  PRIMARY KEY (`arc_type_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_type_box
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_type_box`;
CREATE TABLE `bs_arc_type_box`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_type_id` bigint UNSIGNED NOT NULL COMMENT '档案类型Id',
  `box_id` bigint UNSIGNED NOT NULL COMMENT '档案盒Id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案类型-档案盒关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_arc_type_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_arc_type_folder`;
CREATE TABLE `bs_arc_type_folder`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_type_id` bigint UNSIGNED NOT NULL COMMENT '档案类型Id',
  `folder_id` bigint UNSIGNED NOT NULL COMMENT '案卷Id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案类型-案卷关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_attachment
-- ----------------------------
DROP TABLE IF EXISTS `bs_attachment`;
CREATE TABLE `bs_attachment`  (
  `file_id` bigint NOT NULL COMMENT 'ID',
  `file_index` int NULL DEFAULT NULL COMMENT '序号',
  `file_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '编号',
  `file_type` bigint NULL DEFAULT NULL COMMENT '附件类型ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '附件名称',
  `file_num` int NULL DEFAULT NULL COMMENT '文件数量',
  `happened_time` datetime NULL DEFAULT NULL COMMENT '发生时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `file_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '来源',
  `push_time` datetime NULL DEFAULT NULL COMMENT '推送时间',
  `third_param` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方参数（影像）',
  `is_relation` int NULL DEFAULT 0 COMMENT '是否关联 0是 1否',
  `system_code` int NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` int NULL DEFAULT 0,
  `file_form` int NULL DEFAULT NULL COMMENT '附件形态 0电子及纸质 1纸质 2电子',
  `receipt_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '单据编号',
  `relate_proof` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '关联凭证',
  PRIMARY KEY (`file_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '附件管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow`;
CREATE TABLE `bs_borrow`  (
  `borrow_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `borrow_inst` bigint UNSIGNED NULL DEFAULT NULL COMMENT '借阅机构',
  `borrow_no` int NOT NULL COMMENT '借阅编号',
  `borrow_dept_id` bigint NULL DEFAULT NULL COMMENT '借阅人部门Id',
  `borrow_dept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '借阅人部门',
  `borrow_way` int NOT NULL COMMENT '借阅方式（0：电子阅览  1：电子下载  2：电子打印  3：纸质原件  4：纸质复印  5：纸质阅览）',
  `borrow_per_type` int NOT NULL DEFAULT 0 COMMENT '借阅人类型：0：本人借阅，1：他人内部借阅，2：他人外链借阅',
  `borrow_per` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '借阅人',
  `borrow_per_id` bigint NULL DEFAULT NULL COMMENT '借阅人Id',
  `per_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '证件/员工编号',
  `per_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '联系电话',
  `borrow_reason` int NULL DEFAULT NULL COMMENT '借阅原因（0：编史修志 1：工作查考  2：学术研究  3：经济建设  4：宣传教育  5：其他）',
  `reason_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '原因描述',
  `borrow_day` int NOT NULL COMMENT '借阅天数',
  `borrow_submitter` bigint NOT NULL COMMENT '申请提交人',
  `borrow_submitter_str` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申请提交人名称',
  `borrow_status` int NOT NULL DEFAULT 0 COMMENT '借阅状态 （0： 待审批  1：审批不通过   2：审批通过  3：确认借出 ）',
  `examine_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批备注',
  `return_status` int NULL DEFAULT NULL COMMENT '借阅有效状态 （0：待归还  1：部分归还  2：已归还  3：有效   4：失效  5: 部分有效）',
  `examine_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '到期时间',
  `return_time` datetime NULL DEFAULT NULL COMMENT '归还时间',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `third_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `examine_user` bigint NULL DEFAULT NULL COMMENT '借阅登记人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `system_code` int NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `app_eff_time` datetime NULL DEFAULT NULL COMMENT '申请有效时间',
  `borrow_type` int NULL DEFAULT NULL COMMENT '借阅类型 0:档案（卷）借阅,1:资料借阅',
  `borrow_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '借阅凭证url',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`borrow_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅申请表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_content`;
CREATE TABLE `bs_borrow_content`  (
  `bor_content_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键 （借阅内容id） ',
  `borrow_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '借阅id',
  `arc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号',
  `folder_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷号',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名',
  `content_id` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '借阅内容详细id（以逗号分隔）',
  `borrow_type` int NULL DEFAULT NULL COMMENT '借阅类型 （ 0:整件档案借阅:  1:部分文件资料借阅）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `placecode_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '库房位置码id',
  `content_status` int NULL DEFAULT 0 COMMENT '归还状态（0:  待确认 1：待归还  2：已归还  3:  有效   4：失效）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`bor_content_id`) USING BTREE,
  INDEX `idx_borrow_id`(`borrow_id`) USING BTREE,
  INDEX `idx_content_id`(`content_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅申请内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_content_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_content_arc`;
CREATE TABLE `bs_borrow_content_arc`  (
  `id` bigint NOT NULL COMMENT '主键',
  `borrow_id` bigint NOT NULL COMMENT '借阅id',
  `borrow_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '借阅的版本号',
  `arc_id` bigint NOT NULL COMMENT '关联的档案id',
  `folder_id` bigint NULL DEFAULT NULL COMMENT '关联案卷id',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  `third_param` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方参数（影像参数）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_borrow_id`(`borrow_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅档案与案卷关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_content_materials
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_content_materials`;
CREATE TABLE `bs_borrow_content_materials`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `bor_content_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '借阅内容id',
  `materials_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '材料id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bor_content_id`(`bor_content_id`) USING BTREE,
  INDEX `idx_materials_id`(`materials_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅内容对应资料表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_examine
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_examine`;
CREATE TABLE `bs_borrow_examine`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审批人id',
  `examine_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `examine_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_remakrs` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批备注',
  `examine_result` int NULL DEFAULT NULL COMMENT '审批结果 1不同意 2同意',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_process_instance_id`(`process_instance_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅审批记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_flow`;
CREATE TABLE `bs_borrow_flow`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `borrow_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案id',
  `flow_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转名称',
  `flow_user` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '批审备注',
  `flow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流转时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_borrow_id`(`borrow_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_share
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_share`;
CREATE TABLE `bs_borrow_share`  (
  `share_id` bigint NOT NULL COMMENT '分享ID',
  `borrow_id` bigint NOT NULL COMMENT '借阅ID',
  `link_type` int NULL DEFAULT NULL COMMENT '分享外链类别 0公开 1密码',
  `link_pwd` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '链接密码',
  `link_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '链接地址',
  `share_user_id` bigint NOT NULL COMMENT '分享人id',
  `share_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分享人',
  `share_state` int NULL DEFAULT 0 COMMENT '分享状态：0有效，1失效',
  `system_code` int NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `invalid_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0,
  PRIMARY KEY (`share_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅外链分享表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_borrow_urge
-- ----------------------------
DROP TABLE IF EXISTS `bs_borrow_urge`;
CREATE TABLE `bs_borrow_urge`  (
  `urge_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `borrow_inst` bigint NULL DEFAULT NULL COMMENT '借阅机构',
  `borrow_per` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '借阅人',
  `borrow_way` int NOT NULL COMMENT '借阅方式（0：电子阅览  1：电子下载  2：电子打印  3：纸质原件  4：纸质复印  5：纸质阅览）',
  `borrow_time` datetime NOT NULL COMMENT '借阅日期',
  `expire_time` datetime NOT NULL COMMENT '到期时间',
  `urge_way` int NOT NULL COMMENT '催还方式（0:消息提醒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `urge_level` int NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`urge_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '借阅催还记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_box
-- ----------------------------
DROP TABLE IF EXISTS `bs_box`;
CREATE TABLE `bs_box`  (
  `box_id` bigint UNSIGNED NOT NULL COMMENT '档案盒主键',
  `box_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案盒号',
  `box_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案盒题名',
  `box_type` bigint NULL DEFAULT NULL COMMENT '档案盒类型',
  `box_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创键人',
  `box_dept` bigint NULL DEFAULT NULL COMMENT '所属部门ID',
  `year` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '年份',
  `case_num` int NOT NULL DEFAULT 0 COMMENT '档案件数',
  `update_user` bigint NULL DEFAULT NULL COMMENT '最近修改人',
  `placecode_id` bigint NULL DEFAULT NULL COMMENT '位置码id ',
  `placecode_str` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '库房位置码',
  `acc_period` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计期间 会计期间类型为月度:1-12对应1-12月;季度:1-4对应1-4季度;半年度:1对应上半年,2对应下半年',
  `box_level` int NOT NULL DEFAULT 0 COMMENT ' \'系统分类:0办公，1财务，2业务，3人事\',',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-单位名称',
  `folder_start` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-案卷顺序号码起',
  `folder_end` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-案卷顺序号码止',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`box_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案盒表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_box_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_box_folder`;
CREATE TABLE `bs_box_folder`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `box_id` bigint UNSIGNED NOT NULL COMMENT '档案盒Id',
  `folder_id` bigint UNSIGNED NOT NULL COMMENT '案卷Id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案盒-案卷关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_destroy
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy`;
CREATE TABLE `bs_destroy`  (
  `destroy_id` bigint UNSIGNED NOT NULL COMMENT '销毁id',
  `destroy_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销毁编号',
  `destroy_shape` int NULL DEFAULT NULL COMMENT '销毁形态 0纸质 1电子 2纸质及电子',
  `destroy_type` int NULL DEFAULT NULL COMMENT '销毁方式',
  `destroy_inst` bigint NULL DEFAULT NULL COMMENT '销毁机构',
  `destroy_dept` bigint NULL DEFAULT NULL COMMENT '销毁部门',
  `destroy_user` bigint NULL DEFAULT NULL COMMENT '销毁人',
  `monitor_inst` bigint NULL DEFAULT NULL COMMENT '监销机构',
  `monitor_dept` bigint NULL DEFAULT NULL COMMENT '监销部门',
  `monitor_user` bigint NULL DEFAULT NULL COMMENT '监销人',
  `destroy_time` datetime NULL DEFAULT NULL COMMENT '销毁时间',
  `state` int NULL DEFAULT NULL COMMENT '状态  0:待销毁 1:已销毁',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '销毁原因',
  `destroy_file_type` int NULL DEFAULT 0 COMMENT '销毁的资料类型(0档案,1案卷)',
  `destroy_level` int NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`destroy_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销毁记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_destroy_apply
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_apply`;
CREATE TABLE `bs_destroy_apply`  (
  `apply_id` bigint UNSIGNED NOT NULL COMMENT '申请id',
  `apply_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申请编号',
  `apply_index` bigint NULL DEFAULT NULL COMMENT '申请记录顺序',
  `apply_inst` bigint NULL DEFAULT NULL COMMENT '申请机构',
  `apply_dept` bigint NULL DEFAULT NULL COMMENT '申请部门',
  `apply_user` bigint NULL DEFAULT NULL COMMENT '申请人',
  `apply_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申请原因',
  `apply_opera` int NULL DEFAULT NULL COMMENT '申请操作',
  `apply_state` int NULL DEFAULT NULL COMMENT '申请（审批） 0待审批 1审批中 2已审批 ',
  `apply_time` datetime NULL DEFAULT NULL COMMENT '申请时间',
  `apply_source` int NOT NULL DEFAULT 0 COMMENT '申请来源（默认0）  0：办公 1：会计',
  `examine_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审批人',
  `examine_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_result` int NULL DEFAULT NULL COMMENT '审批结果： 0同意 1不同意',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `handle_result` int NULL DEFAULT 0 COMMENT '处理结果 0未销毁，1已销毁',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`apply_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销毁申请记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_destroy_apply_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_apply_arc`;
CREATE TABLE `bs_destroy_apply_arc`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `apply_id` bigint UNSIGNED NOT NULL COMMENT '申请id',
  `arc_id` bigint UNSIGNED NOT NULL COMMENT '档案id',
  `storage_period` int NULL DEFAULT NULL COMMENT '已保管时间(年)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_apply_id`(`apply_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '申请-档案关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_destroy_examine
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_examine`;
CREATE TABLE `bs_destroy_examine`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `examine_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审批人id',
  `examine_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_result` int NULL DEFAULT NULL COMMENT '审批结果： 0同意 1不同意',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '销毁审批记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_destroy_identify
-- ----------------------------
DROP TABLE IF EXISTS `bs_destroy_identify`;
CREATE TABLE `bs_destroy_identify`  (
  `identify_id` bigint UNSIGNED NOT NULL COMMENT '鉴定id',
  `identify_inst` bigint NULL DEFAULT NULL COMMENT '鉴定机构',
  `identify_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '鉴定编号',
  `identify_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鉴定日期',
  `identify_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定意见',
  `identify_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定人',
  `identify_result` int NULL DEFAULT NULL COMMENT '鉴定结果 0:保留 1:销毁',
  `identify_arc` bigint NULL DEFAULT NULL COMMENT '鉴定档案id,案卷id',
  `identify_dept` bigint NULL DEFAULT NULL COMMENT '鉴定申请部门',
  `state` int NULL DEFAULT NULL COMMENT '状态 0:待鉴定 1:已鉴定',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定申请原因',
  `handle_state` int NULL DEFAULT NULL COMMENT '处理状态0应延期1已延期2应销毁3待销毁4销毁中5已销毁',
  `identify_report` int(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '鉴定报告状态 0未上传1已上传',
  `report_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定报告本机地址',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `third_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '鉴定材料上传影像地址',
  `identify_type` int NULL DEFAULT 0 COMMENT '鉴定资料类型默认为0(0:档案,1:案卷)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`identify_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案鉴定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_file
-- ----------------------------
DROP TABLE IF EXISTS `bs_file`;
CREATE TABLE `bs_file`  (
  `id` bigint NOT NULL COMMENT '主键',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名称',
  `file_type` int NULL DEFAULT NULL COMMENT '文件类型，1:借阅,2:鉴定,3:PDF文件上传',
  `file_suffix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件后缀',
  `file_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件唯一编码',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `status` int NULL DEFAULT NULL COMMENT '上传状态',
  `msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '返回信息',
  `pageid` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '影像返回唯一值',
  `md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '影像返回文件唯一值',
  `system_code` int NULL DEFAULT NULL COMMENT '系统类型type 0办公，1财务，2业务，3人事',
  `is_cms_deleted` int NULL DEFAULT 0 COMMENT '是否上传影像（0未上传影像 1上传成功 2删除成功）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder`;
CREATE TABLE `bs_folder`  (
  `folder_id` bigint UNSIGNED NOT NULL COMMENT '案卷主键',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '上级卷ID',
  `folder_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '案卷号',
  `folder_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷标题',
  `folder_type_id` bigint UNSIGNED NOT NULL COMMENT '案卷类型ID',
  `folder_shape` int NOT NULL DEFAULT 0 COMMENT '会计系统-案卷形态，0:电子，1：纸质',
  `folder_inst` bigint NULL DEFAULT NULL COMMENT '所属机构id',
  `folder_dept` bigint NULL DEFAULT NULL COMMENT '所属部门id',
  `folder_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '立卷人',
  `year` int NULL DEFAULT NULL COMMENT '年度',
  `case_num` int NOT NULL DEFAULT 0 COMMENT '件数',
  `page_num` int NOT NULL DEFAULT 0 COMMENT '页数',
  `update_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '最近修改人',
  `placecode_id` bigint NULL DEFAULT NULL COMMENT '位置码id',
  `placecode_str` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '库房位置码',
  `acc_period` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '1' COMMENT '会计期间 会计期间类型为月度:1-12对应1-12月;季度:1-4对应1-4季度;半年度:1对应上半年,2对应下半年',
  `folder_level` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '系统分类:0办公，1财务，2业务，3人事',
  `status` int NULL DEFAULT NULL COMMENT '会计系统-0待归档 2通过',
  `responsibltor` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-责任人',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-单位名称',
  `cred_start` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-记账凭证号码起',
  `cred_end` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-记账凭证号码止',
  `serial_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-案卷顺序号',
  `folder_lock` int NOT NULL DEFAULT 0 COMMENT '会计系统-案卷表-案卷标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定 6转递锁定 7鉴定锁定 8已销毁锁定 业务系统:0',
  `store_date` datetime NULL DEFAULT NULL COMMENT '保管终止日期(yyyy)',
  `keep_year` int NULL DEFAULT NULL COMMENT '保管年限 默认(999永久 10保存10年 30保存30年)',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `warehousing_time` datetime NULL DEFAULT NULL COMMENT '入库时间',
  `folder_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`folder_id`) USING BTREE,
  INDEX `idx_folder_type_id`(`folder_type_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_acc_audit
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_acc_audit`;
CREATE TABLE `bs_folder_acc_audit`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `folder_id` bigint NOT NULL DEFAULT 0 COMMENT '案卷ID',
  `sub_user` bigint NULL DEFAULT NULL COMMENT '发起归档审核人(提交人)',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '会计案卷-审核流程关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_autorule
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_autorule`;
CREATE TABLE `bs_folder_autorule`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `type_id` bigint UNSIGNED NOT NULL COMMENT '分类id',
  `method` int NULL DEFAULT NULL COMMENT '组卷方式 0:手动，1:自动',
  `rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷号生成规则',
  `title_rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名生成规则id',
  `system_code` int NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `auto_type` int NULL DEFAULT 0 COMMENT '自动组卷的类型，0:自动立卷，1：自动组卷',
  `auto_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '规则的备注和描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_id`(`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '自动组卷配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_dept
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_dept`;
CREATE TABLE `bs_folder_dept`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `folder_id` bigint NOT NULL COMMENT '关联案卷表案卷id',
  `dept_id` bigint NOT NULL COMMENT '保管部门id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_flow`;
CREATE TABLE `bs_folder_flow`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `folder_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '案卷id',
  `flow_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '流转名称',
  `flow_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转备注',
  `flow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流转时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷流程记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_no_rule_conf
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_no_rule_conf`;
CREATE TABLE `bs_folder_no_rule_conf`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置选项名称',
  `examp` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `defaultValue` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '配置选项值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷号生成规则配置选项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_tem
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_tem`;
CREATE TABLE `bs_folder_tem`  (
  `folder_tem_id` bigint UNSIGNED NOT NULL COMMENT '扩展字段id',
  `folder_type_id` bigint UNSIGNED NOT NULL COMMENT '案卷类型ID',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `is_primary` int NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `is_show` int NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`folder_tem_id`) USING BTREE,
  INDEX `idx_folder_type_id`(`folder_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷扩展字段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_tem_content`;
CREATE TABLE `bs_folder_tem_content`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `folder_id` bigint UNSIGNED NULL DEFAULT NULL,
  `tem_id` bigint UNSIGNED NULL DEFAULT NULL,
  `tem_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE,
  INDEX `idx_tem_id`(`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷类型扩展字段内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_folder_type
-- ----------------------------
DROP TABLE IF EXISTS `bs_folder_type`;
CREATE TABLE `bs_folder_type`  (
  `folder_type_id` bigint UNSIGNED NOT NULL COMMENT '案卷分类主键',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级分类',
  `folder_type_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '案卷分类号',
  `name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `rules` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷号自动拼接规则',
  `type` int NOT NULL DEFAULT 0 COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '分类序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`folder_type_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_inout_storage
-- ----------------------------
DROP TABLE IF EXISTS `bs_inout_storage`;
CREATE TABLE `bs_inout_storage`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `placecode_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '位置码id',
  `arc_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案id',
  `operation_type` int NULL DEFAULT NULL COMMENT '操作类型（0：入库   1：出库）',
  `operation_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `store_inst` bigint NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '出入库记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_ldap_middle
-- ----------------------------
DROP TABLE IF EXISTS `bs_ldap_middle`;
CREATE TABLE `bs_ldap_middle`  (
  `id` bigint UNSIGNED NOT NULL,
  `type` int NULL DEFAULT NULL COMMENT '类型 0组织 1用户',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '参数体',
  `inst_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '机构id',
  `dept_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '部门id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除:0否 1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = 'LDAP中间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_materials
-- ----------------------------
DROP TABLE IF EXISTS `bs_materials`;
CREATE TABLE `bs_materials`  (
  `materials_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '材料id',
  `materials_type_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '材料类型id',
  `arc_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '材料名称',
  `shape` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '材料形态 0 实物及电子 1实物  2 电子',
  `page` int NULL DEFAULT NULL COMMENT '页数',
  `form_time` datetime NULL DEFAULT NULL COMMENT '形成时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`materials_id`) USING BTREE,
  INDEX `idx_materials_type_id`(`materials_type_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '材料表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_materials_type
-- ----------------------------
DROP TABLE IF EXISTS `bs_materials_type`;
CREATE TABLE `bs_materials_type`  (
  `materials_type_id` bigint UNSIGNED NOT NULL COMMENT '材料分类id',
  `materials_type_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '材料分类号',
  `arc_type_id` bigint UNSIGNED NOT NULL COMMENT '档案分类id',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '材料分类id-父级',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '材料序号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '材料分类名称',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（暂不用）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `materials_reveivable` int NULL DEFAULT NULL COMMENT '应收材料的数量',
  `materials_shape` int NULL DEFAULT NULL COMMENT '档案形态:0纸质及电子 1 纸质档案 2 电子档案 3实物件 ',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`materials_type_id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '材料类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_materials_type_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_materials_type_arc`;
CREATE TABLE `bs_materials_type_arc`  (
  `id` bigint UNSIGNED NOT NULL,
  `arc_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案id',
  `materials_type_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '材料分类id',
  `materials_paper_status` int NULL DEFAULT 0 COMMENT '纸质收集状态0:收集中，1:已完成，2:不需要收集',
  `materials_elect_status` int NULL DEFAULT 0 COMMENT '电子收集状态 0:收集中，1:已完成，2:异常',
  `materials_practical` int NULL DEFAULT 0 COMMENT '实收数量',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_materials_type_id`(`materials_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '材料分类-档案关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_message
-- ----------------------------
DROP TABLE IF EXISTS `bs_message`;
CREATE TABLE `bs_message`  (
  `message_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '提醒用户id',
  `message_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '消息标题',
  `message_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '消息内容',
  `message_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '消息类型',
  `message_status` int NULL DEFAULT 0 COMMENT '阅读状态（0：未读    1：已读）',
  `inform_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
  `system_code` int NOT NULL DEFAULT 0 COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '消息提示表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_perwaitfor
-- ----------------------------
DROP TABLE IF EXISTS `bs_perwaitfor`;
CREATE TABLE `bs_perwaitfor`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `init_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '申请Id',
  `flow_per` bigint NULL DEFAULT NULL COMMENT '流程当前处理人id',
  `wait_type` int NULL DEFAULT NULL COMMENT '代办事项类型(0：借阅申请待审批   1：销毁申请待审批)',
  `init_user` bigint NULL DEFAULT NULL COMMENT '发起人',
  `init_dpt` bigint NULL DEFAULT NULL COMMENT '发起部门',
  `init_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
  `top_status` int NULL DEFAULT 0 COMMENT '置顶状态  （0：不置顶  1：置顶）',
  `system_code` int NOT NULL DEFAULT 0 COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `extend_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_init_id`(`init_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '待办事项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_placecode
-- ----------------------------
DROP TABLE IF EXISTS `bs_placecode`;
CREATE TABLE `bs_placecode`  (
  `placecode_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '位置码id',
  `warehouse_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '库房id',
  `placecode_no` bigint NOT NULL DEFAULT 0 COMMENT '位置码-数字',
  `placecode_str` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '位置码-字符串',
  `use_state` int NULL DEFAULT 0 COMMENT '使用状态(未使用:0,已使用:1)',
  `capacity_state` int NULL DEFAULT 1 COMMENT '容量状态(未满:0,空闲:1,已满:2) ',
  `system_code` int NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`placecode_id`) USING BTREE,
  INDEX `idx_warehouse_id`(`warehouse_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '位置码表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_progress
-- ----------------------------
DROP TABLE IF EXISTS `bs_progress`;
CREATE TABLE `bs_progress`  (
  `progress_id` bigint NOT NULL COMMENT 'ID',
  `arc_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案状态:0收集中 1待归档 2已归档 3已销毁 4已遗失 5部门移交 6已删除',
  `arc_type_id` bigint NULL DEFAULT NULL COMMENT '档案类型id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案题名',
  `sounce` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案来源',
  `have_folder` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '是否需要查询暂无关联的案卷的凭证',
  `arc_shape` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案形态: 0 纸质及电子 1 纸质档案 2 电子档案 3实物件',
  `arc_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号',
  `archives_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案来源 ：AM档案系统 TM,OA',
  `exam_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审核状态',
  `sort` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '排序顺序（ asc，desc）',
  `des_start_time` datetime NULL DEFAULT NULL COMMENT '著录日期始',
  `des_end_time` datetime NULL DEFAULT NULL COMMENT '著录日期止',
  `tem_content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '额外信息（高级搜索）',
  `arc_commit_num` bigint NULL DEFAULT NULL COMMENT '提交的档案总数',
  `arc_commit_index` int NULL DEFAULT NULL COMMENT '提交序号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除:0否-1是',
  PRIMARY KEY (`progress_id`) USING BTREE,
  INDEX `arc_type_id`(`arc_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '进度条查询表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_rel_auto_rule
-- ----------------------------
DROP TABLE IF EXISTS `bs_rel_auto_rule`;
CREATE TABLE `bs_rel_auto_rule`  (
  `id` bigint NOT NULL COMMENT '主键',
  `rel_id` bigint NOT NULL COMMENT '关联的id',
  `auto_rule` int NULL DEFAULT NULL COMMENT '规则：0:不等于，1:等于，2:唯一',
  `col_type` int NULL DEFAULT NULL COMMENT '字段属性，0:基本属性，1:扩展属性',
  `col_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案字段的key',
  `col_val` varchar(9999) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案字段的值仅基本属性中存在',
  `col_val_type` int NULL DEFAULT NULL COMMENT '下拉形式，0:树下拉，1:列表下拉',
  `rule_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse`;
CREATE TABLE `bs_reverse`  (
  `reverse_id` bigint NOT NULL COMMENT '反归档id',
  `reverse_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '申请编号',
  `type_id` bigint NULL DEFAULT NULL COMMENT '分类id，档案类型',
  `rever_type` int NULL DEFAULT NULL COMMENT '反归档类型0:资料，1:案卷',
  `rever_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名',
  `apply_date` datetime NULL DEFAULT NULL COMMENT '申请时间',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '申请原因',
  `status` int NULL DEFAULT NULL COMMENT '审核状态0:待审核，1:审核中，2:审核通过，3:审核拒绝',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `manage_inst` bigint NULL DEFAULT NULL COMMENT '所属机构 ',
  `manage_dept` bigint NULL DEFAULT NULL COMMENT '所属部门 ',
  `manage_user` bigint NULL DEFAULT NULL COMMENT '责任人    创建人',
  `system_code` int NOT NULL COMMENT '系统分类:0办公，1财务，2业务，3人事',
  `folder_shape` int NULL DEFAULT NULL COMMENT '形态 0电子 1纸质',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '最近修改人',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`reverse_id`) USING BTREE,
  INDEX `idx_type_id`(`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_arc`;
CREATE TABLE `bs_reverse_arc`  (
  `id` bigint NOT NULL COMMENT '主键',
  `arc_id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `arc_no` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号',
  `reference_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文号/项目编号',
  `arc_type_id` bigint NULL DEFAULT NULL COMMENT '档案分类id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名',
  `arc_level` int NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `secret_level` int NULL DEFAULT NULL COMMENT '档案密级:0公开 1内部 2工作秘密',
  `arc_shape` int NULL DEFAULT NULL COMMENT '档案形态:0纸质及电子 1 纸质档案 2 电子档案 3实物件 ',
  `arc_lock` int NULL DEFAULT 0 COMMENT '档案标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定',
  `status` int NULL DEFAULT 0 COMMENT '档案状态:0收集中 1待归档 2已归档 3已销毁 4已遗失 5部门移交 6已删除 7反归档',
  `keep_year` int NULL DEFAULT NULL COMMENT '保管年限999永久 10保存10年 30保存30年',
  `check_date` datetime NULL DEFAULT NULL COMMENT '归档日期',
  `page_no` int NULL DEFAULT NULL COMMENT '页数',
  `submit_date` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_inst` bigint NULL DEFAULT NULL COMMENT '建档机构',
  `create_dept` bigint NULL DEFAULT NULL COMMENT '建档部门',
  `create_user` bigint NULL DEFAULT NULL COMMENT '建档收集人',
  `create_date` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建档日期',
  `arc_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '年度',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `store_user` bigint NULL DEFAULT NULL COMMENT '保管人',
  `store_date` datetime NULL DEFAULT NULL COMMENT '保管终止日期',
  `manage_inst` bigint NULL DEFAULT NULL COMMENT '所属机构 档案业务发生机构',
  `manage_dept` bigint NULL DEFAULT NULL COMMENT '所属部门 档案业务发生部门',
  `manage_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '责任人    档案业务责任人',
  `archives_date` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成文日期 档案业务形成日期',
  `check_user` bigint NULL DEFAULT NULL COMMENT '归档检查人',
  `third_param` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方参数（影像参数）',
  `archives_source` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案来源 ：AM档案系统 TM,OA',
  `external_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '外部接入的档案唯一性记录',
  `placecode_id` bigint NULL DEFAULT NULL COMMENT '位置码id',
  `box_id` bigint NOT NULL DEFAULT 0 COMMENT '档案盒id',
  `box_user` bigint NULL DEFAULT NULL COMMENT '装盒人',
  `box_time` datetime NULL DEFAULT NULL COMMENT '装盒时间',
  `box_num` int NULL DEFAULT NULL COMMENT '序号 装盒',
  `folder_id` bigint(16) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '案卷id',
  `folder_no` int NULL DEFAULT NULL COMMENT '件号 组卷',
  `folder_time` datetime NULL DEFAULT NULL COMMENT '组卷时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `is_material` int NULL DEFAULT NULL COMMENT '是否维护材料清单 0是 1否',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除:0否-1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_arc_type_id`(`arc_type_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE,
  INDEX `idx_box_id`(`box_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_arc_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_arc_tem_content`;
CREATE TABLE `bs_reverse_arc_tem_content`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `rel_arc_id` bigint NULL DEFAULT NULL COMMENT '反归档备份表的主键',
  `relation_id` bigint NOT NULL COMMENT '档案id',
  `tem_id` bigint NOT NULL COMMENT '扩展字段ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展字段内容',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `is_show` int NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `is_primary` int NOT NULL DEFAULT 0 COMMENT '是否必输 0：是 1:否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rel_arc_id`(`rel_arc_id`) USING BTREE,
  INDEX `idx_arc_id`(`relation_id`) USING BTREE,
  INDEX `idx_tem_id`(`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档扩展字段详情表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_examine
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_examine`;
CREATE TABLE `bs_reverse_examine`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审批人id',
  `examine_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `examine_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `examine_remakrs` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批备注',
  `examine_result` int NULL DEFAULT NULL COMMENT '审批结果 1不同意 2同意',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档审批记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_flow
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_flow`;
CREATE TABLE `bs_reverse_flow`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `reverse_id` bigint NULL DEFAULT NULL COMMENT '反归档申请id',
  `flow_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转名称',
  `flow_user` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '操作人',
  `flow_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批意见',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '批审备注',
  `flow_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '流转时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_reverse_id`(`reverse_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_folder`;
CREATE TABLE `bs_reverse_folder`  (
  `id` bigint NOT NULL COMMENT '主键',
  `folder_id` bigint NOT NULL COMMENT '案卷主键',
  `folder_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '案卷号',
  `folder_title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷标题',
  `folder_type_id` bigint NULL DEFAULT NULL COMMENT '案卷类型ID',
  `folder_inst` bigint NULL DEFAULT NULL COMMENT '所属机构id',
  `folder_dept` bigint NULL DEFAULT NULL COMMENT '所属部门id',
  `folder_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '立卷人',
  `year` int NULL DEFAULT NULL COMMENT '年度',
  `case_num` int NOT NULL DEFAULT 0 COMMENT '件数',
  `page_num` int NOT NULL DEFAULT 0 COMMENT '页数',
  `submit_date` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `update_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '最近修改人',
  `placecode_id` bigint NULL DEFAULT NULL COMMENT '位置码id',
  `placecode_str` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '库房位置码',
  `acc_period` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '1' COMMENT '会计期间 会计期间类型为月度:1-12对应1-12月;季度:1-4对应1-4季度;半年度:1对应上半年,2对应下半年',
  `folder_level` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '系统分类:0办公，1财务，2业务，3人事',
  `status` int NULL DEFAULT NULL COMMENT '会计系统-0待归档 2通过',
  `responsibltor` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-责任人',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-单位名称',
  `cred_start` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-记账凭证号码起',
  `cred_end` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-记账凭证号码止',
  `serial_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计系统-案卷顺序号',
  `folder_lock` int NOT NULL DEFAULT 0 COMMENT '会计系统-案卷表-案卷标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定 6转递锁定 7鉴定锁定 8已销毁锁定',
  `store_date` datetime NULL DEFAULT NULL COMMENT '保管终止日期(yyyy)',
  `keep_year` int NULL DEFAULT NULL COMMENT '保管年限 默认(999永久 10保存10年 30保存30年)',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `warehousing_time` datetime NULL DEFAULT NULL COMMENT '入库时间',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE,
  INDEX `idx_folder_type_id`(`folder_type_id`) USING BTREE,
  INDEX `idx_placecode_id`(`placecode_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档案卷表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_folder_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_folder_arc`;
CREATE TABLE `bs_reverse_folder_arc`  (
  `id` bigint NOT NULL COMMENT '主键',
  `arc_id` bigint NULL DEFAULT NULL COMMENT '关联档案id',
  `folder_id` bigint NULL DEFAULT NULL COMMENT '关联案卷id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除:0否-1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档案卷和档案关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_inspect_examine
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_inspect_examine`;
CREATE TABLE `bs_reverse_inspect_examine`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `examine_user` bigint NULL DEFAULT NULL COMMENT '审批人ID',
  `examine_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `examine_result` int NULL DEFAULT NULL COMMENT '审批结果 1 同意 2不同意',
  `examine_remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '审批备注',
  `rel_id` bigint NULL DEFAULT NULL COMMENT '反归档案卷或资料记录ID',
  `process_instance_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流程ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rel_id`(`rel_id`) USING BTREE,
  INDEX `idx_process_instance_id`(`process_instance_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档审批流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_reverse_rel
-- ----------------------------
DROP TABLE IF EXISTS `bs_reverse_rel`;
CREATE TABLE `bs_reverse_rel`  (
  `id` bigint NOT NULL COMMENT '主键',
  `reverse_id` bigint NOT NULL COMMENT '反归档id',
  `rel_id` bigint NULL DEFAULT NULL COMMENT '关联的id，案卷或者档案的id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rel_id`(`rel_id`) USING BTREE,
  INDEX `idx_reverse_id`(`reverse_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '反归档关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_transfer
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer`;
CREATE TABLE `bs_transfer`  (
  `transfer_id` bigint UNSIGNED NOT NULL COMMENT '移交id',
  `transfer_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '移交编号',
  `transfer_user` bigint NOT NULL COMMENT '移交人id',
  `transfer_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '移交人名',
  `transfer_org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '移交组织名',
  `transfer_org` bigint NULL DEFAULT NULL COMMENT '移交组织',
  `transfer_date` datetime NULL DEFAULT NULL COMMENT '移交时间',
  `transfer_type` int NOT NULL COMMENT '移交类型 0 档案移交 1材料移交',
  `transfer_state` int NULL DEFAULT NULL COMMENT '移交状态 0 移交中（待接收） 1已接收 2移交退回',
  `receive_user` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '通知人id',
  `receive_user_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '通知人名',
  `receive_org_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '接收组织名',
  `receive_org` bigint NULL DEFAULT NULL COMMENT '接收组织',
  `receive_date` datetime NULL DEFAULT NULL COMMENT '接收时间',
  `re_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '退回原因或者接收原因',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `receiver` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '接收通过(接收人),退回(退回操作人)',
  `transfer_level` int NOT NULL DEFAULT 0 COMMENT '会计系统-案卷移交表-系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`transfer_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '移交记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_transfer_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_arc`;
CREATE TABLE `bs_transfer_arc`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `transfer_id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `arc_id` bigint UNSIGNED NOT NULL COMMENT '档案id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transfer_id`(`transfer_id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '移交档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_transfer_folder
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_folder`;
CREATE TABLE `bs_transfer_folder`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `transfer_id` bigint UNSIGNED NOT NULL COMMENT '移交Id',
  `folder_id` bigint UNSIGNED NOT NULL COMMENT '案卷id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_transfer_id`(`transfer_id`) USING BTREE,
  INDEX `idx_folder_id`(`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '移交案卷表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_transfer_remove
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_remove`;
CREATE TABLE `bs_transfer_remove`  (
  `remove_id` bigint UNSIGNED NOT NULL COMMENT '转递id',
  `remove_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '转递编号',
  `remove_inst` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '转递机构',
  `remove_time` datetime NULL DEFAULT NULL COMMENT '转递日期',
  `remove_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '转递联系人',
  `remove_opearetor` bigint NULL DEFAULT NULL COMMENT '转递操作人',
  `arc_num` int NULL DEFAULT NULL COMMENT '档案件数',
  `arc_page` int NULL DEFAULT NULL COMMENT '档案页数',
  `storage_day` int NULL DEFAULT NULL COMMENT '电子保管天数',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `transfer_org` bigint NULL DEFAULT NULL COMMENT '被转递组织',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`remove_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '转递记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_transfer_remove_arc
-- ----------------------------
DROP TABLE IF EXISTS `bs_transfer_remove_arc`;
CREATE TABLE `bs_transfer_remove_arc`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `remove_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '主键',
  `arc_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '档案id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_arc_id`(`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '转递档案' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bs_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `bs_warehouse`;
CREATE TABLE `bs_warehouse`  (
  `warehouse_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '库房id',
  `inst_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '机构id',
  `warehouse_code` int NOT NULL DEFAULT 0 COMMENT '库房编号',
  `warehouse_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '库房位置编码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '库房名称',
  `type` int NOT NULL DEFAULT 0 COMMENT '库房类型(自有:0,外包:1)',
  `area` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '库房面积',
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '库房位置',
  `amount` int NULL DEFAULT 0 COMMENT '库柜数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '备注',
  `system_code` int NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`warehouse_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '库房表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_arc
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_arc`;
CREATE TABLE `ent_bs_arc`  (
  `arc_id` bigint NOT NULL COMMENT '档案id',
  `arc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案号',
  `reference_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文号/项目编号',
  `categories_id` bigint NOT NULL COMMENT '档案门类id',
  `generals_id` bigint NULL DEFAULT NULL COMMENT '全宗id',
  `scope_id` bigint NULL DEFAULT NULL COMMENT '归档范围',
  `node_id` bigint NULL DEFAULT NULL COMMENT '归档节点id',
  `tidy_type` int NULL DEFAULT NULL COMMENT '整理方式0按件1按卷（字典表）',
  `arc_type_id` bigint NOT NULL COMMENT '档案分类id',
  `project_id` bigint NULL DEFAULT NULL COMMENT '关联的项目id',
  `person_id` bigint NULL DEFAULT NULL COMMENT '关联的人员id',
  `arc_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案题名',
  `secret_level` int NULL DEFAULT NULL COMMENT '密级',
  `arc_shape` int NULL DEFAULT NULL COMMENT '档案形态:0纸质及电子 1 纸质档案 2 电子档案 3实物件 ',
  `arc_lock` int NULL DEFAULT NULL COMMENT '档案标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定',
  `status` int NULL DEFAULT NULL COMMENT '档案状态:0收集中 1待归档 2已归档 3已销毁 4已遗失 5部门移交 6已删除',
  `keep_year` int NULL DEFAULT NULL COMMENT '保管年限999永久 10保存10年 30保存30年保管期限',
  `check_date` datetime NULL DEFAULT NULL COMMENT '归档日期',
  `acc_period` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计期间',
  `page_no` int NULL DEFAULT NULL COMMENT '页数',
  `create_inst` bigint NULL DEFAULT NULL COMMENT '建档机构',
  `create_dept` bigint NULL DEFAULT NULL COMMENT '建档部门',
  `create_user` bigint NULL DEFAULT NULL COMMENT '建档人',
  `create_date` datetime NULL DEFAULT NULL COMMENT '建档日期',
  `arc_year` int NULL DEFAULT NULL COMMENT '年度',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `store_user` bigint NULL DEFAULT NULL COMMENT '保管人',
  `store_date` datetime NULL DEFAULT NULL COMMENT '保管终止日期',
  `manage_inst` bigint NULL DEFAULT NULL COMMENT '所属机构',
  `manage_dept` bigint NULL DEFAULT NULL COMMENT '所属部门',
  `manage_user` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '责任人',
  `archives_date` datetime NULL DEFAULT NULL COMMENT '成文日期',
  `check_user` bigint NULL DEFAULT NULL COMMENT '归档检查人',
  `third_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方参数（影像参数）',
  `archives_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案来源 ：AM档案系统 TM,OA',
  `external_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '外部接入的档案唯一性记录',
  `folder_num` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '件号 组卷',
  `arc_precis` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '摘要',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `arc_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '实物档案首页图片url',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`arc_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业档案档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_arc_folder_flow
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_arc_folder_flow`;
CREATE TABLE `ent_bs_arc_folder_flow`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `flow_relation_id` bigint UNSIGNED NOT NULL COMMENT '流转关联id，对应档案id或则案卷id',
  `flow_type` int NULL DEFAULT NULL COMMENT '流转类型：0:档案，1:案卷',
  `flow_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转名称',
  `handle_status` int NULL DEFAULT NULL COMMENT '处理状态 退回：0，通过：1',
  `flow_user` bigint NULL DEFAULT NULL COMMENT '操作人',
  `flow_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '流转备注',
  `flow_time` datetime NOT NULL COMMENT '流转时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业档案案卷流转记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_arc_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_arc_tem_content`;
CREATE TABLE `ent_bs_arc_tem_content`  (
  `id` bigint NOT NULL COMMENT 'id',
  `arc_id` bigint NOT NULL COMMENT '档案id',
  `tem_id` bigint NOT NULL COMMENT '扩展字段id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展字段内容',
  `name_ch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '字段中文名',
  `name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '字段英文名',
  `select_nodes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下拉框选项如:(A,B,C)用,隔开',
  `is_show` int NULL DEFAULT NULL COMMENT '是否展示搜索条件0：不是 1：是',
  `type` int NULL DEFAULT NULL COMMENT '扩展字段控件类型0：输入框，1：日期框，2：下拉框',
  `is_primary` int NULL DEFAULT NULL COMMENT '是否必须输0：否 1：是',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案关联扩展字段内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_box
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_box`;
CREATE TABLE `ent_bs_box`  (
  `box_id` bigint NOT NULL COMMENT '档案盒主键',
  `box_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案盒号',
  `box_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案盒题名',
  `box_type` bigint NOT NULL COMMENT '盒类型',
  `generals_id` bigint NULL DEFAULT NULL COMMENT '全宗id',
  `category_id` bigint NULL DEFAULT NULL COMMENT '门类id',
  `box_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '创建人',
  `box_dept` bigint NULL DEFAULT NULL COMMENT '所属部门ID',
  `year` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '年份',
  `case_num` int NULL DEFAULT NULL COMMENT '档案件数',
  `update_user` bigint NULL DEFAULT NULL COMMENT '最近修改人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '更新时间',
  PRIMARY KEY (`box_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案盒表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_box_arc_folder
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_box_arc_folder`;
CREATE TABLE `ent_bs_box_arc_folder`  (
  `id` bigint NOT NULL COMMENT 'id;主键',
  `box_id` bigint NOT NULL COMMENT '盒id;盒id',
  `relation_id` bigint NOT NULL COMMENT '关联id;关联id',
  `relation_type` int NOT NULL COMMENT '关联类型;关联类型：0:档案，1:案卷',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业盒关联档案和案卷关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_folder
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_folder`;
CREATE TABLE `ent_bs_folder`  (
  `folder_id` bigint NOT NULL COMMENT '案卷id,主键',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '案上级卷ID',
  `folder_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷号',
  `folder_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷标题',
  `folder_type_id` bigint NOT NULL COMMENT '案卷类型ID',
  `categories_id` bigint NOT NULL COMMENT '案卷门类ID',
  `scope_id` bigint NULL DEFAULT NULL COMMENT '归档范围',
  `node_id` bigint NULL DEFAULT NULL COMMENT '归档节点',
  `generals_id` bigint NOT NULL COMMENT '全宗id',
  `project_id` bigint NULL DEFAULT NULL COMMENT '项目id',
  `person_id` bigint NULL DEFAULT NULL COMMENT '关联的人员id',
  `secret_level` int NULL DEFAULT NULL COMMENT '密级',
  `folder_shape` int NULL DEFAULT NULL COMMENT '会计-案卷形态0纸质及电子 1 纸质档案 2 电子档案',
  `folder_inst` bigint NULL DEFAULT NULL COMMENT '所属机构id',
  `folder_dept` bigint NULL DEFAULT NULL COMMENT '所属部门id',
  `folder_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '立卷人',
  `year` int NULL DEFAULT NULL COMMENT '年度',
  `case_num` int NULL DEFAULT NULL COMMENT '件数',
  `page_num` int NULL DEFAULT NULL COMMENT '页数',
  `update_user` bigint NULL DEFAULT NULL COMMENT '最近修改人',
  `acc_period` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '会计期间',
  `status` int NULL DEFAULT NULL COMMENT '状态：0收集中 1待归档 2已归档',
  `folder_lock` int NOT NULL COMMENT '案卷标志：会计系统-案卷表-案卷标志:0正常 1借阅锁定 2移交锁定 3移交退回  4遗失锁定 5销毁锁定 6转递锁定 7鉴定锁定 8已销毁锁定 业务系统:0',
  `store_date` datetime NULL DEFAULT NULL COMMENT '保管终止日期(yyyy)',
  `keep_year` int NULL DEFAULT NULL COMMENT '保管年限 默认(999永久 10保存10年 30保存30年)',
  `store_inst` bigint NULL DEFAULT NULL COMMENT '保管机构',
  `store_dept` bigint NULL DEFAULT NULL COMMENT '保管部门',
  `warehousing_time` datetime NULL DEFAULT NULL COMMENT '入库时间',
  `folder_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷备注',
  `folder_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '实物案卷首页图片url',
  `check_user` bigint NULL DEFAULT NULL COMMENT '归档检查人',
  `check_date` datetime NULL DEFAULT NULL COMMENT '归档日期',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`folder_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for ent_bs_folder_arc
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_folder_arc`;
CREATE TABLE `ent_bs_folder_arc`  (
  `id` bigint NOT NULL COMMENT 'id;主键',
  `folder_id` bigint NOT NULL COMMENT '案卷id;案卷id',
  `arc_id` bigint NOT NULL COMMENT '档案id;档案id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业案卷关联档案关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_folder_tem_content
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_folder_tem_content`;
CREATE TABLE `ent_bs_folder_tem_content`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `folder_id` bigint NOT NULL COMMENT '关联id',
  `tem_id` bigint NOT NULL COMMENT '扩展字段ID',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展字段内容',
  `name_ch` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `name_en` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下来框选项如:(A,B,C)用,隔开',
  `is_show` int NOT NULL DEFAULT 0 COMMENT '是否展示称搜索条件 0：是 1：不是',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框',
  `is_primary` int NOT NULL DEFAULT 0 COMMENT '是否必输 0：否 1:是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷关联扩展字段内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_person
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_person`;
CREATE TABLE `ent_bs_person`  (
  `person_id` bigint NOT NULL COMMENT '人员id',
  `category_id` bigint NOT NULL COMMENT '档案门类id',
  `arc_type_id` bigint NOT NULL COMMENT '档案分类id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '员工姓名',
  `Induction_time` datetime NOT NULL COMMENT '入职时间',
  `person_position` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '职位',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '联系方式',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '联系邮箱',
  `position_status` int NOT NULL COMMENT '在职状态(0离职，1在职)',
  `manage_dept` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '所属部门',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除(0否 ，1是)',
  PRIMARY KEY (`person_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '人员信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_perwaitfor
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_perwaitfor`;
CREATE TABLE `ent_bs_perwaitfor`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `init_id` bigint NULL DEFAULT NULL COMMENT '申请Id',
  `flow_per` bigint NULL DEFAULT NULL COMMENT '流程当前处理人id',
  `wait_type` int NULL DEFAULT NULL COMMENT '代办事项类型（字典表）',
  `init_user` bigint NULL DEFAULT NULL COMMENT '发起人',
  `init_dpt` bigint NULL DEFAULT NULL COMMENT '发起部门',
  `init_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
  `top_status` int NULL DEFAULT 0 COMMENT '置顶状态  （0：不置顶  1：置顶）',
  `extend_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '扩展内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业待办事项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_progress
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_progress`;
CREATE TABLE `ent_bs_progress`  (
  `progress_id` bigint NOT NULL COMMENT 'ID',
  `arc_type_id` bigint NULL DEFAULT NULL COMMENT '档案类型id',
  `tem_content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '额外信息（高级搜索）',
  `base_content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '基础字段',
  `arc_commit_num` bigint NULL DEFAULT NULL COMMENT '提交的档案总数',
  `arc_year` int NULL DEFAULT NULL COMMENT '年度',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除:0否-1是',
  PRIMARY KEY (`progress_id`) USING BTREE,
  INDEX `arc_type_id`(`arc_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '进度条查询表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_bs_project
-- ----------------------------
DROP TABLE IF EXISTS `ent_bs_project`;
CREATE TABLE `ent_bs_project`  (
  `project_id` bigint NOT NULL COMMENT '主键id',
  `categories_id` bigint NULL DEFAULT NULL COMMENT '档案门类id',
  `arc_type_id` bigint NULL DEFAULT NULL COMMENT '档案分类id',
  `project_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目名称',
  `project_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '项目编号',
  `status` int NULL DEFAULT NULL COMMENT '项目状态 0：未完结1:已完结',
  `tidy_type` int NULL DEFAULT NULL COMMENT '整理方式：0按件整理，1:按卷整理',
  `project_date_start` datetime NULL DEFAULT NULL COMMENT '项目日期始',
  `project_date_end` datetime NULL DEFAULT NULL COMMENT '项目日期止',
  `manage_dept` bigint NULL DEFAULT NULL COMMENT '所属部门',
  `manage_inst` bigint NULL DEFAULT NULL COMMENT '所属机构',
  `responsible_person` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '责任人',
  `year` int NULL DEFAULT NULL COMMENT '年度',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`project_id`) USING BTREE,
  UNIQUE INDEX `idx_project_id`(`project_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '项目信息表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for ent_sys_announ
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_announ`;
CREATE TABLE `ent_sys_announ`  (
  `ananoun_id` bigint NOT NULL COMMENT '主键',
  `ananoun_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标题',
  `ananoun_describe` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述',
  `ananoun_content` varchar(10000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '内容',
  `status` int NULL DEFAULT NULL COMMENT '状态，0:未公开，1:已公开',
  `release_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`ananoun_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业档案公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_announ_dept_inst
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_announ_dept_inst`;
CREATE TABLE `ent_sys_announ_dept_inst`  (
  `id` bigint NOT NULL COMMENT '主键',
  `ananoun_id` bigint NOT NULL COMMENT '公告id',
  `rel_id` bigint NOT NULL COMMENT '部门id或者机构的id',
  `type` int NOT NULL COMMENT '类型，0:机构，1:部门',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '企业档案公告部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_announ_read
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_announ_read`;
CREATE TABLE `ent_sys_announ_read`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `ananoun_id` bigint NOT NULL COMMENT '公告id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除(0否 ，1是)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '公告已读表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_archives_category
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_archives_category`;
CREATE TABLE `ent_sys_archives_category`  (
  `category_id` bigint NOT NULL COMMENT '档案门类id',
  `category_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '门类号',
  `category_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '门类名称',
  `category_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '门类描述',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '顺序号',
  `manage_type` int NOT NULL COMMENT '管理方式0文书1项目2人事3会计4实物',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级',
  `category_level` int NOT NULL COMMENT '门类级别',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案门类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_archives_filed_node
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_archives_filed_node`;
CREATE TABLE `ent_sys_archives_filed_node`  (
  `node_id` bigint NOT NULL COMMENT '档案归类节点id',
  `node_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '归档节点名称',
  `node_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '归档节点描述',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '顺序号',
  `arc_type_id` bigint NOT NULL COMMENT '档案分类id',
  `create_time` date NOT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`node_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案归档节点表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_archives_scope
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_archives_scope`;
CREATE TABLE `ent_sys_archives_scope`  (
  `scope_id` bigint NOT NULL COMMENT 'id;id',
  `scope_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '范围名称;范围名称',
  `keep_year` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '保管期限;保管期限',
  `secrets` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '密级;密级',
  `shapes` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案形态;档案形态',
  `scope_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '范围描述;范围描述',
  `archives_type_id` bigint NOT NULL COMMENT '档案类型id;档案类型id',
  `scope_index` int NULL DEFAULT NULL COMMENT '顺序号;顺序号',
  `create_time` datetime NOT NULL COMMENT '创建时间;创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间;更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）;是否删除（0否 1是）',
  PRIMARY KEY (`scope_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '归档范围配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_archives_tem
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_archives_tem`;
CREATE TABLE `ent_sys_archives_tem`  (
  `tem_id` bigint NOT NULL COMMENT '扩展字段id',
  `arc_type_id` bigint NOT NULL COMMENT '档案分类ID',
  `name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `name_ch` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `index_no` int NOT NULL COMMENT '顺序号',
  `is_primary` int NOT NULL DEFAULT 1 COMMENT '是否必输 0：否 1:是',
  `is_show` int NOT NULL DEFAULT 1 COMMENT '是否展示称搜索条件 0：不展示 1：展示',
  `is_sign` int NOT NULL DEFAULT 0 COMMENT '是否是电子档元数据 0：不显示，1：显示',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框 ',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下拉框选项如:(A,B,C)用,隔开',
  `is_list` int NOT NULL DEFAULT 0 COMMENT '是否显示在列表  0:不显示，1：显示',
  `is_basis` int NOT NULL DEFAULT 0 COMMENT '是否是基础字段（0否1是）',
  `create_time` date NOT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案扩展字段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_archives_type
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_archives_type`;
CREATE TABLE `ent_sys_archives_type`  (
  `arc_type_id` bigint NOT NULL COMMENT '档案分类id',
  `arc_type_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案分类号',
  `arc_type_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案分类名称',
  `arc_type_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案分类描述',
  `index_no` int NOT NULL DEFAULT 0 COMMENT '顺序号',
  `keep_year` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '保管年限 10：保管10年、30：保管30年、999永久保管',
  `secrets` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '密级',
  `shapes` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '档案形态0纸质及电子、1纸质、2电子、3实物件',
  `tidy_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '整理方式0按件1按卷',
  `is_box` int NOT NULL DEFAULT 0 COMMENT '是否装盒0无需装盒 1需要装盒',
  `sift_show` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '筛选展示0年度1保管期限2密级',
  `show_item` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '展示项 年度|保管期限|密级  2022,2023|0,1|0,1 ',
  `arc_no_rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '档案号编号规则',
  `folder_no_rules` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '案卷号编号规则',
  `parent_id` bigint NOT NULL COMMENT '父级',
  `arc_type_level` int NOT NULL COMMENT '分类级别',
  `categories_id` bigint NOT NULL COMMENT '门类id',
  `create_time` date NOT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`arc_type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_folder_tem
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_folder_tem`;
CREATE TABLE `ent_sys_folder_tem`  (
  `tem_id` bigint NOT NULL COMMENT '扩展字段id',
  `arc_type_id` bigint NOT NULL COMMENT '档案分类ID',
  `name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段英文名',
  `name_ch` varchar(2255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字段中文名',
  `index_no` int NOT NULL COMMENT '顺序号',
  `is_primary` int NOT NULL DEFAULT 1 COMMENT '是否必输 0：否 1:是',
  `is_show` int NOT NULL DEFAULT 1 COMMENT '是否展示称搜索条件 0：不展示 1：展示',
  `is_sign` int NOT NULL DEFAULT 0 COMMENT '是否是电子档元数据0：不显示，1：显示',
  `type` int NOT NULL DEFAULT 0 COMMENT '扩展字段控件类型0：输入框 1：日期框 2：下拉框 ',
  `select_nodes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '下拉框选项如:(A,B,C)用,隔开',
  `is_basis` int NOT NULL DEFAULT 0 COMMENT '是否上基础字段（0否1是）',
  `is_list` int NOT NULL DEFAULT 0 COMMENT '是否显示在列表  0:不显示，1：显示',
  `create_time` date NOT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`tem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '案卷扩展字段表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_generals
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_generals`;
CREATE TABLE `ent_sys_generals`  (
  `generals_id` bigint NOT NULL COMMENT 'id;',
  `generals_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '全宗号;',
  `generals_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '全宗名称;',
  `generals_describe` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '描述说明;',
  `generals_index` int NOT NULL COMMENT '顺序号;顺序号',
  `relation_status` int NOT NULL DEFAULT 0 COMMENT '是否关联档案类型（0否 1是）;',
  `create_time` datetime NOT NULL COMMENT '创建时间;',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间;',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）;',
  PRIMARY KEY (`generals_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '全宗管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_generals_archives_type
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_generals_archives_type`;
CREATE TABLE `ent_sys_generals_archives_type`  (
  `id` bigint NOT NULL COMMENT 'id',
  `generals_id` bigint NOT NULL COMMENT '全宗id',
  `archives_type_id` bigint NOT NULL COMMENT '档案类型id',
  `category_id` bigint NOT NULL COMMENT '门类id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '全宗-档案类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_generals_category
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_generals_category`;
CREATE TABLE `ent_sys_generals_category`  (
  `id` bigint NOT NULL COMMENT 'id',
  `generals_id` bigint NOT NULL COMMENT '全宗id',
  `category_id` bigint NOT NULL COMMENT '门类id',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '全宗-门类类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_generals_org
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_generals_org`;
CREATE TABLE `ent_sys_generals_org`  (
  `id` bigint NOT NULL COMMENT 'id;id',
  `generals_id` bigint NOT NULL COMMENT '全宗id;全宗id',
  `org_id` bigint NOT NULL COMMENT '机构id;机构id',
  `create_time` datetime NOT NULL COMMENT '创建时间;创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间;更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）;是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '全宗-机构表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_menu_user
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_menu_user`;
CREATE TABLE `ent_sys_menu_user`  (
  `id` bigint NOT NULL COMMENT 'id;主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `menu_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '菜单id（逗号分隔）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户快捷菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ent_sys_no_rule
-- ----------------------------
DROP TABLE IF EXISTS `ent_sys_no_rule`;
CREATE TABLE `ent_sys_no_rule`  (
  `rule_id` bigint NOT NULL COMMENT '生成规则id',
  `rule_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '生成规则名称',
  `rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '生成规则',
  `no_type` int NOT NULL COMMENT '号类型0档案，1案卷',
  `arc_type_id` bigint NOT NULL COMMENT '档案分类id',
  `create_time` date NOT NULL COMMENT '创建时间',
  `update_time` date NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '档案/案卷生成规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for st_sys_file
-- ----------------------------
DROP TABLE IF EXISTS `st_sys_file`;
CREATE TABLE `st_sys_file`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件原始名称',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名',
  `ext` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件扩展名',
  `size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `file_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件来源',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件访问地址(local)',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件相对路径',
  `file_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '存储平台',
  `base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '基础存储路径（文件所在位置）',
  `th_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '缩略图路径',
  `th_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '缩略图名称',
  `th_size` double NULL DEFAULT NULL COMMENT '缩略图大小',
  `create_user` bigint NULL DEFAULT NULL COMMENT '上传人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_api
-- ----------------------------
DROP TABLE IF EXISTS `sys_api`;
CREATE TABLE `sys_api`  (
  `id` bigint UNSIGNED NOT NULL,
  `app_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `public_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `system_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `system_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `system_type` int NULL DEFAULT NULL COMMENT '0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_app_id`(`app_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '对外api接口表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_api_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_api_log`;
CREATE TABLE `sys_api_log`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `user_id` bigint UNSIGNED NULL DEFAULT 0 COMMENT '用户id',
  `request_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-ip',
  `request_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-接口url',
  `request_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-接口-中文功能描述',
  `request_params` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-入参',
  `response_code` int NOT NULL DEFAULT 0 COMMENT '日志状态(成功:0,失败:1,异常:2)',
  `exception_msg` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '第三方操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_approval_act
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_act`;
CREATE TABLE `sys_approval_act`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `inst_id` bigint UNSIGNED NULL DEFAULT 0 COMMENT '机构id',
  `approval_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '审批类型',
  `act_key` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '流程定义中的key',
  `parameters` varchar(9999) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '定义的参数,用以判断是否要更新-流程定义模板',
  `arc_level` int NOT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `approval_required` int NOT NULL DEFAULT 0 COMMENT '是否需审批(默认0)  0：是 1：否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '关联表(审批类型-工作流定义名)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `dept_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门id',
  `dept_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '部门号',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门id-父级',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门名称',
  `name_level` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '部门名称-递归显示',
  `newlevel` int NULL DEFAULT 0 COMMENT '层级',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_ldap_id`(`ldap_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dept_middle
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept_middle`;
CREATE TABLE `sys_dept_middle`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT 'id',
  `no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '部门编号',
  `parent_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '上级部门编号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '部门名称',
  `inst_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '机构编号',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '处理状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dept_third
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept_third`;
CREATE TABLE `sys_dept_third`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT 'id',
  `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '参数类型',
  `type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '参数类型',
  `dept_id` bigint UNSIGNED NOT NULL COMMENT '部门id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '部门表第三方参数' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `sys_dictionary`;
CREATE TABLE `sys_dictionary`  (
  `id` bigint NOT NULL COMMENT '主键',
  `dic_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '字典表名',
  `dic_val` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '字典表值',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注描述',
  `dic_sequen` int NULL DEFAULT NULL COMMENT '顺序,同一层级排序',
  `dic_level` int NOT NULL DEFAULT 0 COMMENT '层级',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父ID',
  `system_code` int NULL DEFAULT NULL COMMENT '系统分类:0办公，1财务，2业务，3人事',
  `dic_extra` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '额外信息扩展字段',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_group`;
CREATE TABLE `sys_group`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户组编号',
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户组名',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `status` int NULL DEFAULT NULL COMMENT '状态，0:未启用，1:启用',
  `system_type` int NULL DEFAULT NULL COMMENT '档案系统分类:0办公，1财务，2业务，3人事',
  `permiss_level` int NULL DEFAULT NULL COMMENT '组织权限类型 0:指定部门可看，1：档案所属部门可看，2：全部部门可看',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '组织表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_inst
-- ----------------------------
DROP TABLE IF EXISTS `sys_inst`;
CREATE TABLE `sys_inst`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `inst_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '机构id',
  `inst_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '机构号',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '机构id-父级',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '机构名称',
  `name_level` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '机构名称-递归显示',
  `newlevel` int NULL DEFAULT 0 COMMENT '层级',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '全总号',
  `identifier_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '全宗名称',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `ldap_query_dn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'ldap查询dn',
  `ldap_query_user_txt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'ldap查询用户的条件',
  `ldap_query_dpt_txt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'ldap查询部门的条件',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '机构表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '主键',
  `user_id` bigint UNSIGNED NULL DEFAULT 0 COMMENT '用户id',
  `request_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-ip',
  `request_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求-接口url',
  `request_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-接口-中文功能描述',
  `request_params` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '请求-入参',
  `response_code` int NOT NULL DEFAULT 0 COMMENT '日志状态(成功:0,失败:1,异常:2)',
  `exception_msg` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '菜单id',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父菜单id',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '菜单名称',
  `perms` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '权限标识',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '接口地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '前端组件路径',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '菜单类型(菜单:M,目录:D,按钮:B)',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链(否:0,是:1)',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存(不缓存:0,缓存:1)',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '#' COMMENT '菜单图标',
  `status` int NULL DEFAULT 0 COMMENT '启用状态(显示:0,停用:1)',
  `visible` int NULL DEFAULT 0 COMMENT '隐藏状态(显示:0,隐藏:1)',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '备注',
  `permissions_type` int NOT NULL DEFAULT 0 COMMENT '默认0 超级管理员9',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`menu_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_no_rule
-- ----------------------------
DROP TABLE IF EXISTS `sys_no_rule`;
CREATE TABLE `sys_no_rule`  (
  `type_id` bigint UNSIGNED NOT NULL COMMENT 'id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '号名称',
  `rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '号规则',
  `no_type` int NULL DEFAULT NULL COMMENT '号类型 1：档案号 2：盒号 3：案卷号',
  `system_code` int NULL DEFAULT NULL COMMENT '系统分类:0办公，1财务，2业务，3人事',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创键时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  `arc_type_id` bigint NULL DEFAULT NULL COMMENT '分类id',
  `title_rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名生成规则',
  `title_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '题名名称',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统标识号生成规则表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_param
-- ----------------------------
DROP TABLE IF EXISTS `sys_param`;
CREATE TABLE `sys_param`  (
  `id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '参数key',
  `value` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '参数value',
  `type` int NOT NULL DEFAULT 0 COMMENT '0:固定参数;1:json对象,2:json数组',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注描述',
  `system_code` int NULL DEFAULT NULL COMMENT '系统分类:0办公，1财务，2业务，3人事',
  `status` int NULL DEFAULT NULL COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '系统参数表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permiss
-- ----------------------------
DROP TABLE IF EXISTS `sys_permiss`;
CREATE TABLE `sys_permiss`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `confid_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '保密级别名称',
  `confid_code` int NULL DEFAULT NULL COMMENT '保密code',
  `newlevel` int NULL DEFAULT 0 COMMENT '分类级别',
  `system_type` int NULL DEFAULT NULL COMMENT '系统类别',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permiss_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_permiss_group`;
CREATE TABLE `sys_permiss_group`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `group_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '用户组id',
  `permiss` int NULL DEFAULT NULL COMMENT '权限code',
  `dept_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '部门id',
  `type_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '类型id',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  `permiss_level` int NULL DEFAULT NULL COMMENT '组织权限类型 0:指定部门可看，1：档案所属部门可看，2：全部部门可看',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id`(`group_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_type_id`(`type_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '组织权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint UNSIGNED NOT NULL DEFAULT 1 COMMENT '角色id',
  `inst_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '机构id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '角色名称',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '描述',
  `status` int NOT NULL DEFAULT 1 COMMENT '启用状态(启用:0,停用:1)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`role_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `role_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '角色id',
  `menu_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '菜单id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '关联表(角色-菜单)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_user`;
CREATE TABLE `sys_role_user`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键id',
  `role_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '角色id',
  `user_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '关联表(角色用户)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户id',
  `inst_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '机构id',
  `dept_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门id',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '登录名',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '用户工号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '姓名',
  `pwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `sex` int NULL DEFAULT 0 COMMENT '性别(女:0,男:1)',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '联系方式',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '电子邮箱',
  `state` int NOT NULL DEFAULT 0 COMMENT '账号状态(未启用:0,启用:1,注销:2,锁定:3)',
  `type` int NOT NULL DEFAULT 0 COMMENT '账号类型(普通:0,经销商:1)',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `is_scan` int NULL DEFAULT 1 COMMENT '是否有扫描仪权限 0是 1否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_admin`;
CREATE TABLE `sys_user_admin`  (
  `user_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户id',
  `inst_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '机构id',
  `dept_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '部门id',
  `login_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '登录名',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '用户工号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '姓名',
  `pwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `sex` int NULL DEFAULT 0 COMMENT '性别(女:0,男:1)',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '联系方式',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '电子邮箱',
  `state` int NOT NULL DEFAULT 0 COMMENT '账号状态(未启用:0,启用:1,注销:2,锁定:3)',
  `type` int NOT NULL DEFAULT 0 COMMENT '账号类型(普通:0,系统管理员:1)',
  `ldap_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '第三方系统同步主键',
  `is_scan` int NULL DEFAULT 1 COMMENT '是否有扫描仪权限 0是 1否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_inst_id`(`inst_id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_group`;
CREATE TABLE `sys_user_group`  (
  `id` bigint UNSIGNED NOT NULL COMMENT '主键',
  `group_id` bigint UNSIGNED NOT NULL COMMENT '用户组id',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group_id`(`group_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户组织表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
