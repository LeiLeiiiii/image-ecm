-- ----------------------------
-- Records of sys_api
-- ----------------------------
INSERT INTO sys_api VALUES (3130050638595073, 'cs01', 'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsRG+yYpRbmJtNseufCxwlC4zSPQbyOdOYspERKRnH6oHyKbQ8sx+Rv10cU6J9V+ylvoLcswNcgWJ7p7icNHU/i+309nX2d+0kPojJTgpkoSdMepQWyW51gBDS+kzvqBExfTzC+wNAXTTO7cWNk1a90uNPLR6xskkIPp+rE/B6Z7GndGp28v/+DovvRa3es+K1oAtQC94cFZS4ZLmMzeYCe0EAlr9v+AD9h4VsmNP2UL8eBactsyjzkkjWUvpi6M1VG9pt4f0VjxjAbD/OrTfDSmOMi5i5zmhkoUdrLiPGQ2AzjCQv6g5bdZa8Htj7TfydPyik31J2JTLyCdw4U3rzwIDAQAB', '测试会计系统推送', '1', '127.0.0.1', NULL, '2022-12-13 16:48:05', '2022-12-13 16:48:05', 0);

-- ----------------------------
-- Records of sys_dictionary 必要
-- ----------------------------

-- ----------------------------
-- Records of sys_inst 必要
-- ----------------------------

-- ----------------------------
-- Records of sys_menu 必要 改
-- ----------------------------
UPDATE sys_menu SET parent_id = 0, menu_name = '基础管理', perms = 'base', path = '/basics/**', component = NULL, menu_type = 'D', is_frame = 1, is_cache = 1, icon = 'system', status = 0, visible = 0, order_num = 9, remark = NULL, permissions_type = 9, create_time = '2020-10-22 14:27:04', update_time = '2022-12-15 10:56:33', is_deleted = 0 WHERE menu_id = 1120000;

UPDATE sys_menu SET parent_id = 3109440312681473, menu_name = '系统管理', perms = 'entSys', path = '/arcEnt/entSys/**', component = 'EmptyLayout', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'chart', status = 0, visible = 0, order_num = 9, remark = '0', permissions_type = 0, create_time = '2022-11-15 16:24:37', update_time = '2023-02-06 16:28:49', is_deleted = 0 WHERE menu_id = 3110221431849986;

UPDATE sys_menu SET parent_id = 3110221431849986, menu_name = '公告管理', perms = 'entAnnount', path = '/arcEnt/entSys/entAnnount/**', component = 'arcEnt/entSys/entAnnount', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'chart', status = 0, visible = 0, order_num = 1, remark = '0', permissions_type = 0, create_time = '2022-11-15 16:25:35', update_time = '2023-02-06 16:19:16', is_deleted = 0 WHERE menu_id = 3110221910672385;

UPDATE sys_menu SET parent_id = 3110221431849986, menu_name = '全宗管理', perms = 'entQzManage', path = '/arcEnt/entSys/entQzManage/**', component = 'arcEnt/entSys/entQzManage', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'dict', status = 0, visible = 0, order_num = 2, remark = '0', permissions_type = 0, create_time = '2022-11-15 17:18:44', update_time = '2023-02-06 16:19:26', is_deleted = 0 WHERE menu_id = 3110248034436097;

UPDATE sys_menu SET parent_id = 3109440312681473, menu_name = '归档管理', perms = 'entArchiveManage', path = '/arcEnt/entArchiveManage/**', component = 'EmptyLayout', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'documentation', status = 0, visible = 0, order_num = 2, remark = '0', permissions_type = 0, create_time = '2022-11-16 09:34:10', update_time = '2023-02-06 16:16:14', is_deleted = 0 WHERE menu_id = 3110727477724161;

UPDATE sys_menu SET parent_id = 3110727477724161, menu_name = '档案收集', perms = 'entArcCollection', path = '/arcEnt/entArchiveManage/entArcCollection/**', component = 'arcEnt/entArchiveManage/entArcCollection', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'dict', status = 0, visible = 0, order_num = 1, remark = '0', permissions_type = 0, create_time = '2022-11-16 09:34:35', update_time = '2023-02-06 16:17:45', is_deleted = 0 WHERE menu_id = 3110727685039105;

UPDATE sys_menu SET parent_id = 3110727477724161, menu_name = '归档审核', perms = 'entArcAudit', path = '/arcEnt/entArchiveManage/entArcAudit/**', component = 'arcEnt/entArchiveManage/entArcAudit', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'clipboard', status = 0, visible = 0, order_num = 2, remark = '0', permissions_type = 0, create_time = '2022-11-16 09:35:06', update_time = '2023-02-06 16:18:07', is_deleted = 0 WHERE menu_id = 3110727938868225;

UPDATE sys_menu SET parent_id = 3110221431849986, menu_name = '档案模型', perms = 'entArcModel', path = '/arcEnt/entSys/entArcModel/**', component = 'arcEnt/entSys/entArcModel', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'checkbox', status = 0, visible = 0, order_num = 3, remark = '0', permissions_type = 0, create_time = '2022-11-16 11:26:30', update_time = '2023-02-06 16:19:40', is_deleted = 0 WHERE menu_id = 3110782695597058;

UPDATE sys_menu SET parent_id = 3110221431849986, menu_name = '归档范围配置', perms = 'entArchiveScope', path = '/arcEnt/entSys/entArchiveScope/**', component = 'arcEnt/entSys/entArchiveScope', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'build', status = 0, visible = 0, order_num = 4, remark = '0', permissions_type = 0, create_time = '2022-11-16 14:34:05', update_time = '2023-02-06 16:19:48', is_deleted = 0 WHERE menu_id = 3110874891346945;

UPDATE sys_menu SET parent_id = 3109440312681473, menu_name = '档案管理', perms = 'entArcManage', path = '/arcEnt/entArcManage/**', component = 'EmptyLayout', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'row', status = 0, visible = 0, order_num = 5, remark = '0', permissions_type = 0, create_time = '2022-11-18 16:40:03', update_time = '2023-02-06 16:28:33', is_deleted = 0 WHERE menu_id = 3112352382043137;

UPDATE sys_menu SET parent_id = 3112352382043137, menu_name = '档案装盒', perms = 'entArcBox', path = '/arcEnt/entArcManage/entArcBox/**', component = 'arcEnt/entArcManage/entArcBox', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'folderOpen', status = 0, visible = 0, order_num = 4, remark = '0', permissions_type = 0, create_time = '2022-11-18 16:45:00', update_time = '2023-02-07 11:18:39', is_deleted = 0 WHERE menu_id = 3112354815026177;

UPDATE sys_menu SET parent_id = 3110221431849986, menu_name = '四性检测', perms = 'entFourSexDetection', path = '/arcEnt/entSys/entFourSexDetection/**', component = 'arcEnt/entSys/entFourSexDetection', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'monitor', status = 0, visible = 0, order_num = 5, remark = '0', permissions_type = 0, create_time = '2022-11-29 10:32:32', update_time = '2023-02-06 16:20:03', is_deleted = 0 WHERE menu_id = 3119957417985026;

UPDATE sys_menu SET parent_id = 3130727197606914, menu_name = '公告管理', perms = 'docAnnoun', path = '', component = 'docSys/docSysManage/docAnnoun', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'edit', status = 0, visible = 0, order_num = 5, remark = '0', permissions_type = 0, create_time = '2022-12-14 15:49:36', update_time = '2022-12-26 13:58:50', is_deleted = 0 WHERE menu_id = 3130730100941826;

UPDATE sys_menu SET parent_id = 3135527151895553, menu_name = '个人文档库', perms = 'personalDocLibrary', path = '', component = 'documentCenter/personalDocLibrary', menu_type = 'M', is_frame = 1, is_cache = 0, icon = 'dict', status = 0, visible = 0, order_num = 1, remark = '0', permissions_type = 0, create_time = '2022-12-21 10:41:00', update_time = '2023-01-17 16:50:00', is_deleted = 0 WHERE menu_id = 3135532932269058;

-- ----------------------------
-- Records of sys_menu 必要 增
-- ----------------------------
INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3139050485089281, 3130707796820994, '高级配置', 'advancedConfig', '', 'docSys/advancedConfig', 'M', 1, 0, 'table', 0, 0, 3, '0', 0, '2022-12-26 09:57:28', '2022-12-26 13:59:06', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3146172436562946, 3135527151895553, '未上架文档', 'noShelfDoc', '', 'documentCenter/noShelfDoc', 'M', 1, 0, 'again_2', 0, 0, 3, '0', 0, '2023-01-05 11:27:07', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168968228094978, 3109440312681473, '档案利用', 'entArcUtilization', '', 'EmptyLayout', 'M', 1, 0, 'job', 0, 0, 6, '0', 0, '2023-02-06 16:25:17', '2023-02-06 17:03:48', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168969053201410, 3109440312681473, '库房管理', 'entWarehouse', '', 'EmptyLayout', 'M', 1, 0, 'list', 0, 0, 7, '0', 0, '2023-02-06 16:26:57', '2023-02-07 12:23:04', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168969598919682, 3109440312681473, '报表统计', 'entReportStatistics', '', 'EmptyLayout', 'M', 1, 0, 'statistics_4', 0, 0, 8, '0', 0, '2023-02-06 16:28:04', '2023-02-06 17:03:55', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168988278563841, 3168968228094978, '档案查询', 'entArcQuery', '', 'arcEnt/entArcUtilization/entArcQuery', 'M', 1, 0, 'search', 0, 0, 1, '0', 0, '2023-02-06 17:06:04', '2023-02-07 12:19:43', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168988712780801, 3168968228094978, '借阅管理', 'entArcLoan', '', 'arcEnt/entArcUtilization/entArcLoan', 'M', 1, 0, 'clipboard', 0, 0, 2, '0', 0, '2023-02-06 17:06:57', '2023-02-07 12:19:52', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168989132211202, 3168969053201410, '出库入库', 'entInOut', '', 'arcEnt/entWarehouse/entInOut', 'M', 1, 0, 'guide', 0, 0, 1, '0', 0, '2023-02-06 17:07:48', '2023-02-07 12:32:45', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168989822690305, 3168969053201410, '库房管理', 'entHouse', '', 'arcEnt/entWarehouse/entHouse', 'M', 1, 0, 'dict', 0, 0, 2, '0', 0, '2023-02-06 17:09:13', '2023-02-07 12:32:56', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168990172087298, 3168969598919682, '归档统计', 'entArchival', '', 'arcEnt/entReportStatistics/entArchival', 'M', 1, 0, 'monitor', 0, 0, 1, '0', 0, '2023-02-06 17:09:55', '2023-02-07 12:21:51', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168990489003009, 3168969598919682, '销毁统计', 'entDestruction', '', 'arcEnt/entReportStatistics/entDestruction', 'M', 1, 0, 'server', 0, 0, 4, '0', 0, '2023-02-06 17:10:34', '2023-02-07 12:22:15', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168990675543041, 3168969598919682, '移转统计', 'entTransfer', '', 'arcEnt/entReportStatistics/entTransfer', 'M', 1, 0, 'build', 0, 0, 3, '0', 0, '2023-02-06 17:10:57', '2023-02-07 12:22:05', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3168990846280705, 3168969598919682, '利用统计', 'entUtilization', '', 'arcEnt/entReportStatistics/entUtilization', 'M', 1, 0, 'druid', 0, 0, 2, '0', 0, '2023-02-06 17:11:18', '2023-02-07 12:21:58', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169484751840257, 3112352382043137, '移交管理', 'entHandover', '', 'arcEnt/entArcManage/entHandover', 'M', 1, 0, 'slider', 0, 0, 2, '0', 0, '2023-02-07 09:56:09', '2023-02-07 12:44:19', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169485501588481, 3112352382043137, '鉴定销毁', 'entIdentDestruction', '', 'arcEnt/entArcManage/entIdentDestruction', 'M', 1, 0, 'log', 0, 0, 3, '0', 0, '2023-02-07 09:57:40', '2023-02-07 11:18:39', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169507873391617, 3169485501588481, '档案鉴定', 'entArcAppraisal', '', 'arcEnt/entArcManage/entIdentDestruction/entArcAppraisal', 'M', 1, 0, 'allselec', 0, 0, 1, '0', 0, '2023-02-07 10:43:11', '2023-02-07 12:16:59', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169508555056129, 3169485501588481, '销毁申请', 'entApplication', '', 'arcEnt/entArcManage/entIdentDestruction/entApplication', 'M', 1, 0, 'chart', 0, 0, 2, '0', 0, '2023-02-07 10:44:34', '2023-02-07 12:17:14', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169508767958017, 3169485501588481, '销毁审批', 'entApproval', '', 'arcEnt/entArcManage/entIdentDestruction/entApproval', 'M', 1, 0, 'component', 0, 0, 3, '0', 0, '2023-02-07 10:45:00', '2023-02-07 12:17:29', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169509028832258, 3169485501588481, '档案销毁', 'entDestruction', '', 'arcEnt/entArcManage/entIdentDestruction/entDestruction', 'M', 1, 0, 'dict', 0, 0, 4, '0', 0, '2023-02-07 10:45:32', '2023-02-07 12:17:45', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169509165958145, 3169485501588481, '销毁清册', 'entInventory', '', 'arcEnt/entArcManage/entIdentDestruction/entInventory', 'M', 1, 0, 'allselec', 0, 0, 5, '0', 0, '2023-02-07 10:45:49', '2023-02-07 12:18:00', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169525009777665, 3112352382043137, '档案管理', 'entArchives', '', 'arcEnt/entArcManage/entArchives', 'M', 1, 0, 'build', 0, 0, 1, '0', 0, '2023-02-07 11:18:03', '2023-02-07 12:44:08', 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169525986092034, 3169525009777665, '一件一档管理', 'entArcOne', '', '', 'M', 1, 0, 'documentation', 0, 0, 1, '0', 0, '2023-02-07 11:20:02', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169526501139458, 3169525009777665, '传统立卷管理', 'entTraditional', '', '', 'M', 1, 0, 'drag', 0, 0, 2, '0', 0, '2023-02-07 11:21:05', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169526909445121, 3169525009777665, '项目档案管理(按卷)', 'entProjectVolume', '', '', 'M', 1, 0, 'education', 0, 0, 3, '0', 0, '2023-02-07 11:21:55', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169527361823746, 3169525009777665, '项目档案管理(按件)', 'entProjectPiece', '', '', 'M', 1, 0, 'example', 0, 0, 4, '0', 0, '2023-02-07 11:22:50', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169528300487681, 3169525009777665, '人事档案管理(按卷)', 'entPersonnelVolume', '', '', 'M', 1, 0, 'form', 0, 0, 5, '0', 0, '2023-02-07 11:24:45', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169528629511169, 3169525009777665, '人事档案管理(按件)', 'entPersonnelPiece', '', '', 'M', 1, 0, 'chart', 0, 0, 6, '0', 0, '2023-02-07 11:25:25', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169547444036609, 3169525009777665, '会计档案管理(传统立卷)', 'entAccountVolume', '', '', 'M', 1, 0, 'education', 0, 0, 7, '0', 0, '2023-02-07 12:03:42', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169547666654209, 3169525009777665, '会计档案管理(一件一档)', 'entAccountPiece', '', '', 'M', 1, 0, 'example', 0, 0, 8, '0', 0, '2023-02-07 12:04:09', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169547929371650, 3169525009777665, '实物档案管理', 'entPhysical', '', '', 'M', 1, 0, 'guide', 0, 0, 9, '0', 0, '2023-02-07 12:04:41', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169568299246594, 3169484751840257, '档案移交', 'entArcReceive', '', '', 'M', 1, 0, 'exit-fullscreen', 0, 0, 1, '0', 0, '2023-02-07 12:46:07', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169568488514561, 3169484751840257, '档案接收', 'entArcTransfer', '', '', 'M', 1, 0, 'input', 0, 0, 2, '0', 0, '2023-02-07 12:46:31', NULL, 0);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, perms, path, component, menu_type, is_frame, is_cache, icon, status, visible, order_num, remark, permissions_type, create_time, update_time, is_deleted) VALUES (3169568853214210, 3169484751840257, '转递列表', 'entForward', '', '', 'M', 1, 0, 'folderList', 0, 0, 3, '0', 0, '2023-02-07 12:47:15', NULL, 0);

-- ----------------------------
-- Records of sys_param 必要
-- ----------------------------
INSERT INTO sys_param (id, name, value, type, remark, system_code, status, create_time, update_time, is_deleted) VALUES (1222, 'DOC_MAXIMUM_SIZE', '100', 0, '容量上限', NULL, 1, '2022-12-19 13:52:44', '2023-02-09 16:48:18', 0);

INSERT INTO sys_param (id, name, value, type, remark, system_code, status, create_time, update_time, is_deleted) VALUES (1223, 'DOC_RECYCLE_DAY_KEY', '60', 0, '回收站保留期限', NULL, 1, '2022-12-19 13:52:44', '2023-02-09 16:49:05', 0);

INSERT INTO sys_param (id, name, value, type, remark, system_code, status, create_time, update_time, is_deleted) VALUES (1224, 'DOC_FOLDER_TREE_TYPE', '1', 0, '企业文件夹视图', NULL, 1, '2022-12-19 13:52:44', '2023-02-09 16:49:01', 0);

UPDATE sys_param SET name = 'AUTOFOLDESYS', value = 'GATHERFIRST', type = 0, remark = NULL, system_code = 1, status = 1, create_time = '2022-06-28 15:50:44', update_time = '2022-12-09 17:46:20', is_deleted = 0 WHERE id = 3;

-- ----------------------------
-- Records of sys_permiss 
-- ----------------------------

-- ----------------------------
-- Records of sys_role 必要
-- ----------------------------

-- ----------------------------
-- Records of sys_role_menu 必要
-- ----------------------------

-- ----------------------------
-- Records of sys_role_user 必要
-- ----------------------------

-- ----------------------------
-- Records of sys_user 必要
-- ----------------------------

-- ----------------------------
-- Records of sys_user_admin 必要
-- ----------------------------

-- ----------------------------
-- Records of doc_sys_house
-- ----------------------------
INSERT INTO `doc_sys_house` VALUES (1, '企业文档库', 1, NULL, NULL, '2022-12-19 10:51:12', '2022-12-19 10:51:12', 0);
INSERT INTO `doc_sys_house_user` VALUES (3174041952035842, 1, 101, 0, 2, '2023-02-13 20:27:48', NULL, 0);
INSERT INTO `doc_sys_house_user` VALUES (3174414541300737, 1, 2851889946633218, 1, 2, '2023-02-14 09:05:50', NULL, 0);


INSERT INTO `sys_dictionary` VALUES (3163960877499393, 'DOC_APPROVAL_PENDING', '0', '待审批', 0, 3, 3163961967543297, 5, NULL, '2023-01-30 14:37:48', NULL, 0);
INSERT INTO `sys_dictionary` VALUES (3163961569231874, 'DOC_APPROVAL_UNDER', '1', '审批中', 1, 3, 3163961967543297, 5, NULL, '2023-01-30 14:39:12', NULL, 0);
INSERT INTO `sys_dictionary` VALUES (3163961967543297, 'DOC_APPROVAL', NULL, '文档上架审批状态', NULL, 1, 3125820798116865, 5, NULL, '2023-01-30 14:40:01', NULL, 0);
INSERT INTO `sys_dictionary` VALUES (3175866762765313, 'DOC_SHAPE_SECTION_ONE', '3', '单次', 3, 5, 3126493286335489, 5, NULL, '2023-02-16 10:20:23', NULL, 0);