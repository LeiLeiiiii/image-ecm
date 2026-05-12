/*
 Navicat Premium Data Transfer

 Source Server         : 172.1.3.165
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : 172.1.3.165:3306
 Source Schema         : sit_afm

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 28/05/2024 16:26:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for afm_api_data
-- ----------------------------
DROP TABLE IF EXISTS `afm_api_data`;
CREATE TABLE `afm_api_data`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `status` int(0) NULL DEFAULT NULL COMMENT '队列处理状态，0未成功，1成功',
  `request_params` varchar(8000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '推送参数',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  `error_msg` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '错误描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1777966235326094708 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '对外接口请求数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_file_exif
-- ----------------------------
DROP TABLE IF EXISTS `afm_file_exif`;
CREATE TABLE `afm_file_exif`  (
  `exif_id` bigint(0) NOT NULL COMMENT '主键',
  `file_index` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件主索引',
  `file_md5` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件md5',
  `file_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名称',
  `file_url` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件url',
  `source_sys` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '来源系统',
  `business_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_index` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务索引（主索引）',
  `material_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资料类型',
  `upload_user_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传人登录名',
  `upload_user_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传人（姓名）',
  `upload_org` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传机构',
  `file_exif` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件元数据（json格式）',
  `is_vector` int(0) NOT NULL DEFAULT 0 COMMENT '是否存为向量（0否 1是）',
  `collection_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '向量集合所在位置名称',
  `vector_partition` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '向量所在分区',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`exif_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_image_dup_assoc
-- ----------------------------
DROP TABLE IF EXISTS `afm_image_dup_assoc`;
CREATE TABLE `afm_image_dup_assoc`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `dup_note_id` bigint(0) NOT NULL COMMENT '查重记录id',
  `assoc_exif_id` bigint(0) NOT NULL COMMENT '相似文件id',
  `similarity` double NOT NULL DEFAULT 0 COMMENT '相似度',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图像查重关联表;' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_image_dup_note
-- ----------------------------
DROP TABLE IF EXISTS `afm_image_dup_note`;
CREATE TABLE `afm_image_dup_note`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `source_sys` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '来源系统',
  `business_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_index` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务索引（主索引）',
  `material_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资料类型',
  `exif_id_or_md5` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件id或md5',
  `file_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件名',
  `upload_user_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '上传人登录名',
  `upload_user_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '上传人（姓名）',
  `upload_org` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传机构',
  `upload_org_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传机构中文名称',
  `img_dup_time` datetime(0) NOT NULL COMMENT '图像查重时间',
  `img_dup_result` double NOT NULL DEFAULT 0 COMMENT '图像查重结果',
  `similarity` double NOT NULL DEFAULT 0.85 COMMENT '这条查重的相似度',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图像查重记录表;' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_image_ps_note
-- ----------------------------
DROP TABLE IF EXISTS `afm_image_ps_note`;
CREATE TABLE `afm_image_ps_note`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `source_sys` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '来源系统',
  `business_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_index` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务索引（主索引）',
  `material_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资料类型',
  `exif_id` bigint(0) NOT NULL COMMENT '文件id',
  `file_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件名称',
  `upload_user_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传人登录名',
  `upload_user_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传人（姓名）',
  `upload_org` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传机构',
  `ps_det_time` datetime(0) NULL DEFAULT NULL COMMENT '篡改检测时间',
  `ps_det_result` int(0) NULL DEFAULT NULL COMMENT '篡改检测结果（0无篡改 1有篡改）',
  `ps_count` int(0) NULL DEFAULT NULL COMMENT '篡改处',
  `ps_det_file_id` bigint(0) NULL DEFAULT NULL COMMENT '含篡改区域文件id，无篡改则为空',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '篡改检测记录表;' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_invoice_det_note
-- ----------------------------
DROP TABLE IF EXISTS `afm_invoice_det_note`;
CREATE TABLE `afm_invoice_det_note`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `source_sys` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '来源系统',
  `business_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `business_index` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '业务索引（主索引）',
  `material_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资料类型',
  `exif_id` bigint(0) NULL DEFAULT NULL COMMENT '文件id',
  `file_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件名称',
  `upload_user_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传人登录名',
  `upload_user_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传人（姓名）',
  `upload_org` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上传机构',
  `invoice_det_time` datetime(0) NOT NULL COMMENT '发票检测时间',
  `invoice_verify_result` int(0) NULL DEFAULT NULL COMMENT '验真是否通过（0否 1是）',
  `invoice_dup_result` int(0) NULL DEFAULT NULL COMMENT '查重是否通过 （0否 1是）',
  `invoice_link_result` int(0) NULL DEFAULT NULL COMMENT '连续是否通过（0否 1是）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '发票检测记录表;' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_invoice_det_note_assoc
-- ----------------------------
DROP TABLE IF EXISTS `afm_invoice_det_note_assoc`;
CREATE TABLE `afm_invoice_det_note_assoc`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `invoice_note_id` bigint(0) NULL DEFAULT NULL COMMENT '发票检测记录id',
  `assoc_exif_id` bigint(0) NULL DEFAULT NULL COMMENT '关联文件id',
  `assoc_type` int(0) NULL DEFAULT NULL COMMENT '关联文件类型（0重复 1连续）',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '发票检测记录关联表;' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for afm_invoice_file_data
-- ----------------------------
DROP TABLE IF EXISTS `afm_invoice_file_data`;
CREATE TABLE `afm_invoice_file_data`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `file_md5` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '文件md5',
  `invoice_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票代码',
  `invoice_num` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票号',
  `invoice_check_code` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票校验码',
  `invoice_date` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票日期',
  `invoice_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票类型',
  `invoice_total` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发票金额',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_deleted` int(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0否 1是）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '发票文件详情信息;' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
