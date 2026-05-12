# Draft: Aibridge Dropdown Implementation

## Requirements (confirmed)
- 添加下拉选择框用于选择关联业务系统（例如审单），流程类型，业务类型
- 从ECM_AIBRIDGE_TEMP表获取流程类型、code以及业务类型和code
- aibridge流程类型，业务类型，用户ID关联信息由aibridge提供，通过数据库dblink同步
- aibridge业务类型可选项为当前提交人有权限的aibridge业务类型（提交人ID与aibridge业务类型的权限关联关系由aibridge同步表提供）
- 用户点击提交时，文档系统根据当前提交人ID关联查询aibridge同步表中的用户ID，获取并在弹窗中展示其对应权限的aibridge业务类型
- 当提交人ID在aibridge同步表中不存在时，将弹窗进行提示，用户可根据提示去aibridge系统中手动创建用户等信息
- 业务人员选择aibridge业务类型，点击确定后，相关信息写入待处理清单表
- 待处理清单表结构包含：busi_id, busi_no, app_code, DELEGATE_TYPE, DELEGATE_TYPE_NAME, TYPE_BIG, TYPE_BIG_NAME, busi_type, right_ver, tree_type, org_code, create_user, create_time, update_user, update_time, is_deleted, create_user_name, update_user_name, org_name, status
- 需要封装返回数据为树形结构JSON格式，类似示例提供的格式

## Technical Decisions
- 实体类位置：ecm模块，com.sunyard.edm.po包（用户确认）
- 服务类：新建AibridgeSyncService（用户确认）
- 控制器：新建AibridgeController（用户确认）
- 当前提交人ID获取：通过方法参数传入（用户确认）
- 树形结构构建：根据业务逻辑构建固定层级（用户确认）
- 返回格式：树形结构JSON，包含appCode, appName, parent, children等字段
- 数据量处理：不需要分页，数据量小（用户确认）

## Research Findings
- ECM_AIBRIDGE_TEMP表存在（通过用户描述确认）
- 表结构包括：BRANCH_NAME, BRANCH_NUMBER, DELEGATE_TYPE, DELEGATE_TYPE_NAME, TYPE_BIG, TYPE_BIG_NAME, USER_ID, USER_SHOW_ID, USER_NAME
- 待处理清单表结构已提供

## Open Questions
- 下拉选择框在何处触发？（需要确定具体的UI位置和触发时机）
- 是否需要考虑缓存查询结果？

## Scope Boundaries
- INCLUDE: 
  * 创建ECM_AIBRIDGE_TEMP实体类
  * 创建AibridgeSyncService查询方法
  * 创建AibridgeController返回树形结构数据
  * 基于当前用户ID过滤可选业务类型
  * 处理用户不在同步表中的情况（提示）
  * 返回符合要求的JSON树形结构
- EXCLUDE:
  * 实际的UI下拉选择框实现（前端工作）
  * 待处理清单表的写入（单独功能）
  * aibridge数据同步机制（由aibridge系统负责）
  * 数据库dblink配置（已存在）
