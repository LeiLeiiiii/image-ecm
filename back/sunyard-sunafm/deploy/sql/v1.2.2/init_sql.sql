/*
 Navicat Premium Data Transfer

 Source Server         : 172.1.3.165
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : 172.1.3.165:3306
 Source Schema         : dev_afm

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 27/12/2024 09:36:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for afm_api_data
-- ----------------------------
DROP TABLE IF EXISTS `afm_api_data`;
CREATE TABLE `afm_api_data` (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
 `status` int DEFAULT NULL COMMENT '队列处理状态，0未成功，1成功',
 `request_params` varchar(8000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '推送参数',
 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `is_deleted` int NOT NULL DEFAULT '0' COMMENT '删除状态(否:0,是:1)',
 `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '错误描述',
 `retry_num` int DEFAULT '0' COMMENT '重试次数',
 `type` int DEFAULT NULL COMMENT '查重请求类型  0：影像查重  1：文本查重',
 PRIMARY KEY (`id`) USING BTREE,
 KEY `status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1968970975942455297 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='对外接口请求数据';

-- ----------------------------
-- Table structure for afm_file_exif
-- ----------------------------
DROP TABLE IF EXISTS `afm_file_exif`;
CREATE TABLE `afm_file_exif` (
`exif_id` bigint NOT NULL COMMENT '主键',
`file_index` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件主索引',
`file_md5` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件md5',
`file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名称',
`file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件url',
`source_sys` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源系统',
`business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务类型',
`business_index` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务索引（主索引）',
`material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资料类型',
`upload_user_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传人登录名',
`upload_user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传人（姓名）',
`upload_org` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传机构',
`file_exif` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件元数据（json格式）',
`is_vector` int NOT NULL DEFAULT '0' COMMENT '是否已存入向量数据库',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
`update_time` datetime DEFAULT NULL COMMENT '修改时间',
`is_deleted` int NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
`year` int DEFAULT NULL COMMENT '年份',
`server_id` bigint DEFAULT NULL COMMENT '关联的服务器id',
`type` int DEFAULT NULL COMMENT '文件查重类型  0：影像查重  1：文本查重',
`file_text` longtext COLLATE utf8mb4_general_ci COMMENT '文本查重信息',
PRIMARY KEY (`exif_id`) USING BTREE,
UNIQUE KEY `file_index` (`file_index`,`type`),
KEY `file_md5_index` (`file_md5`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='文件表';

-- ----------------------------
-- Table structure for afm_image_dup_assoc
-- ----------------------------
DROP TABLE IF EXISTS `afm_image_dup_assoc`;
CREATE TABLE `afm_image_dup_assoc` (
`id` bigint NOT NULL COMMENT '主键',
`dup_note_id` bigint NOT NULL COMMENT '查重记录id',
`assoc_exif_id` bigint NOT NULL COMMENT '相似文件id',
`similarity` double NOT NULL DEFAULT '0' COMMENT '相似度',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`) USING BTREE,
KEY `dup_note_id` (`dup_note_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='图像查重关联表;';

-- ----------------------------
-- Table structure for afm_image_dup_note
-- ----------------------------
DROP TABLE IF EXISTS `afm_image_dup_note`;
CREATE TABLE `afm_image_dup_note` (
`id` bigint NOT NULL COMMENT '主键',
`source_sys` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '来源系统',
`business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务类型',
`business_index` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务索引（主索引）',
`material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资料类型',
`exif_id_or_md5` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件id或md5',
`file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件名',
`upload_user_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '上传人登录名',
`upload_user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '上传人（姓名）',
`upload_org` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传机构',
`upload_org_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传机构中文名称',
`img_dup_time` datetime NOT NULL COMMENT '图像查重时间',
`img_dup_result` double NOT NULL DEFAULT '0' COMMENT '图像查重结果',
`similarity` double NOT NULL DEFAULT '0.85' COMMENT '这条查重的相似度',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime DEFAULT NULL COMMENT '修改时间',
`is_deleted` int NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
PRIMARY KEY (`id`) USING BTREE,
KEY `afm_image_dup_note_exif_id_or_md5_index` (`exif_id_or_md5`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='图像查重记录表;';

-- ----------------------------
-- Table structure for afm_image_ps_note
-- ----------------------------
DROP TABLE IF EXISTS `afm_image_ps_note`;
CREATE TABLE `afm_image_ps_note` (
`id` bigint NOT NULL COMMENT '主键',
`source_sys` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源系统',
`business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务类型',
`business_index` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务索引（主索引）',
`material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资料类型',
`exif_id` bigint NOT NULL COMMENT '文件id',
`file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名称',
`upload_user_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传人登录名',
`upload_user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传人（姓名）',
`upload_org` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传机构',
`ps_det_time` datetime DEFAULT NULL COMMENT '篡改检测时间',
`ps_det_result` int DEFAULT NULL COMMENT '篡改检测结果（0无篡改 1有篡改）',
`ps_count` int DEFAULT NULL COMMENT '篡改处',
`ps_det_file_id` bigint DEFAULT NULL COMMENT '含篡改区域文件id，无篡改则为空',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
`update_time` datetime DEFAULT NULL COMMENT '修改时间',
`is_deleted` int NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='篡改检测记录表;';

-- ----------------------------
-- Table structure for afm_invoice_det_note
-- ----------------------------
DROP TABLE IF EXISTS `afm_invoice_det_note`;
CREATE TABLE `afm_invoice_det_note` (
`id` bigint NOT NULL COMMENT '主键',
`source_sys` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '来源系统',
`business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务类型',
`business_index` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务索引（主索引）',
`material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资料类型',
`exif_id` bigint DEFAULT NULL COMMENT '文件id',
`file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件名称',
`upload_user_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传人登录名',
`upload_user_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传人（姓名）',
`upload_org` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '上传机构',
`invoice_det_time` datetime NOT NULL COMMENT '发票检测时间',
`invoice_verify_result` int DEFAULT NULL COMMENT '验真是否通过（0否 1是）',
`invoice_dup_result` int DEFAULT NULL COMMENT '查重是否通过 （0否 1是）',
`invoice_link_result` int DEFAULT NULL COMMENT '连续是否通过（0否 1是）',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
`update_time` datetime DEFAULT NULL COMMENT '修改时间',
`is_deleted` int NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='发票检测记录表;';

-- ----------------------------
-- Table structure for afm_invoice_det_note_assoc
-- ----------------------------
DROP TABLE IF EXISTS `afm_invoice_det_note_assoc`;
CREATE TABLE `afm_invoice_det_note_assoc` (
`id` bigint NOT NULL COMMENT '主键',
`invoice_note_id` bigint DEFAULT NULL COMMENT '发票检测记录id',
`assoc_exif_id` bigint DEFAULT NULL COMMENT '关联文件id',
`assoc_type` int DEFAULT NULL COMMENT '关联文件类型（0重复 1连续）',
`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime DEFAULT NULL COMMENT '修改时间',
`is_deleted` int NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='发票检测记录关联表;';

-- ----------------------------
-- Table structure for afm_invoice_file_data
-- ----------------------------
DROP TABLE IF EXISTS `afm_invoice_file_data`;
CREATE TABLE `afm_invoice_file_data` (
`id` bigint NOT NULL COMMENT '主键',
`file_md5` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件md5',
`invoice_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票代码',
`invoice_num` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票号',
`invoice_check_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票校验码',
`invoice_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票日期',
`invoice_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票类型',
`invoice_total` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '发票金额',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
`is_deleted` int NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='发票文件详情信息;';

-- ----------------------------
-- Table structure for afm_server
-- ----------------------------
DROP TABLE IF EXISTS `afm_server`;
CREATE TABLE `afm_server` (
`id` bigint NOT NULL COMMENT '主键',
`name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '向量数据库服务名称',
`host` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '连接地址',
`port` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '端口',
`user` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '连接用户名',
`password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '连接密码',
`db_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '向量数据库的名称',
`type` int DEFAULT NULL COMMENT '状态 0:可读，1:可读写',
`doc_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资料类型',
`status` int DEFAULT NULL COMMENT '0:不可使用；1:可使用',
`year` int DEFAULT NULL COMMENT '年份，如果是多年则，存放第一年的日期',
`num` int DEFAULT NULL COMMENT '集合排列的数量',
`collection_total` bigint DEFAULT NULL COMMENT '集合内总条数',
`collection_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '服务器对应集合名称',
`server_type` int DEFAULT NULL COMMENT '数据库查重类型  0：图像查重服务器 1：文本查重服务器',
PRIMARY KEY (`id`) USING BTREE,
UNIQUE KEY `唯一索引` (`year`,`doc_code`,`status`,`num`,`type`,`server_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='服务器表';

SET FOREIGN_KEY_CHECKS = 1;
