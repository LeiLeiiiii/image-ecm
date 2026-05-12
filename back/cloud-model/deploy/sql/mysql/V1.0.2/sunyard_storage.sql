/*
 Navicat Premium Dump SQL

 Source Server         : ecm-server
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 172.1.3.165:3306
 Source Schema         : uat_sunyard_storage

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 04/11/2024 09:27:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for st_equipment
-- ----------------------------
DROP TABLE IF EXISTS `st_equipment`;
CREATE TABLE `st_equipment`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键id',
  `equipment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '设备名',
  `equipment_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备编码',
  `storage_type` int NOT NULL COMMENT '存储方式',
  `base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '基础路径',
  `domain_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '自定义域名',
  `storage_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '节点地址',
  `bucket` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '存储bucket',
  `access_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '存储连接key',
  `access_secret` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '存储连接密钥',
  `status` int NOT NULL DEFAULT 1 COMMENT '是否启用0未启用，1启用',
  `create_user` bigint NULL DEFAULT NULL COMMENT '上传人id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '最近修改人',
  `is_deleted` int NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '存储设备' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for st_file
-- ----------------------------
DROP TABLE IF EXISTS `st_file`;
CREATE TABLE `st_file`  (
  `id` bigint NOT NULL DEFAULT 0 COMMENT '主键id',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件原始名称',
  `filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名',
  `ext` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件扩展名',
  `size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `upload_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '上传Id（每个文件有唯一的一个上传id）',
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '所属桶名',
  `object_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件的key(桶下的文件路径)',
  `chunk_size` bigint NULL DEFAULT NULL COMMENT '每个分片大小（byte）',
  `chunk_num` int NULL DEFAULT NULL COMMENT '分片数量',
  `file_source` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件来源(使用spring:application:name)',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件访问地址(local)',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件相对路径',
  `equipment_id` bigint NULL DEFAULT NULL COMMENT '存储设备id',
  `source_file_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '源文件MD5',
  `file_md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '目标文件MD5',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '存储平台',
  `base_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '基础存储路径（文件所在位置）',
  `th_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '缩略图路径',
  `th_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '缩略图名称',
  `th_size` double NULL DEFAULT NULL COMMENT '缩略图大小',
  `busi_batch_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '业务批次号',
  `config_id` bigint NULL DEFAULT NULL COMMENT '配置编号（关联sys_file_config表的id）',
  `create_user` bigint NULL DEFAULT NULL COMMENT '上传人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '删除状态(否:0,是:1)',
  `is_upload_ok` int NULL DEFAULT 0 COMMENT '是否上传完成（0:未完成，1：完成）',
  `is_encrypt` int NULL DEFAULT 0 COMMENT '是否加密 （0否 1是）',
  `encrypt_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '加密密钥',
  `encrypt_index` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '加密标识符',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_source_file_md5`(`source_file_md5` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '文件表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
