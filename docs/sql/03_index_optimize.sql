-- =====================================================================
-- 高校综合管理系统 · 索引优化脚本（最终版）
-- 依赖: 01_schema.sql 已执行
-- 特点: 建索引的同时删掉被复合索引左前缀覆盖的冗余单列索引，
--       每张表索引控制在 6 个以内（写多读少的表更少）
-- 执行: mysql -uroot -p < 03_index_optimize.sql
-- =====================================================================
USE `campus_ms`;

-- ---------------------------------------------------------------------
-- 设计思路
--
-- 教务系统 90% 的查询是这四种形态：
--   A. 某人的列表 + 时间倒序            -> (owner_id, create_time)
--   B. 某人的待办 + 状态过滤             -> (owner_id, status, create_time)
--   C. 某学期的课表/成绩                 -> (semester_id, clazz_id/teacher_id)
--   D. 冲突检测（同一时段只能排一门课）   -> (owner_id, semester_id, day_of_week, section_start)
--
-- 三条铁律：
--   1. 等值条件放前，范围/排序放后
--   2. 单列索引只要有复合索引能覆盖左前缀，就删掉（省写入开销）
--   3. 冲突检测索引把"最常用的第一个等值列"放最前，才能同时服务于单维查询
-- ---------------------------------------------------------------------


-- =====================================================================
-- 一、成绩表 score（大表，重点优化）
-- =====================================================================

-- 学生 + 学期：学生端「我的成绩」按学期查
ALTER TABLE `score` ADD INDEX `idx_stu_sem` (`student_id`, `semester_id`, `exam_type`);

-- 排课 + 学生：教师录入成绩页拉教学班名单（也覆盖了原 idx_schedule 的左前缀）
ALTER TABLE `score` DROP INDEX `idx_schedule`,
                  ADD INDEX `idx_sch_stu` (`schedule_id`, `student_id`);

-- 课程 + 学期：挂科率/平均分统计（第三列 score 用于覆盖，避免回表）
ALTER TABLE `score` ADD INDEX `idx_course_sem` (`course_id`, `semester_id`, `score`);


-- =====================================================================
-- 二、选课表 elect_course
-- =====================================================================

-- 教学班 + 状态：已选人数、选课名单（选课高峰每秒几十次）
ALTER TABLE `elect_course` ADD INDEX `idx_sch_status` (`schedule_id`, `status`);

-- 学生 + 状态：我的课表
ALTER TABLE `elect_course` ADD INDEX `idx_stu_status` (`student_id`, `status`, `select_time`);


-- =====================================================================
-- 三、请假 / 助学金（审批流最热的查询：待办列表）
-- =====================================================================

-- 请假：审批人待办。多带 create_time，消除 ORDER BY 的 filesort
ALTER TABLE `leave_apply`
  DROP INDEX `idx_approver`,
  ADD INDEX `idx_approver_time` (`current_approver`, `status`, `create_time`);

-- 请假：学生历史（idx_stu_time 左前缀已覆盖原 idx_student，删掉单列）
ALTER TABLE `leave_apply`
  DROP INDEX `idx_student`,
  ADD INDEX `idx_stu_time` (`student_id`, `create_time`);

-- 助学金：同请假
ALTER TABLE `aid_apply`
  DROP INDEX `idx_approver`,
  ADD INDEX `idx_approver_time` (`current_approver`, `status`, `create_time`);

ALTER TABLE `aid_apply`
  DROP INDEX `idx_student`,
  ADD INDEX `idx_stu_time` (`student_id`, `create_time`);

-- 审批流水：按业务单拉时间轴
ALTER TABLE `approval_record` ADD INDEX `idx_biz_node` (`biz_type`, `biz_id`, `node`);


-- =====================================================================
-- 四、报修工单
-- =====================================================================

-- 维修工待办（覆盖原 idx_handler）
ALTER TABLE `repair_order`
  DROP INDEX `idx_handler`,
  ADD INDEX `idx_handler_status` (`handler_id`, `status`);

-- 后勤看板按状态筛
ALTER TABLE `repair_order`
  DROP INDEX `idx_status`,
  ADD INDEX `idx_status_time` (`status`, `create_time`);

-- 学生历史（覆盖原 idx_student）
ALTER TABLE `repair_order`
  DROP INDEX `idx_student`,
  ADD INDEX `idx_stu_time` (`student_id`, `create_time`);


-- =====================================================================
-- 五、排课表 schedule（冲突检测核心）
-- 列顺序刻意用「业务第一列在左」，这样一个索引同时服务单维查询和冲突检测
-- =====================================================================

-- 班级冲突检测 / 班级课表
ALTER TABLE `schedule`
  DROP INDEX `idx_clazz`, DROP INDEX `idx_semester`,
  ADD INDEX `idx_clazz_slot` (`clazz_id`, `semester_id`, `day_of_week`, `section_start`, `section_end`);

-- 教师冲突检测 / 教师课表
ALTER TABLE `schedule`
  DROP INDEX `idx_teacher`,
  ADD INDEX `idx_teacher_slot` (`teacher_id`, `semester_id`, `day_of_week`, `section_start`, `section_end`);

-- 教室冲突检测
ALTER TABLE `schedule`
  ADD INDEX `idx_room_slot` (`classroom_id`, `semester_id`, `day_of_week`, `section_start`, `section_end`);

-- 注：原 idx_slot(semester_id, day_of_week, section_start) 保留，
--     用于「某学期某天全校课表」这类不按班级/教师过滤的查询。


-- =====================================================================
-- 六、学生 / 用户
-- =====================================================================

-- 班级 + 学籍状态 + 学号：辅导员看本班学生（数据权限拦截器拼 clazz_id IN (...)）
ALTER TABLE `student`
  DROP INDEX `idx_clazz`,
  ADD INDEX `idx_clazz_in` (`clazz_id`, `is_in_school`, `stu_no`);

-- 用户列表按类型/状态筛
ALTER TABLE `sys_user` ADD INDEX `idx_type_status` (`user_type`, `status`);


-- =====================================================================
-- 七、公告 / 消息
-- =====================================================================

-- 公告列表：范围 + 状态 + 置顶 + 时间
ALTER TABLE `notice`
  DROP INDEX `idx_scope`,
  ADD INDEX `idx_scope_time` (`receive_scope`, `status`, `is_top`, `publish_time`);

-- 未读消息数（首页小红点，每次进页面都查）
ALTER TABLE `sys_message`
  DROP INDEX `idx_receiver`,
  ADD INDEX `idx_receiver_read` (`receiver_id`, `is_read`, `create_time`);


-- =====================================================================
-- 八、日志表（只写少读，索引克制到 3-4 个）
-- =====================================================================

ALTER TABLE `sys_oper_log` ADD INDEX `idx_user_time` (`username`, `oper_time`);
ALTER TABLE `sys_login_log` ADD INDEX `idx_user_time` (`username`, `login_time`);


-- =====================================================================
-- 九、验收：索引数量检查
-- =====================================================================

SELECT TABLE_NAME, COUNT(DISTINCT INDEX_NAME) AS idx_cnt
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'campus_ms'
GROUP BY TABLE_NAME
ORDER BY idx_cnt DESC
LIMIT 15;


-- =====================================================================
-- 十、慢 SQL 排查工具（需要时手工执行）
-- =====================================================================
--
-- 1) 开慢查询日志
-- SET GLOBAL slow_query_log = ON;
-- SET GLOBAL long_query_time = 1;
-- SET GLOBAL log_queries_not_using_indexes = ON;   -- 生产慎用，小表全扫也会刷
--
-- 2) 看正在执行的 SQL
-- SELECT id, user, host, db, command, time, state, LEFT(info,120)
-- FROM information_schema.processlist
-- WHERE command <> 'Sleep' ORDER BY time DESC;
--
-- 3) 看执行计划（8.0.18+ 可以用 EXPLAIN ANALYZE 看真实耗时）
-- EXPLAIN SELECT ...;
-- EXPLAIN ANALYZE SELECT ...;
--
-- 4) 找没被用过的索引（可以删）
-- SELECT * FROM sys.schema_unused_indexes;
--
-- 5) 找冗余索引
-- SELECT * FROM sys.schema_redundant_indexes;
--
-- 6) 深分页改写
-- 慢： SELECT * FROM score WHERE semester_id=1 ORDER BY id LIMIT 100000, 10;
-- 快： SELECT s.* FROM score s
--      INNER JOIN (SELECT id FROM score WHERE semester_id=1 ORDER BY id LIMIT 100000, 10) t
--      ON s.id = t.id;
--
-- 7) 确认某条 SQL 的扫描行数
-- EXPLAIN FORMAT=JSON SELECT ...;   -- 看 rows_examined_per_scan
-- =====================================================================
