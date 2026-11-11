-- 清理压测数据（学号 B 开头 / 手机号 900000+）
USE `campus_ms`;

-- 先删子表
DELETE FROM `score` WHERE `student_id` IN (SELECT id FROM `student` WHERE `stu_no` LIKE 'B%');
DELETE FROM `leave_apply` WHERE `apply_no` LIKE 'BENCH%';
DELETE FROM `elect_course` WHERE `student_id` IN (SELECT id FROM `student` WHERE `stu_no` LIKE 'B%');
DELETE FROM `student` WHERE `stu_no` LIKE 'B%';

-- 重置自增（可选）
-- ALTER TABLE `student` AUTO_INCREMENT = 100;

SELECT 'score' t, COUNT(*) c FROM `score`
UNION ALL SELECT 'leave_apply', COUNT(*) FROM `leave_apply`
UNION ALL SELECT 'elect_course', COUNT(*) FROM `elect_course`
UNION ALL SELECT 'student', COUNT(*) FROM `student`;
