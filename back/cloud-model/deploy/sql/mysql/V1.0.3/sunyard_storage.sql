/*
 Navicat Premium Dump SQL

 Source Server         : ecm-server
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 172.1.3.165:3306
 Source Schema         : dev_sunyard_storage

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 04/11/2024 09:31:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for st_split_upload
-- ----------------------------
DROP TABLE IF EXISTS `st_split_upload`;
CREATE TABLE `st_split_upload` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键 ID',
                                   `file_id` bigint DEFAULT NULL COMMENT '文件ID',
                                   `upload_id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '上传任务的唯一标识 (UUID)',
                                   `chunk_size` int DEFAULT NULL COMMENT '每个分片的大小（字节）',
                                   `chunk_num` int DEFAULT NULL COMMENT '分片的总数量',
                                   `is_upload_ok` int DEFAULT '0' COMMENT '是否已成功上传',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
                                   `is_deleted` int DEFAULT '0' NOT NULL  COMMENT '删除状态(否:0,是:1)',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `upload_id` (`upload_id`,`chunk_num`) COMMENT '唯一索引：由 upload_id 和 chunk_index 组合而成',
                                   KEY `idx_file_id` (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1854708979016405238 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分片上传临时表';

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `st_file`
DROP COLUMN `upload_id`,
DROP COLUMN `bucket_name`,
DROP COLUMN `chunk_size`,
DROP COLUMN `chunk_num`,
DROP COLUMN `url`,
DROP COLUMN `file_path`,
DROP COLUMN `platform`,
DROP COLUMN `base_path`,
DROP COLUMN `th_url`,
DROP COLUMN `th_filename`,
DROP COLUMN `th_size`,
DROP COLUMN `busi_batch_no`,
DROP COLUMN `config_id`,
DROP COLUMN `update_time`,
DROP COLUMN `is_upload_ok`,
DROP COLUMN `encrypt_index`;




ALTER TABLE `st_file`
    MODIFY COLUMN `filename` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件名',
    MODIFY COLUMN `ext` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件扩展名',
    MODIFY COLUMN `file_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '文件来源(使用spring:application:name)',
    MODIFY COLUMN `source_file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '源文件MD5',
    MODIFY COLUMN `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '目标文件MD5',
    MODIFY COLUMN `encrypt_key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '加密密钥',
    ADD COLUMN `encrypt_type` INT NULL DEFAULT NULL COMMENT '加密类型',
    ADD COLUMN `encrypt_len` BIGINT NULL DEFAULT NULL COMMENT '加密长度';

