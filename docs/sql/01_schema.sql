-- =====================================================================
-- 高校综合管理系统 · 数据库结构脚本
-- DBMS  : MySQL 8.0.16+ （InnoDB / utf8mb4 / 无物理外键）
-- 库名  : campus_ms
-- 说明  : 主键统一 BIGINT 自增；逻辑删除 is_deleted；公共字段四件套
-- 执行  : mysql -uroot -p < 01_schema.sql
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `campus_ms`;
CREATE DATABASE `campus_ms`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `campus_ms`;

-- =====================================================================
-- 一、系统基础模块
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. sys_user 用户表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`        VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  `password`        VARCHAR(100) NOT NULL                COMMENT 'BCrypt 密码',
  `nickname`        VARCHAR(50)  NOT NULL                COMMENT '姓名/昵称',
  `real_name`       VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
  `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `email`           VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像 URL',
  `user_type`       TINYINT      NOT NULL DEFAULT 6      COMMENT '1超管 2校级 3教务 4教师 5辅导员 6学生 7后勤 8维修',
  `dept_id`         BIGINT       DEFAULT NULL            COMMENT '所属院系ID',
  `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1正常',
  `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
  `last_login_ip`   VARCHAR(64)  DEFAULT NULL            COMMENT '最后登录IP',
  `remark`          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_by`       VARCHAR(64)  DEFAULT NULL            COMMENT '创建人',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       VARCHAR(64)  DEFAULT NULL            COMMENT '更新人',
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`, `is_deleted`),
  KEY `idx_dept`   (`dept_id`),
  KEY `idx_type`   (`user_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ---------------------------------------------------------------------
-- 2. sys_role 角色表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
  `role_code`   VARCHAR(64)  NOT NULL                COMMENT '角色标识 ROLE_XXX',
  `data_scope`  TINYINT      NOT NULL DEFAULT 4      COMMENT '数据权限 1全部 2本院系 3本班 4仅本人 5本部门',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1正常',
  `remark`      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ---------------------------------------------------------------------
-- 3. sys_user_role 用户角色关联表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_user_role` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
  `create_by`   VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`, `is_deleted`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- ---------------------------------------------------------------------
-- 4. sys_menu 菜单表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_menu` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID 0=根',
  `menu_name`   VARCHAR(50)  NOT NULL                COMMENT '菜单名称',
  `menu_type`   CHAR(1)      NOT NULL DEFAULT 'C'    COMMENT 'M目录 C菜单 F按钮',
  `path`        VARCHAR(200) DEFAULT NULL            COMMENT '路由地址',
  `component`   VARCHAR(255) DEFAULT NULL            COMMENT '组件路径',
  `perms`       VARCHAR(100) DEFAULT NULL            COMMENT '权限标识 edu:course:list',
  `icon`        VARCHAR(100) DEFAULT NULL            COMMENT '图标',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `visible`     TINYINT      NOT NULL DEFAULT 1      COMMENT '是否显示 0隐藏 1显示',
  `keep_alive`  TINYINT      NOT NULL DEFAULT 0      COMMENT '是否缓存 0否 1是',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1正常',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_perms`  (`perms`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- ---------------------------------------------------------------------
-- 5. sys_role_menu 角色菜单关联表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_role_menu` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
  `menu_id`     BIGINT   NOT NULL COMMENT '菜单ID',
  `create_by`   VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`, `is_deleted`),
  KEY `idx_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

-- ---------------------------------------------------------------------
-- 6. sys_dept 院系/组织表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_dept` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父级ID 0=根',
  `ancestors`   VARCHAR(500) NOT NULL DEFAULT '0'    COMMENT '祖级路径 0,1,2',
  `dept_name`   VARCHAR(100) NOT NULL                COMMENT '院系/部门名称',
  `dept_code`   VARCHAR(50)  NOT NULL                COMMENT '院系编码',
  `leader`      VARCHAR(50)  DEFAULT NULL            COMMENT '负责人',
  `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1正常',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`, `is_deleted`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='院系/组织表';

-- ---------------------------------------------------------------------
-- 7. major 专业表
-- ---------------------------------------------------------------------
CREATE TABLE `major` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dept_id`       BIGINT       NOT NULL                COMMENT '所属院系',
  `major_name`    VARCHAR(100) NOT NULL                COMMENT '专业名称',
  `major_code`    VARCHAR(50)  NOT NULL                COMMENT '专业代码',
  `length_school` TINYINT      NOT NULL DEFAULT 4      COMMENT '学制(年)',
  `degree`        VARCHAR(20)  DEFAULT NULL            COMMENT '授予学位',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0停招 1正常',
  `create_by`     VARCHAR(64)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_major_code` (`major_code`, `is_deleted`),
  KEY `idx_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业表';

-- ---------------------------------------------------------------------
-- 8. clazz 班级表
-- ---------------------------------------------------------------------
CREATE TABLE `clazz` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dept_id`       BIGINT       NOT NULL                COMMENT '所属院系（冗余）',
  `major_id`      BIGINT       NOT NULL                COMMENT '所属专业',
  `clazz_name`    VARCHAR(100) NOT NULL                COMMENT '班级名称 计科2301',
  `grade`         VARCHAR(10)  NOT NULL                COMMENT '年级 2023',
  `counselor_id`  BIGINT       DEFAULT NULL            COMMENT '辅导员 user_id',
  `student_count` INT          NOT NULL DEFAULT 0      COMMENT '在册人数',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0已毕业 1在读',
  `create_by`     VARCHAR(64)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_clazz_name` (`clazz_name`, `is_deleted`),
  KEY `idx_major`     (`major_id`),
  KEY `idx_dept`      (`dept_id`),
  KEY `idx_counselor` (`counselor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级表';

-- ---------------------------------------------------------------------
-- 9. semester 学期学年表
-- ---------------------------------------------------------------------
CREATE TABLE `semester` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `academic_year` VARCHAR(9)  NOT NULL                COMMENT '学年 2025-2026',
  `term`          TINYINT     NOT NULL                COMMENT '学期 1第一 2第二 3小学期',
  `semester_name` VARCHAR(50) NOT NULL                COMMENT '学期名称 2025-2026-1',
  `start_date`    DATE        NOT NULL                COMMENT '开始日期',
  `end_date`      DATE        NOT NULL                COMMENT '结束日期',
  `select_start`  DATETIME    DEFAULT NULL            COMMENT '选课开始时间',
  `select_end`    DATETIME    DEFAULT NULL            COMMENT '选课结束时间',
  `is_current`    TINYINT     NOT NULL DEFAULT 0      COMMENT '是否当前学期 0否 1是',
  `status`        TINYINT     NOT NULL DEFAULT 1      COMMENT '状态 0未开始 1进行中 2已结束',
  `create_by`     VARCHAR(64) DEFAULT NULL,
  `create_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64) DEFAULT NULL,
  `update_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semester_name` (`semester_name`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学期学年表';

-- ---------------------------------------------------------------------
-- 10. student 学生表
-- ---------------------------------------------------------------------
CREATE TABLE `student` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`           BIGINT       NOT NULL                COMMENT '关联 sys_user.id',
  `stu_no`            VARCHAR(30)  NOT NULL                COMMENT '学号',
  `dept_id`           BIGINT       DEFAULT NULL            COMMENT '院系（冗余）',
  `major_id`          BIGINT       DEFAULT NULL            COMMENT '专业（冗余）',
  `clazz_id`          BIGINT       NOT NULL                COMMENT '班级ID',
  `gender`            TINYINT      DEFAULT NULL            COMMENT '性别 1男 2女',
  `birth_date`        DATE         DEFAULT NULL            COMMENT '出生日期',
  `id_card`           VARCHAR(32)  DEFAULT NULL            COMMENT '身份证号',
  `enrollment_date`   DATE         DEFAULT NULL            COMMENT '入学日期',
  `political_status`  VARCHAR(20)  DEFAULT NULL            COMMENT '政治面貌',
  `nationality`       VARCHAR(20)  DEFAULT '汉族'          COMMENT '民族',
  `home_address`      VARCHAR(255) DEFAULT NULL            COMMENT '家庭住址',
  `emergency_contact` VARCHAR(50)  DEFAULT NULL            COMMENT '紧急联系人',
  `emergency_phone`   VARCHAR(20)  DEFAULT NULL            COMMENT '紧急联系电话',
  `photo_url`         VARCHAR(255) DEFAULT NULL            COMMENT '证件照 URL',
  `is_in_school`      TINYINT      NOT NULL DEFAULT 1      COMMENT '是否在校 1在读 2休学 3退学 4毕业',
  `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_by`         VARCHAR(64)  DEFAULT NULL,
  `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`         VARCHAR(64)  DEFAULT NULL,
  `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`        TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_no` (`stu_no`, `is_deleted`),
  UNIQUE KEY `uk_stu_user` (`user_id`, `is_deleted`),
  KEY `idx_clazz` (`clazz_id`),
  KEY `idx_dept`  (`dept_id`),
  KEY `idx_major` (`major_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生表';

-- ---------------------------------------------------------------------
-- 11. teacher 教师表
-- ---------------------------------------------------------------------
CREATE TABLE `teacher` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL                COMMENT '关联 sys_user.id',
  `tea_no`      VARCHAR(30)  NOT NULL                COMMENT '工号',
  `dept_id`     BIGINT       NOT NULL                COMMENT '所属院系',
  `title`       VARCHAR(30)  DEFAULT NULL            COMMENT '职称 教授/副教授/讲师/助教',
  `gender`      TINYINT      DEFAULT NULL            COMMENT '性别 1男 2女',
  `entry_date`  DATE         DEFAULT NULL            COMMENT '入职日期',
  `is_counselor` TINYINT     NOT NULL DEFAULT 0      COMMENT '是否兼任辅导员',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0离职 1在职',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tea_no`   (`tea_no`, `is_deleted`),
  UNIQUE KEY `uk_tea_user` (`user_id`, `is_deleted`),
  KEY `idx_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师表';

-- ---------------------------------------------------------------------
-- 12. sys_dict_type 字典类型表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_dict_type` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_name`   VARCHAR(100) NOT NULL                COMMENT '字典名称',
  `dict_type`   VARCHAR(100) NOT NULL                COMMENT '字典类型编码',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1正常',
  `remark`      VARCHAR(500) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- ---------------------------------------------------------------------
-- 13. sys_dict_data 字典数据表
-- ---------------------------------------------------------------------
CREATE TABLE `sys_dict_data` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type`   VARCHAR(100) NOT NULL                COMMENT '字典类型编码',
  `dict_label`  VARCHAR(100) NOT NULL                COMMENT '字典标签',
  `dict_value`  VARCHAR(100) NOT NULL                COMMENT '字典值',
  `dict_class`  VARCHAR(50)  DEFAULT NULL            COMMENT '样式类 primary/success/danger',
  `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0禁用 1正常',
  `remark`      VARCHAR(500) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- ---------------------------------------------------------------------
-- 14. sys_oper_log 操作日志
-- ---------------------------------------------------------------------
CREATE TABLE `sys_oper_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(50)  DEFAULT NULL COMMENT '操作人',
  `oper_module`   VARCHAR(100) DEFAULT NULL COMMENT '模块',
  `oper_type`     VARCHAR(20)  DEFAULT NULL COMMENT '操作类型 INSERT/UPDATE/DELETE/EXPORT/APPROVE',
  `method`        VARCHAR(255) DEFAULT NULL COMMENT '方法名',
  `request_uri`   VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
  `ip`            VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
  `oper_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `request_param` MEDIUMTEXT   DEFAULT NULL COMMENT '请求参数',
  `result`        MEDIUMTEXT   DEFAULT NULL COMMENT '返回结果',
  `cost_time`     BIGINT       DEFAULT NULL COMMENT '耗时(ms)',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0失败 1成功',
  `error_msg`     TEXT         DEFAULT NULL COMMENT '异常信息',
  `create_by`     VARCHAR(64)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_time`     (`oper_time`),
  KEY `idx_module`   (`oper_module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- ---------------------------------------------------------------------
-- 15. sys_login_log 登录日志
-- ---------------------------------------------------------------------
CREATE TABLE `sys_login_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  DEFAULT NULL COMMENT '登录账号',
  `ip`          VARCHAR(64)  DEFAULT NULL COMMENT '登录IP',
  `location`    VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  `browser`     VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
  `os`          VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
  `login_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0失败 1成功',
  `msg`         VARCHAR(255) DEFAULT NULL COMMENT '提示信息',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_time`     (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- ---------------------------------------------------------------------
-- 16. sys_file 文件表（MinIO 元数据）
-- ---------------------------------------------------------------------
CREATE TABLE `sys_file` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_name`     VARCHAR(255) NOT NULL COMMENT '存储文件名',
  `original_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  `object_name`   VARCHAR(500) NOT NULL COMMENT '对象路径 avatar/xxx.jpg',
  `bucket`        VARCHAR(100) NOT NULL DEFAULT 'campus' COMMENT '桶名',
  `file_type`     VARCHAR(50)  DEFAULT NULL COMMENT 'MIME',
  `file_size`     BIGINT       DEFAULT NULL COMMENT '字节数',
  `url`           VARCHAR(500) DEFAULT NULL COMMENT '访问地址',
  `biz_type`      VARCHAR(50)  DEFAULT NULL COMMENT '业务类型 avatar/apply/repair/notice',
  `biz_id`        BIGINT       DEFAULT NULL COMMENT '业务ID',
  `upload_by`     BIGINT       DEFAULT NULL COMMENT '上传人 user_id',
  `create_by`     VARCHAR(64)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_biz`    (`biz_type`, `biz_id`),
  KEY `idx_upload` (`upload_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';

-- ---------------------------------------------------------------------
-- 17. sys_message 站内消息/待办提醒
-- ---------------------------------------------------------------------
CREATE TABLE `sys_message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receiver_id` BIGINT       NOT NULL COMMENT '接收人 user_id',
  `title`       VARCHAR(200) NOT NULL COMMENT '标题',
  `content`     VARCHAR(1000) DEFAULT NULL COMMENT '内容',
  `msg_type`    TINYINT      NOT NULL DEFAULT 1 COMMENT '1系统 2待办 3审批结果 4公告',
  `biz_type`    VARCHAR(50)  DEFAULT NULL COMMENT '业务类型 leave/aid/repair',
  `biz_id`      BIGINT       DEFAULT NULL COMMENT '业务ID',
  `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0否 1是',
  `read_time`   DATETIME     DEFAULT NULL COMMENT '阅读时间',
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内消息表';

-- =====================================================================
-- 二、教务管理模块
-- =====================================================================

-- ---------------------------------------------------------------------
-- 18. course 课程表
-- ---------------------------------------------------------------------
CREATE TABLE `course` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_code` VARCHAR(50)  NOT NULL COMMENT '课程编号',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `dept_id`     BIGINT       DEFAULT NULL COMMENT '开课院系',
  `credit`      DECIMAL(4,1) NOT NULL DEFAULT 0.0 COMMENT '学分',
  `class_hour`  INT          NOT NULL DEFAULT 0 COMMENT '学时',
  `course_type` TINYINT      NOT NULL DEFAULT 1 COMMENT '1必修 2选修 3公共课',
  `exam_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '1考试 2考查',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0停开 1正常',
  `remark`      VARCHAR(500) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_code` (`course_code`, `is_deleted`),
  KEY `idx_dept` (`dept_id`),
  KEY `idx_name` (`course_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表';

-- ---------------------------------------------------------------------
-- 19. course_plan 培养方案（专业-课程绑定）
-- ---------------------------------------------------------------------
CREATE TABLE `course_plan` (
  `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `major_id`       BIGINT   NOT NULL COMMENT '专业ID',
  `grade`          VARCHAR(10) NOT NULL COMMENT '适用年级',
  `course_id`      BIGINT   NOT NULL COMMENT '课程ID',
  `course_type`    TINYINT  NOT NULL DEFAULT 1 COMMENT '1必修 2选修',
  `semester_index` TINYINT  NOT NULL DEFAULT 1 COMMENT '第几学期开课',
  `status`         TINYINT  NOT NULL DEFAULT 1 COMMENT '状态 0停用 1正常',
  `create_by`      VARCHAR(64)  DEFAULT NULL,
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`      VARCHAR(64)  DEFAULT NULL,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan` (`major_id`, `grade`, `course_id`, `is_deleted`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培养方案表';

-- ---------------------------------------------------------------------
-- 20. classroom 教室表
-- ---------------------------------------------------------------------
CREATE TABLE `classroom` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `classroom_no` VARCHAR(50) NOT NULL COMMENT '教室编号 A101',
  `building`     VARCHAR(50) DEFAULT NULL COMMENT '教学楼',
  `floor`        INT         DEFAULT NULL COMMENT '楼层',
  `capacity`     INT         NOT NULL DEFAULT 0 COMMENT '容量',
  `room_type`    TINYINT     NOT NULL DEFAULT 1 COMMENT '1普通教室 2机房 3实验室 4会议室',
  `status`       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 0停用 1可用',
  `create_by`    VARCHAR(64) DEFAULT NULL,
  `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64) DEFAULT NULL,
  `update_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`   TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_no` (`classroom_no`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教室表';

-- ---------------------------------------------------------------------
-- 21. schedule 排课表
-- ---------------------------------------------------------------------
CREATE TABLE `schedule` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `semester_id`   BIGINT      NOT NULL COMMENT '学期ID',
  `course_id`     BIGINT      NOT NULL COMMENT '课程ID',
  `teacher_id`    BIGINT      NOT NULL COMMENT '教师ID',
  `clazz_id`      BIGINT      NOT NULL COMMENT '班级ID',
  `classroom_id`  BIGINT      DEFAULT NULL COMMENT '教室ID',
  `classroom`     VARCHAR(50) DEFAULT NULL COMMENT '教室名称（冗余）',
  `week_start`    TINYINT     NOT NULL DEFAULT 1 COMMENT '起始周',
  `week_end`      TINYINT     NOT NULL DEFAULT 16 COMMENT '结束周',
  `day_of_week`   TINYINT     NOT NULL COMMENT '星期几 1-7',
  `section_start` TINYINT     NOT NULL COMMENT '开始节次',
  `section_end`   TINYINT     NOT NULL COMMENT '结束节次',
  `max_num`       INT         NOT NULL DEFAULT 60 COMMENT '选课人数上限',
  `current_num`   INT         NOT NULL DEFAULT 0 COMMENT '已选人数',
  `status`        TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 0停排 1正常',
  `create_by`     VARCHAR(64) DEFAULT NULL,
  `create_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64) DEFAULT NULL,
  `update_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_semester` (`semester_id`),
  KEY `idx_course`   (`course_id`),
  KEY `idx_teacher`  (`teacher_id`),
  KEY `idx_clazz`    (`clazz_id`),
  KEY `idx_slot`     (`semester_id`, `day_of_week`, `section_start`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排课表';

-- ---------------------------------------------------------------------
-- 22. elect_course 选课记录表
-- ---------------------------------------------------------------------
CREATE TABLE `elect_course` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  BIGINT   NOT NULL COMMENT '学生ID',
  `schedule_id` BIGINT   NOT NULL COMMENT '排课ID',
  `select_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '1已选 2已退',
  `create_by`   VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_sch` (`student_id`, `schedule_id`),
  KEY `idx_schedule` (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='选课记录表';

-- ---------------------------------------------------------------------
-- 23. score 成绩表
-- ---------------------------------------------------------------------
CREATE TABLE `score` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  BIGINT       NOT NULL COMMENT '学生ID',
  `schedule_id` BIGINT       NOT NULL COMMENT '排课ID',
  `course_id`   BIGINT       DEFAULT NULL COMMENT '课程ID（冗余）',
  `semester_id` BIGINT       DEFAULT NULL COMMENT '学期ID（冗余）',
  `exam_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '1期末 2补考 3重修 4清考',
  `score`       DECIMAL(5,2) DEFAULT NULL COMMENT '百分制成绩',
  `grade_point` DECIMAL(3,2) DEFAULT NULL COMMENT '绩点',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待录入 1已录入 2已提交 3复核中 4已锁定',
  `remark`      VARCHAR(500) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT NULL,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_sch_type` (`student_id`, `schedule_id`, `exam_type`, `is_deleted`),
  KEY `idx_schedule` (`schedule_id`),
  KEY `idx_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩表';

-- ---------------------------------------------------------------------
-- 24. exam 考试安排表
-- ---------------------------------------------------------------------
CREATE TABLE `exam` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `semester_id`  BIGINT      NOT NULL COMMENT '学期ID',
  `course_id`    BIGINT      NOT NULL COMMENT '课程ID',
  `schedule_id`  BIGINT      DEFAULT NULL COMMENT '排课ID',
  `exam_name`    VARCHAR(100) NOT NULL COMMENT '考试名称',
  `exam_type`    TINYINT     NOT NULL DEFAULT 1 COMMENT '1期末 2补考 3重修',
  `exam_date`    DATE        NOT NULL COMMENT '考试日期',
  `start_time`   TIME        NOT NULL COMMENT '开始时间',
  `end_time`     TIME        NOT NULL COMMENT '结束时间',
  `room`         VARCHAR(50) DEFAULT NULL COMMENT '考场',
  `seat_count`   INT         DEFAULT NULL COMMENT '座位数',
  `status`       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 0取消 1正常 2已结束',
  `create_by`    VARCHAR(64) DEFAULT NULL,
  `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64) DEFAULT NULL,
  `update_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`   TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_semester` (`semester_id`),
  KEY `idx_date`     (`exam_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试安排表';

-- ---------------------------------------------------------------------
-- 25. exam_invigilator 监考安排表
-- ---------------------------------------------------------------------
CREATE TABLE `exam_invigilator` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id`     BIGINT   NOT NULL COMMENT '考试ID',
  `teacher_id`  BIGINT   NOT NULL COMMENT '监考教师ID',
  `invig_role`  TINYINT  NOT NULL DEFAULT 1 COMMENT '1主监考 2副监考',
  `create_by`   VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_teacher` (`exam_id`, `teacher_id`, `is_deleted`),
  KEY `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='监考安排表';

-- ---------------------------------------------------------------------
-- 26. evaluation 教学评教表
-- ---------------------------------------------------------------------
CREATE TABLE `evaluation` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `semester_id`   BIGINT       NOT NULL COMMENT '学期ID',
  `student_id`    BIGINT       NOT NULL COMMENT '学生ID',
  `schedule_id`   BIGINT       NOT NULL COMMENT '排课ID',
  `teacher_id`    BIGINT       NOT NULL COMMENT '教师ID',
  `teach_score`   TINYINT      DEFAULT NULL COMMENT '教学水平 0-20',
  `content_score` TINYINT      DEFAULT NULL COMMENT '内容充实 0-20',
  `attitude_score` TINYINT     DEFAULT NULL COMMENT '教学态度 0-20',
  `total_score`   DECIMAL(5,2) NOT NULL COMMENT '总分(百分制)',
  `comment`       VARCHAR(500) DEFAULT NULL COMMENT '评语(匿名)',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1有效 0无效',
  `create_by`     VARCHAR(64)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT NULL,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_sch_eval` (`student_id`, `schedule_id`, `is_deleted`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教学评教表';

-- ---------------------------------------------------------------------
-- 27. adjust_apply 调课申请表
-- ---------------------------------------------------------------------
CREATE TABLE `adjust_apply` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no`         VARCHAR(32)  NOT NULL COMMENT '申请单号',
  `teacher_id`       BIGINT       NOT NULL COMMENT '申请教师ID',
  `schedule_id`      BIGINT       NOT NULL COMMENT '原排课ID',
  `old_day_of_week`  TINYINT      NOT NULL COMMENT '原星期',
  `old_section_start` TINYINT     NOT NULL COMMENT '原开始节次',
  `old_section_end`  TINYINT      NOT NULL COMMENT '原结束节次',
  `old_week_start`   TINYINT      DEFAULT NULL COMMENT '原起始周',
  `old_week_end`     TINYINT      DEFAULT NULL COMMENT '原结束周',
  `new_day_of_week`  TINYINT      NOT NULL COMMENT '新星期',
  `new_section_start` TINYINT     NOT NULL COMMENT '新开始节次',
  `new_section_end`  TINYINT      NOT NULL COMMENT '新结束节次',
  `new_week_start`   TINYINT      DEFAULT NULL COMMENT '新起始周',
  `new_week_end`     TINYINT      DEFAULT NULL COMMENT '新结束周',
  `new_classroom_id` BIGINT       DEFAULT NULL COMMENT '新教室ID',
  `reason`           VARCHAR(500) NOT NULL COMMENT '调课原因',
  `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1审批中 2通过 3驳回',
  `current_approver` BIGINT       DEFAULT NULL COMMENT '当前审批人',
  `approve_opinion`  VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `create_by`        VARCHAR(64)  DEFAULT NULL,
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(64)  DEFAULT NULL,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调课申请表';

-- =====================================================================
-- 三、学工管理模块
-- =====================================================================

-- ---------------------------------------------------------------------
-- 28. leave_apply 请假申请表
-- ---------------------------------------------------------------------
CREATE TABLE `leave_apply` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no`         VARCHAR(32)   NOT NULL COMMENT '申请单号',
  `student_id`       BIGINT        NOT NULL COMMENT '学生ID',
  `clazz_id`         BIGINT        NOT NULL COMMENT '班级ID（冗余，数据权限用）',
  `leave_type`       TINYINT       NOT NULL DEFAULT 1 COMMENT '1事假 2病假 3其他',
  `start_time`       DATETIME      NOT NULL COMMENT '请假开始时间',
  `end_time`         DATETIME      NOT NULL COMMENT '请假结束时间',
  `days`             DECIMAL(4,1)  NOT NULL COMMENT '请假天数',
  `reason`           VARCHAR(1000) NOT NULL COMMENT '请假事由',
  `file_url`         VARCHAR(1000) DEFAULT NULL COMMENT '附件URL（多个逗号分隔）',
  `status`           TINYINT       NOT NULL DEFAULT 0 COMMENT '0草稿 1辅导员审批中 2学工审批中 3通过 4驳回 5已销假',
  `current_approver` BIGINT        DEFAULT NULL COMMENT '当前审批人 user_id',
  `approve_opinion`  VARCHAR(500)  DEFAULT NULL COMMENT '审批意见',
  `approve_time`     DATETIME      DEFAULT NULL COMMENT '审批完成时间',
  `create_by`        VARCHAR(64)   DEFAULT NULL,
  `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(64)   DEFAULT NULL,
  `update_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_student`  (`student_id`),
  KEY `idx_clazz`    (`clazz_id`),
  KEY `idx_approver` (`current_approver`, `status`),
  KEY `idx_status`   (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='请假申请表';

-- ---------------------------------------------------------------------
-- 29. aid_apply 助学金申请表
-- ---------------------------------------------------------------------
CREATE TABLE `aid_apply` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no`         VARCHAR(32)   NOT NULL COMMENT '申请单号',
  `student_id`       BIGINT        NOT NULL COMMENT '学生ID',
  `clazz_id`         BIGINT        NOT NULL COMMENT '班级ID（冗余）',
  `aid_type`         TINYINT       NOT NULL DEFAULT 1 COMMENT '1国家助学金 2校内助学金 3临时困难补助 4助学贷款',
  `aid_level`        TINYINT       DEFAULT NULL COMMENT '困难等级 1特别困难 2困难 3一般困难',
  `amount`           DECIMAL(10,2) DEFAULT NULL COMMENT '申请金额',
  `reason`           VARCHAR(1000) NOT NULL COMMENT '申请理由',
  `file_url`         VARCHAR(1000) DEFAULT NULL COMMENT '证明材料URL',
  `status`           TINYINT       NOT NULL DEFAULT 0 COMMENT '0草稿 1辅导员初审 2院系复审 3校级终审 4通过 5驳回',
  `current_approver` BIGINT        DEFAULT NULL COMMENT '当前审批人',
  `approve_opinion`  VARCHAR(500)  DEFAULT NULL COMMENT '审批意见',
  `public_status`    TINYINT       NOT NULL DEFAULT 0 COMMENT '公示状态 0未公示 1公示中 2已公示',
  `approve_time`     DATETIME      DEFAULT NULL COMMENT '审批完成时间',
  `create_by`        VARCHAR(64)   DEFAULT NULL,
  `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(64)   DEFAULT NULL,
  `update_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_student`  (`student_id`),
  KEY `idx_clazz`    (`clazz_id`),
  KEY `idx_approver` (`current_approver`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='助学金申请表';

-- ---------------------------------------------------------------------
-- 30. approval_record 审批记录表（通用，每个审批节点一条）
-- ---------------------------------------------------------------------
CREATE TABLE `approval_record` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type`       VARCHAR(50)  NOT NULL COMMENT '业务类型 leave/aid/adjust/repair/dorm_change/booking',
  `biz_id`         BIGINT       NOT NULL COMMENT '业务ID',
  `node`           TINYINT      NOT NULL COMMENT '节点序号 1..n',
  `node_name`      VARCHAR(50)  NOT NULL COMMENT '节点名称 辅导员审批',
  `approver_id`    BIGINT       NOT NULL COMMENT '审批人 user_id',
  `approver_name`  VARCHAR(50)  DEFAULT NULL COMMENT '审批人姓名',
  `approve_result` TINYINT      NOT NULL COMMENT '1同意 2驳回',
  `opinion`        VARCHAR(500) DEFAULT NULL COMMENT '意见',
  `approve_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `create_by`      VARCHAR(64)  DEFAULT NULL,
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`      VARCHAR(64)  DEFAULT NULL,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='审批记录表';

-- ---------------------------------------------------------------------
-- 31. award_punish 奖惩记录表
-- ---------------------------------------------------------------------
CREATE TABLE `award_punish` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`  BIGINT        NOT NULL COMMENT '学生ID',
  `type`        TINYINT       NOT NULL DEFAULT 1 COMMENT '1奖励 2处分',
  `level`       VARCHAR(30)   DEFAULT NULL COMMENT '级别 国家级/省级/校级/院级',
  `title`       VARCHAR(200)  NOT NULL COMMENT '奖惩名称',
  `content`     VARCHAR(1000) DEFAULT NULL COMMENT '详细内容',
  `occur_date`  DATE          DEFAULT NULL COMMENT '发生日期',
  `file_url`    VARCHAR(1000) DEFAULT NULL COMMENT '证明材料',
  `recorder_id` BIGINT        DEFAULT NULL COMMENT '录入人',
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '0撤销 1有效',
  `create_by`   VARCHAR(64)   DEFAULT NULL,
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)   DEFAULT NULL,
  `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_type`    (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='奖惩记录表';

-- =====================================================================
-- 四、后勤宿舍模块
-- =====================================================================

-- ---------------------------------------------------------------------
-- 32. dorm_building 宿舍楼表
-- ---------------------------------------------------------------------
CREATE TABLE `dorm_building` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `building_no`   VARCHAR(20) NOT NULL COMMENT '楼号 1号楼',
  `building_name` VARCHAR(50) DEFAULT NULL COMMENT '楼名',
  `gender`        TINYINT     NOT NULL DEFAULT 1 COMMENT '1男生 2女生',
  `floor_count`   INT         NOT NULL DEFAULT 6 COMMENT '层数',
  `manager_id`    BIGINT      DEFAULT NULL COMMENT '宿管员 user_id',
  `status`        TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 0停用 1可用',
  `create_by`     VARCHAR(64) DEFAULT NULL,
  `create_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64) DEFAULT NULL,
  `update_time`   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_no` (`building_no`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍楼表';

-- ---------------------------------------------------------------------
-- 33. dorm_room 宿舍房间表
-- ---------------------------------------------------------------------
CREATE TABLE `dorm_room` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `building_id` BIGINT      NOT NULL COMMENT '宿舍楼ID',
  `room_no`     VARCHAR(20) NOT NULL COMMENT '房间号 101',
  `floor`       INT         NOT NULL DEFAULT 1 COMMENT '楼层',
  `bed_count`   INT         NOT NULL DEFAULT 4 COMMENT '床位数',
  `used_count`  INT         NOT NULL DEFAULT 0 COMMENT '已住人数',
  `room_status` TINYINT     NOT NULL DEFAULT 1 COMMENT '1空闲 2满员 3维修中',
  `create_by`   VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room` (`building_id`, `room_no`, `is_deleted`),
  KEY `idx_building` (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍房间表';

-- ---------------------------------------------------------------------
-- 34. dorm_assignment 宿舍分配表
-- ---------------------------------------------------------------------
CREATE TABLE `dorm_assignment` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id`    BIGINT   NOT NULL COMMENT '学生ID',
  `room_id`       BIGINT   NOT NULL COMMENT '房间ID',
  `bed_no`        VARCHAR(10) DEFAULT NULL COMMENT '床位号',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '入住时间',
  `check_out_time` DATETIME DEFAULT NULL COMMENT '退宿时间',
  `status`        TINYINT  NOT NULL DEFAULT 1 COMMENT '1在住 2已退宿',
  `create_by`     VARCHAR(64) DEFAULT NULL,
  `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64) DEFAULT NULL,
  `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`    TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stu_room_bed` (`student_id`, `room_id`, `bed_no`, `is_deleted`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍分配表';

-- ---------------------------------------------------------------------
-- 35. dorm_change 宿舍调换申请表
-- ---------------------------------------------------------------------
CREATE TABLE `dorm_change` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no`         VARCHAR(32)  NOT NULL COMMENT '申请单号',
  `student_id`       BIGINT       NOT NULL COMMENT '学生ID',
  `from_room_id`     BIGINT       NOT NULL COMMENT '原房间',
  `to_room_id`       BIGINT       DEFAULT NULL COMMENT '目标房间',
  `reason`           VARCHAR(500) NOT NULL COMMENT '调换原因',
  `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1审批中 2通过 3驳回',
  `current_approver` BIGINT       DEFAULT NULL COMMENT '当前审批人',
  `approve_opinion`  VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `create_by`        VARCHAR(64)  DEFAULT NULL,
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(64)  DEFAULT NULL,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍调换申请表';

-- ---------------------------------------------------------------------
-- 36. repair_order 报修工单表
-- ---------------------------------------------------------------------
CREATE TABLE `repair_order` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`       VARCHAR(32)   NOT NULL COMMENT '工单号',
  `student_id`     BIGINT        NOT NULL COMMENT '报修学生ID',
  `room_id`        BIGINT        DEFAULT NULL COMMENT '宿舍房间ID',
  `address`        VARCHAR(255)  NOT NULL COMMENT '报修地址',
  `repair_type`    TINYINT       NOT NULL DEFAULT 1 COMMENT '1水电 2门窗 3家具 4网络 5其他',
  `content`        VARCHAR(1000) NOT NULL COMMENT '报修内容',
  `file_url`       VARCHAR(1000) DEFAULT NULL COMMENT '图片附件',
  `status`         TINYINT       NOT NULL DEFAULT 0 COMMENT '0待派单 1维修中 2已完成 3取消',
  `handler_id`     BIGINT        DEFAULT NULL COMMENT '维修人员ID',
  `dispatch_time`  DATETIME      DEFAULT NULL COMMENT '派单时间',
  `finish_time`    DATETIME      DEFAULT NULL COMMENT '完成时间',
  `finish_pic`     VARCHAR(1000) DEFAULT NULL COMMENT '维修后照片',
  `student_confirm` TINYINT      NOT NULL DEFAULT 0 COMMENT '学生确认 0未确认 1已确认',
  `score`          TINYINT       DEFAULT NULL COMMENT '满意度 1-5',
  `feedback`       VARCHAR(500)  DEFAULT NULL COMMENT '反馈',
  `create_by`      VARCHAR(64)   DEFAULT NULL,
  `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_by`      VARCHAR(64)   DEFAULT NULL,
  `update_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`     TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_student` (`student_id`),
  KEY `idx_handler` (`handler_id`),
  KEY `idx_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报修工单表';

-- ---------------------------------------------------------------------
-- 37. room_booking 场地预约表
-- ---------------------------------------------------------------------
CREATE TABLE `room_booking` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `apply_no`         VARCHAR(32)  NOT NULL COMMENT '预约单号',
  `classroom_id`     BIGINT       NOT NULL COMMENT '场地ID',
  `applicant_id`     BIGINT       NOT NULL COMMENT '申请人 user_id',
  `applicant_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '1教师 2学生 3部门',
  `booking_date`     DATE         NOT NULL COMMENT '预约日期',
  `start_time`       TIME         NOT NULL COMMENT '开始时间',
  `end_time`         TIME         NOT NULL COMMENT '结束时间',
  `purpose`          VARCHAR(500) NOT NULL COMMENT '用途',
  `people_count`     INT          DEFAULT NULL COMMENT '人数',
  `status`           TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1审批中 2通过 3驳回 4已取消',
  `current_approver` BIGINT       DEFAULT NULL COMMENT '当前审批人',
  `approve_opinion`  VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
  `create_by`        VARCHAR(64)  DEFAULT NULL,
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(64)  DEFAULT NULL,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_room_date` (`classroom_id`, `booking_date`),
  KEY `idx_applicant` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='场地预约表';

-- =====================================================================
-- 五、通知公告模块
-- =====================================================================

-- ---------------------------------------------------------------------
-- 38. notice 通知公告表
-- ---------------------------------------------------------------------
CREATE TABLE `notice` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`            VARCHAR(200) NOT NULL COMMENT '标题',
  `content`          MEDIUMTEXT   NOT NULL COMMENT '正文(富文本)',
  `notice_type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1通知 2公告 3制度',
  `receive_scope`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0全校 1院系 2班级 3指定人',
  `receive_dept_id`  BIGINT       DEFAULT NULL COMMENT '接收院系ID',
  `receive_clazz_id` BIGINT       DEFAULT NULL COMMENT '接收班级ID',
  `attachment`       VARCHAR(1000) DEFAULT NULL COMMENT '附件URL',
  `is_top`           TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `publish_by`       BIGINT       DEFAULT NULL COMMENT '发布人 user_id',
  `publish_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已撤回',
  `read_count`       INT          NOT NULL DEFAULT 0 COMMENT '阅读数',
  `create_by`        VARCHAR(64)  DEFAULT NULL,
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`        VARCHAR(64)  DEFAULT NULL,
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_scope` (`receive_scope`, `status`),
  KEY `idx_time`  (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知公告表';

-- ---------------------------------------------------------------------
-- 39. notice_read 公告已读记录表
-- ---------------------------------------------------------------------
CREATE TABLE `notice_read` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `notice_id`   BIGINT   NOT NULL COMMENT '公告ID',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `read_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  `create_by`   VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`  TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告已读记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 完成：共 39 张表
-- =====================================================================
