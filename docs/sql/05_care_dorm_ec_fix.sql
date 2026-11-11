-- =====================================================================
-- 04_care_dorm_ec.sql 的补丁：授权 + 演示数据
-- 04 执行到「角色授权」时因超管条件过宽而中断（已修复原文件），
-- 本脚本用 INSERT IGNORE 幂等补齐剩余部分。
-- =====================================================================
USE `campus_ms`;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------- 超管
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu`
WHERE (id BETWEEN 90 AND 99 OR id BETWEEN 9101 AND 9899) AND is_deleted = 0;

-- ---------------------------------------------------------------- 辅导员
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (5, 90), (5, 91), (5, 92), (5, 9101), (5, 9102), (5, 9103), (5, 9201), (5, 9202), (5, 9203),
  (5, 93), (5, 94), (5, 95), (5, 9401), (5, 9402),
  (5, 96), (5, 97), (5, 98), (5, 99), (5, 9701), (5, 9702), (5, 9704), (5, 9801), (5, 9802);

-- ---------------------------------------------------------------- 学工
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (6, 90), (6, 91), (6, 92), (6, 9101), (6, 9102), (6, 9103), (6, 9201), (6, 9202), (6, 9203),
  (6, 93), (6, 94), (6, 95), (6, 9401), (6, 9402), (6, 9403),
  (6, 96), (6, 97), (6, 98), (6, 99), (6, 9701), (6, 9702), (6, 9703), (6, 9704);

-- ---------------------------------------------------------------- 后勤
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (8, 93), (8, 94), (8, 95), (8, 9401), (8, 9402), (8, 9403);

-- ---------------------------------------------------------------- 学生
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (7, 96), (7, 98), (7, 99), (7, 9801), (7, 9802);

-- ---------------------------------------------------------------- 教务 / 校级
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (3, 96), (3, 97), (3, 99),
  (2, 96), (2, 97), (2, 99);

-- =====================================================================
-- 演示数据
-- =====================================================================

INSERT IGNORE INTO `care_target` (`id`, `student_id`, `tags`, `risk_level`, `status`, `reason`, `counselor_id`)
VALUES
  (1, 1, '经济困难,学业预警', 2, 1, '家庭经济困难，本学期已有两门课不及格', 5),
  (2, 3, '心理关注', 3, 1, '近期情绪波动较大，需持续关注', 5);

INSERT IGNORE INTO `care_record` (`id`, `target_id`, `student_id`, `record_type`, `title`, `content`, `result`, `recorder_id`, `record_time`)
VALUES
  (1, 1, 1, 1, '首次谈心', '了解家庭情况：父母务农，家中还有一名在读高中生。学生本人有较强的学业压力。', '已建立帮扶档案，安排勤工助学岗位', 5, NOW() - INTERVAL 10 DAY),
  (2, 1, 1, 3, '勤工助学岗位落实', '安排到图书馆做图书整理，每周 8 小时，每月补助 400 元。', '已上岗', 5, NOW() - INTERVAL 5 DAY),
  (3, 2, 3, 4, '心理预警处置', '班主任反馈该生近两周缺课较多，谈话后了解到与室友关系紧张。', '已协调调换宿舍，并联系心理中心', 5, NOW() - INTERVAL 3 DAY);

INSERT IGNORE INTO `dorm_checkin` (`id`, `student_id`, `room_id`, `check_date`, `check_time`, `status`, `source`, `remark`)
VALUES
  (1, 1, 1, CURDATE(), NOW(), 1, 1, NULL),
  (2, 2, 1, CURDATE(), NOW(), 2, 1, '23:47 回寝'),
  (3, 3, 5, CURDATE(), NULL, 3, 1, '未归寝'),
  (4, 4, 6, CURDATE(), NOW(), 4, 2, '已请假回家'),
  (5, 1, 1, CURDATE() - INTERVAL 1 DAY, NOW(), 1, 1, NULL),
  (6, 2, 1, CURDATE() - INTERVAL 1 DAY, NOW(), 1, 1, NULL),
  (7, 3, 5, CURDATE() - INTERVAL 1 DAY, NOW(), 2, 1, '00:12 回寝');

INSERT IGNORE INTO `ec_activity`
  (`id`,`title`,`category`,`credit`,`start_time`,`end_time`,`location`,`capacity`,`enroll_count`,`content`,`publisher_id`,`status`)
VALUES
  (1, '新生入学教育讲座', 1, 0.5, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 3 DAY + INTERVAL 2 HOUR,
   '图书馆报告厅', 300, 0, '介绍校史校情、学籍管理规定、安全教育', 4, 1),
  (2, '图书馆志愿服务', 2, 1.0, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 4 HOUR,
   '图书馆', 30, 0, '图书整理、读者引导', 4, 1),
  (3, '程序设计竞赛校内选拔', 4, 2.0, NOW() + INTERVAL 10 DAY, NOW() + INTERVAL 10 DAY + INTERVAL 3 HOUR,
   '实验楼机房', 60, 0, '选拔参加省赛的队员', 6, 1),
  (4, '校园歌手大赛决赛', 6, 0.5, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY + INTERVAL 3 HOUR,
   '大学生活动中心', 500, 0, '决赛现场', 4, 3);

INSERT IGNORE INTO `ec_enroll` (`id`, `activity_id`, `student_id`, `enroll_time`, `checkin_time`, `status`)
VALUES
  (1, 1, 1, NOW() - INTERVAL 1 DAY, NULL, 1),
  (2, 2, 1, NOW() - INTERVAL 1 DAY, NULL, 1),
  (3, 4, 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, 2);

UPDATE `ec_activity` a
SET a.enroll_count = (
  SELECT COUNT(*) FROM `ec_enroll` e
  WHERE e.activity_id = a.id AND e.status IN (1, 2) AND e.is_deleted = 0
);

SELECT '补丁完成' AS result;
