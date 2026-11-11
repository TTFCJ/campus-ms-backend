-- =====================================================================
-- 学生关爱平台 / 宿舍归寝 / 第二课堂
-- 依赖：01_schema.sql、02_init_data.sql 已执行
-- 执行：mysql -uroot -p < 04_care_dorm_ec.sql
-- 说明：本脚本只新增表、菜单、授权、演示数据，不改已有表
-- =====================================================================
USE `campus_ms`;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 一、学生关爱平台
-- ---------------------------------------------------------------------

-- 1. care_target 关爱对象
CREATE TABLE IF NOT EXISTS `care_target` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`   BIGINT       NOT NULL COMMENT '学生ID',
  `tags`         VARCHAR(200) DEFAULT NULL COMMENT '标签，逗号分隔：经济困难,学业预警,心理关注',
  `risk_level`   TINYINT      NOT NULL DEFAULT 1 COMMENT '风险等级 1一般 2关注 3重点',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1在册 2已解除',
  `reason`       VARCHAR(500) DEFAULT NULL COMMENT '纳入原因',
  `counselor_id` BIGINT       DEFAULT NULL COMMENT '负责辅导员 user_id',
  `create_by`    VARCHAR(64)  DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64)  DEFAULT NULL,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`   TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student` (`student_id`, `is_deleted`),
  KEY `idx_risk` (`risk_level`, `status`),
  KEY `idx_counselor` (`counselor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关爱对象';

-- 2. care_record 关爱记录（谈心/家访/帮扶/预警处置）
CREATE TABLE IF NOT EXISTS `care_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `target_id`    BIGINT       NOT NULL COMMENT '关爱对象ID',
  `student_id`   BIGINT       NOT NULL COMMENT '学生ID（冗余）',
  `record_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '1谈心谈话 2家访 3帮扶 4预警处置',
  `title`        VARCHAR(200) DEFAULT NULL COMMENT '标题',
  `content`      TEXT         COMMENT '详细内容',
  `result`       VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
  `recorder_id`  BIGINT       DEFAULT NULL COMMENT '记录人 user_id',
  `record_time`  DATETIME     NOT NULL COMMENT '发生时间',
  `attachment`   VARCHAR(1000) DEFAULT NULL COMMENT '附件',
  `create_by`    VARCHAR(64)  DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64)  DEFAULT NULL,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`   TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_time` (`record_time`),
  KEY `idx_type` (`record_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关爱记录';

-- ---------------------------------------------------------------------
-- 二、宿舍归寝
-- ---------------------------------------------------------------------

-- 3. dorm_checkin 归寝记录
CREATE TABLE IF NOT EXISTS `dorm_checkin` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  BIGINT       NOT NULL COMMENT '学生ID',
  `room_id`     BIGINT       DEFAULT NULL COMMENT '房间ID',
  `check_date`  DATE         NOT NULL COMMENT '查寝日期',
  `check_time`  DATETIME     DEFAULT NULL COMMENT '打卡/确认时间',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 2晚归 3未归 4请假',
  `source`      TINYINT      NOT NULL DEFAULT 2 COMMENT '来源 1门禁 2手动 3扫码',
  `remark`      VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_date` (`student_id`, `check_date`, `is_deleted`),
  KEY `idx_date` (`check_date`),
  KEY `idx_status_date` (`status`, `check_date`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='归寝记录';

-- ---------------------------------------------------------------------
-- 三、第二课堂
-- ---------------------------------------------------------------------

-- 4. ec_activity 活动
CREATE TABLE IF NOT EXISTS `ec_activity` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(200)  NOT NULL COMMENT '活动名称',
  `category`     TINYINT       NOT NULL DEFAULT 1 COMMENT '1讲座 2志愿服务 3社团 4竞赛 5社会实践 6文体',
  `credit`       DECIMAL(3,1)  NOT NULL DEFAULT 0.5 COMMENT '可获学分',
  `start_time`   DATETIME      NOT NULL COMMENT '开始时间',
  `end_time`     DATETIME      NOT NULL COMMENT '结束时间',
  `location`     VARCHAR(200)  DEFAULT NULL COMMENT '地点',
  `capacity`     INT           NOT NULL DEFAULT 100 COMMENT '人数上限',
  `enroll_count` INT           NOT NULL DEFAULT 0 COMMENT '已报名人数',
  `cover_url`    VARCHAR(500)  DEFAULT NULL COMMENT '封面图',
  `content`      TEXT          COMMENT '活动详情',
  `publisher_id` BIGINT        DEFAULT NULL COMMENT '发布人 user_id',
  `status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '0草稿 1报名中 2进行中 3已结束 4已取消',
  `create_by`    VARCHAR(64)   DEFAULT NULL,
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64)   DEFAULT NULL,
  `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`   TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`start_time`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第二课堂活动';

-- 5. ec_enroll 活动报名
CREATE TABLE IF NOT EXISTS `ec_enroll` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id`  BIGINT   NOT NULL COMMENT '活动ID',
  `student_id`   BIGINT   NOT NULL COMMENT '学生ID',
  `enroll_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `checkin_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `status`       TINYINT  NOT NULL DEFAULT 1 COMMENT '1已报名 2已签到 3已取消',
  `create_by`    VARCHAR(64)  DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64)  DEFAULT NULL,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`   TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_student` (`activity_id`, `student_id`, `is_deleted`),
  KEY `idx_student` (`student_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动报名';

-- =====================================================================
-- 四、菜单（挂到一级目录下）
-- =====================================================================

-- 90 学生关爱 / 93 宿舍考勤 / 96 第二课堂
INSERT INTO `sys_menu`
  (`id`,`parent_id`,`menu_name`,`menu_type`,`path`,`component`,`perms`,`icon`,`sort`,`visible`)
VALUES
  (90, 0, '学生关爱', 'M', '/care', NULL, NULL, 'Heart', 9, 1),
  (91, 90, '关爱对象', 'C', 'target', 'care/target/index', 'care:target:list', 'Heart', 1, 1),
  (92, 90, '关爱记录', 'C', 'record', 'care/record/index', 'care:record:list', 'ChatCircleDots', 2, 1),

  (93, 0, '宿舍考勤', 'M', '/dorm', NULL, NULL, 'Bed', 10, 1),
  (94, 93, '归寝记录', 'C', 'checkin', 'dorm/checkin/index', 'dorm:checkin:list', 'Bed', 1, 1),
  (95, 93, '异常预警', 'C', 'alert', 'dorm/alert/index', 'dorm:checkin:alert', 'Warning', 2, 1),

  (96, 0, '第二课堂', 'M', '/ec', NULL, NULL, 'Trophy', 11, 1),
  (97, 96, '活动管理', 'C', 'activity', 'ec/activity/index', 'ec:activity:list', 'Trophy', 1, 1),
  (98, 96, '我的活动', 'C', 'my', 'ec/my/index', 'ec:enroll:list', 'Star', 2, 1),
  (99, 96, '学分统计', 'C', 'credit', 'ec/credit/index', 'ec:credit:stat', 'ChartBar', 3, 1);

-- 按钮
INSERT INTO `sys_menu`
  (`id`,`parent_id`,`menu_name`,`menu_type`,`perms`,`sort`,`visible`)
VALUES
  (9101, 91, '新增关爱对象', 'F', 'care:target:add', 1, 0),
  (9102, 91, '修改关爱对象', 'F', 'care:target:edit', 2, 0),
  (9103, 91, '解除关爱', 'F', 'care:target:remove', 3, 0),

  (9201, 92, '新增记录', 'F', 'care:record:add', 1, 0),
  (9202, 92, '修改记录', 'F', 'care:record:edit', 2, 0),
  (9203, 92, '删除记录', 'F', 'care:record:remove', 3, 0),

  (9401, 94, '登记归寝', 'F', 'dorm:checkin:add', 1, 0),
  (9402, 94, '修改归寝', 'F', 'dorm:checkin:edit', 2, 0),
  (9403, 94, '导出记录', 'F', 'dorm:checkin:export', 3, 0),

  (9701, 97, '发布活动', 'F', 'ec:activity:add', 1, 0),
  (9702, 97, '修改活动', 'F', 'ec:activity:edit', 2, 0),
  (9703, 97, '删除活动', 'F', 'ec:activity:remove', 3, 0),
  (9704, 97, '活动签到', 'F', 'ec:activity:checkin', 4, 0),

  (9801, 98, '报名活动', 'F', 'ec:enroll:add', 1, 0),
  (9802, 98, '取消报名', 'F', 'ec:enroll:remove', 2, 0);

-- =====================================================================
-- 五、角色授权
-- =====================================================================

-- 超管：新增的 10 个菜单 + 17 个按钮
-- 注意范围要写死：已有按钮 ID 是 1101~8401，也会命中 id >= 90
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu`
WHERE (id BETWEEN 90 AND 99 OR id BETWEEN 9101 AND 9899) AND is_deleted = 0;

-- 辅导员：关爱对象 + 关爱记录 + 归寝 + 第二课堂
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (5, 90), (5, 91), (5, 92), (5, 9101), (5, 9102), (5, 9103), (5, 9201), (5, 9202), (5, 9203),
  (5, 93), (5, 94), (5, 95), (5, 9401), (5, 9402),
  (5, 96), (5, 97), (5, 98), (5, 99), (5, 9701), (5, 9702), (5, 9704), (5, 9801), (5, 9802);

-- 学工管理员：同辅导员 + 全部
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (6, 90), (6, 91), (6, 92), (6, 9101), (6, 9102), (6, 9103), (6, 9201), (6, 9202), (6, 9203),
  (6, 93), (6, 94), (6, 95), (6, 9401), (6, 9402), (6, 9403),
  (6, 96), (6, 97), (6, 98), (6, 99), (6, 9701), (6, 9702), (6, 9703), (6, 9704);

-- 后勤：归寝
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (8, 93), (8, 94), (8, 95), (8, 9401), (8, 9402), (8, 9403);

-- 学生：只看第二课堂
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (7, 96), (7, 98), (7, 99), (7, 9801), (7, 9802);

-- 教务 / 校级：只读第二课堂
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (3, 96), (3, 97), (3, 99),
  (2, 96), (2, 97), (2, 99);

-- =====================================================================
-- 六、演示数据
-- =====================================================================

-- 关爱对象（学生 1、3 纳入关爱）
INSERT INTO `care_target` (`student_id`, `tags`, `risk_level`, `status`, `reason`, `counselor_id`)
VALUES
  (1, '经济困难,学业预警', 2, 1, '家庭经济困难，本学期已有两门课不及格', 5),
  (3, '心理关注', 3, 1, '近期情绪波动较大，需持续关注', 5);

-- 关爱记录
INSERT INTO `care_record` (`target_id`, `student_id`, `record_type`, `title`, `content`, `result`, `recorder_id`, `record_time`)
VALUES
  (1, 1, 1, '首次谈心', '了解家庭情况：父母务农，家中还有一名在读高中生。学生本人有较强的学业压力。', '已建立帮扶档案，安排勤工助学岗位', 5, NOW() - INTERVAL 10 DAY),
  (1, 1, 3, '勤工助学岗位落实', '安排到图书馆做图书整理，每周 8 小时，每月补助 400 元。', '已上岗', 5, NOW() - INTERVAL 5 DAY),
  (2, 3, 4, '心理预警处置', '班主任反馈该生近两周缺课较多，谈话后了解到与室友关系紧张。', '已协调调换宿舍，并联系心理中心', 5, NOW() - INTERVAL 3 DAY);

-- 归寝记录（最近 3 天）
INSERT INTO `dorm_checkin` (`student_id`, `room_id`, `check_date`, `check_time`, `status`, `source`, `remark`)
VALUES
  (1, 1, CURDATE(), NOW(), 1, 1, NULL),
  (2, 1, CURDATE(), NOW(), 2, 1, '23:47 回寝'),
  (3, 5, CURDATE(), NULL, 3, 1, '未归寝'),
  (4, 6, CURDATE(), NOW(), 4, 2, '已请假回家'),
  (1, 1, CURDATE() - INTERVAL 1 DAY, NOW(), 1, 1, NULL),
  (2, 1, CURDATE() - INTERVAL 1 DAY, NOW(), 1, 1, NULL),
  (3, 5, CURDATE() - INTERVAL 1 DAY, NOW(), 2, 1, '00:12 回寝');

-- 第二课堂活动
INSERT INTO `ec_activity`
  (`title`,`category`,`credit`,`start_time`,`end_time`,`location`,`capacity`,`enroll_count`,`content`,`publisher_id`,`status`)
VALUES
  ('新生入学教育讲座', 1, 0.5, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 3 DAY + INTERVAL 2 HOUR,
   '图书馆报告厅', 300, 0, '介绍校史校情、学籍管理规定、安全教育', 4, 1),
  ('图书馆志愿服务', 2, 1.0, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 4 HOUR,
   '图书馆', 30, 0, '图书整理、读者引导', 4, 1),
  ('程序设计竞赛校内选拔', 4, 2.0, NOW() + INTERVAL 10 DAY, NOW() + INTERVAL 10 DAY + INTERVAL 3 HOUR,
   '实验楼机房', 60, 0, '选拔参加省赛的队员', 6, 1),
  ('校园歌手大赛决赛', 6, 0.5, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 3 HOUR,
   '大学生活动中心', 500, 0, '决赛现场', 4, 3);

-- 报名记录（学生 1 报了两个）
INSERT INTO `ec_enroll` (`activity_id`, `student_id`, `enroll_time`, `checkin_time`, `status`)
VALUES
  (1, 1, NOW() - INTERVAL 1 DAY, NULL, 1),
  (2, 1, NOW() - INTERVAL 1 DAY, NULL, 1),
  (4, 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, 2);

-- 报名人数字段同步
UPDATE `ec_activity` a
SET a.enroll_count = (SELECT COUNT(*) FROM `ec_enroll` e
                      WHERE e.activity_id = a.id AND e.status IN (1, 2) AND e.is_deleted = 0);

SELECT '完成：新增 5 张表、10 个菜单、17 个按钮' AS result;
