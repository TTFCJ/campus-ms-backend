# 数据库表结构

> 由 `information_schema` 自动提取，共 46 张表。

字符集 `utf8mb4`　引擎 `InnoDB`　逻辑删除字段 `is_deleted`

公共字段（所有业务表都有）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `create_by` | varchar(64) | 创建人 |
| `create_time` | datetime | 创建时间 |
| `update_by` | varchar(64) | 更新人 |
| `update_time` | datetime | 更新时间 |
| `is_deleted` | tinyint | 0 未删 / 1 已删 |

---

## adjust_apply

**调课申请表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `apply_no` | varchar(32) | 否 | UNI |  | 申请单号 |
| `teacher_id` | bigint | 否 | MUL |  | 申请教师ID |
| `schedule_id` | bigint | 否 |  |  | 原排课ID |
| `old_day_of_week` | tinyint | 否 |  |  | 原星期 |
| `old_section_start` | tinyint | 否 |  |  | 原开始节次 |
| `old_section_end` | tinyint | 否 |  |  | 原结束节次 |
| `old_week_start` | tinyint | 是 |  |  | 原起始周 |
| `old_week_end` | tinyint | 是 |  |  | 原结束周 |
| `new_day_of_week` | tinyint | 否 |  |  | 新星期 |
| `new_section_start` | tinyint | 否 |  |  | 新开始节次 |
| `new_section_end` | tinyint | 否 |  |  | 新结束节次 |
| `new_week_start` | tinyint | 是 |  |  | 新起始周 |
| `new_week_end` | tinyint | 是 |  |  | 新结束周 |
| `new_classroom_id` | bigint | 是 |  |  | 新教室ID |
| `reason` | varchar(500) | 否 |  |  | 调课原因 |
| `status` | tinyint | 否 | MUL | `1` | 0草稿 1审批中 2通过 3驳回 |
| `current_approver` | bigint | 是 |  |  | 当前审批人 |
| `approve_opinion` | varchar(500) | 是 |  |  | 审批意见 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## aid_apply

**助学金申请表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `apply_no` | varchar(32) | 否 | UNI |  | 申请单号 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `clazz_id` | bigint | 否 | MUL |  | 班级ID（冗余） |
| `aid_type` | tinyint | 否 |  | `1` | 1国家助学金 2校内助学金 3临时困难补助 4助学贷款 |
| `aid_level` | tinyint | 是 |  |  | 困难等级 1特别困难 2困难 3一般困难 |
| `amount` | decimal(10,2) | 是 |  |  | 申请金额 |
| `reason` | varchar(1000) | 否 |  |  | 申请理由 |
| `file_url` | varchar(1000) | 是 |  |  | 证明材料URL |
| `status` | tinyint | 否 |  | `0` | 0草稿 1辅导员初审 2院系复审 3校级终审 4通过 5驳回 |
| `current_approver` | bigint | 是 | MUL |  | 当前审批人 |
| `approve_opinion` | varchar(500) | 是 |  |  | 审批意见 |
| `public_status` | tinyint | 否 |  | `0` | 公示状态 0未公示 1公示中 2已公示 |
| `approve_time` | datetime | 是 |  |  | 审批完成时间 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## approval_record

**审批记录表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `biz_type` | varchar(50) | 否 | MUL |  | 业务类型 leave/aid/adjust/repair/dorm_change/booking |
| `biz_id` | bigint | 否 |  |  | 业务ID |
| `node` | tinyint | 否 |  |  | 节点序号 1..n |
| `node_name` | varchar(50) | 否 |  |  | 节点名称 辅导员审批 |
| `approver_id` | bigint | 否 |  |  | 审批人 user_id |
| `approver_name` | varchar(50) | 是 |  |  | 审批人姓名 |
| `approve_result` | tinyint | 否 |  |  | 1同意 2驳回 |
| `opinion` | varchar(500) | 是 |  |  | 意见 |
| `approve_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` | 审批时间 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## attend_record

**签到记录**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `session_id` | bigint | 否 | MUL |  | 场次ID |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `checkin_time` | datetime | 是 |  |  | 签到时间 |
| `status` | tinyint | 否 |  | `1` | 1正常 2迟到 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## attend_session

**课堂签到场次**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `schedule_id` | bigint | 否 | MUL |  | 排课ID |
| `teacher_id` | bigint | 否 |  |  | 教师ID |
| `code` | varchar(8) | 否 | MUL |  | 签到码 |
| `session_date` | date | 否 | MUL |  | 上课日期 |
| `section_start` | tinyint | 是 |  |  |  |
| `section_end` | tinyint | 是 |  |  |  |
| `open_time` | datetime | 否 |  |  | 开放时间 |
| `close_time` | datetime | 否 |  |  | 截止时间 |
| `status` | tinyint | 否 |  | `1` | 1进行中 2已结束 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## award_punish

**奖惩记录表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `type` | tinyint | 否 | MUL | `1` | 1奖励 2处分 |
| `level` | varchar(30) | 是 |  |  | 级别 国家级/省级/校级/院级 |
| `title` | varchar(200) | 否 |  |  | 奖惩名称 |
| `content` | varchar(1000) | 是 |  |  | 详细内容 |
| `occur_date` | date | 是 |  |  | 发生日期 |
| `file_url` | varchar(1000) | 是 |  |  | 证明材料 |
| `recorder_id` | bigint | 是 |  |  | 录入人 |
| `status` | tinyint | 否 |  | `1` | 0撤销 1有效 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## care_record

**关爱记录**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `target_id` | bigint | 否 | MUL |  | 关爱对象ID |
| `student_id` | bigint | 否 | MUL |  | 学生ID（冗余） |
| `record_type` | tinyint | 否 | MUL | `1` | 1谈心谈话 2家访 3帮扶 4预警处置 |
| `title` | varchar(200) | 是 |  |  | 标题 |
| `content` | text | 是 |  |  | 详细内容 |
| `result` | varchar(500) | 是 |  |  | 处理结果 |
| `recorder_id` | bigint | 是 |  |  | 记录人 user_id |
| `record_time` | datetime | 否 | MUL |  | 发生时间 |
| `attachment` | varchar(1000) | 是 |  |  | 附件 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## care_target

**关爱对象**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `tags` | varchar(200) | 是 |  |  | 标签，逗号分隔：经济困难,学业预警,心理关注 |
| `risk_level` | tinyint | 否 | MUL | `1` | 风险等级 1一般 2关注 3重点 |
| `status` | tinyint | 否 |  | `1` | 状态 1在册 2已解除 |
| `reason` | varchar(500) | 是 |  |  | 纳入原因 |
| `counselor_id` | bigint | 是 | MUL |  | 负责辅导员 user_id |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## classroom

**教室表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `classroom_no` | varchar(50) | 否 | MUL |  | 教室编号 A101 |
| `building` | varchar(50) | 是 |  |  | 教学楼 |
| `floor` | int | 是 |  |  | 楼层 |
| `capacity` | int | 否 |  | `0` | 容量 |
| `room_type` | tinyint | 否 |  | `1` | 1普通教室 2机房 3实验室 4会议室 |
| `status` | tinyint | 否 |  | `1` | 状态 0停用 1可用 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## clazz

**班级表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `dept_id` | bigint | 否 | MUL |  | 所属院系（冗余） |
| `major_id` | bigint | 否 | MUL |  | 所属专业 |
| `clazz_name` | varchar(100) | 否 | MUL |  | 班级名称 计科2301 |
| `grade` | varchar(10) | 否 |  |  | 年级 2023 |
| `counselor_id` | bigint | 是 | MUL |  | 辅导员 user_id |
| `student_count` | int | 否 |  | `0` | 在册人数 |
| `status` | tinyint | 否 |  | `1` | 状态 0已毕业 1在读 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## course

**课程表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `course_code` | varchar(50) | 否 | MUL |  | 课程编号 |
| `course_name` | varchar(100) | 否 | MUL |  | 课程名称 |
| `dept_id` | bigint | 是 | MUL |  | 开课院系 |
| `credit` | decimal(4,1) | 否 |  | `0.0` | 学分 |
| `class_hour` | int | 否 |  | `0` | 学时 |
| `course_type` | tinyint | 否 |  | `1` | 1必修 2选修 3公共课 |
| `exam_type` | tinyint | 否 |  | `1` | 1考试 2考查 |
| `status` | tinyint | 否 |  | `1` | 状态 0停开 1正常 |
| `remark` | varchar(500) | 是 |  |  |  |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## course_plan

**培养方案表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `major_id` | bigint | 否 | MUL |  | 专业ID |
| `grade` | varchar(10) | 否 |  |  | 适用年级 |
| `course_id` | bigint | 否 | MUL |  | 课程ID |
| `course_type` | tinyint | 否 |  | `1` | 1必修 2选修 |
| `semester_index` | tinyint | 否 |  | `1` | 第几学期开课 |
| `status` | tinyint | 否 |  | `1` | 状态 0停用 1正常 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## dorm_assignment

**宿舍分配表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `room_id` | bigint | 否 | MUL |  | 房间ID |
| `bed_no` | varchar(10) | 是 |  |  | 床位号 |
| `check_in_time` | datetime | 是 |  |  | 入住时间 |
| `check_out_time` | datetime | 是 |  |  | 退宿时间 |
| `status` | tinyint | 否 |  | `1` | 1在住 2已退宿 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## dorm_building

**宿舍楼表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `building_no` | varchar(20) | 否 | MUL |  | 楼号 1号楼 |
| `building_name` | varchar(50) | 是 |  |  | 楼名 |
| `gender` | tinyint | 否 |  | `1` | 1男生 2女生 |
| `floor_count` | int | 否 |  | `6` | 层数 |
| `manager_id` | bigint | 是 |  |  | 宿管员 user_id |
| `status` | tinyint | 否 |  | `1` | 状态 0停用 1可用 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## dorm_change

**宿舍调换申请表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `apply_no` | varchar(32) | 否 | UNI |  | 申请单号 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `from_room_id` | bigint | 否 |  |  | 原房间 |
| `to_room_id` | bigint | 是 |  |  | 目标房间 |
| `reason` | varchar(500) | 否 |  |  | 调换原因 |
| `status` | tinyint | 否 |  | `1` | 0草稿 1审批中 2通过 3驳回 |
| `current_approver` | bigint | 是 |  |  | 当前审批人 |
| `approve_opinion` | varchar(500) | 是 |  |  | 审批意见 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## dorm_checkin

**归寝记录**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `room_id` | bigint | 是 | MUL |  | 房间ID |
| `check_date` | date | 否 | MUL |  | 查寝日期 |
| `check_time` | datetime | 是 |  |  | 打卡/确认时间 |
| `status` | tinyint | 否 | MUL | `1` | 1正常 2晚归 3未归 4请假 |
| `source` | tinyint | 否 |  | `2` | 来源 1门禁 2手动 3扫码 |
| `remark` | varchar(200) | 是 |  |  | 备注 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## dorm_room

**宿舍房间表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `building_id` | bigint | 否 | MUL |  | 宿舍楼ID |
| `room_no` | varchar(20) | 否 |  |  | 房间号 101 |
| `floor` | int | 否 |  | `1` | 楼层 |
| `bed_count` | int | 否 |  | `4` | 床位数 |
| `used_count` | int | 否 |  | `0` | 已住人数 |
| `room_status` | tinyint | 否 |  | `1` | 1空闲 2满员 3维修中 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## ec_activity

**第二课堂活动**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `title` | varchar(200) | 否 |  |  | 活动名称 |
| `category` | tinyint | 否 | MUL | `1` | 1讲座 2志愿服务 3社团 4竞赛 5社会实践 6文体 |
| `credit` | decimal(3,1) | 否 |  | `0.5` | 可获学分 |
| `start_time` | datetime | 否 | MUL |  | 开始时间 |
| `end_time` | datetime | 否 |  |  | 结束时间 |
| `location` | varchar(200) | 是 |  |  | 地点 |
| `capacity` | int | 否 |  | `100` | 人数上限 |
| `enroll_count` | int | 否 |  | `0` | 已报名人数 |
| `cover_url` | varchar(500) | 是 |  |  | 封面图 |
| `content` | text | 是 |  |  | 活动详情 |
| `publisher_id` | bigint | 是 |  |  | 发布人 user_id |
| `status` | tinyint | 否 | MUL | `0` | 0草稿 1报名中 2进行中 3已结束 4已取消 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |
| `checkin_code` | varchar(8) | 是 |  |  | 当前签到码 |
| `checkin_start` | datetime | 是 |  |  | 签到开始时间 |
| `checkin_end` | datetime | 是 |  |  | 签到截止时间 |

## ec_enroll

**活动报名**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `activity_id` | bigint | 否 | MUL |  | 活动ID |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `enroll_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` | 报名时间 |
| `checkin_time` | datetime | 是 |  |  | 签到时间 |
| `status` | tinyint | 否 | MUL | `1` | 1已报名 2已签到 3已取消 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## elect_course

**选课记录表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `schedule_id` | bigint | 否 | MUL |  | 排课ID |
| `select_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` | 选课时间 |
| `status` | tinyint | 否 |  | `1` | 1已选 2已退 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## evaluation

**教学评教表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `semester_id` | bigint | 否 | MUL |  | 学期ID |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `schedule_id` | bigint | 否 |  |  | 排课ID |
| `teacher_id` | bigint | 否 | MUL |  | 教师ID |
| `teach_score` | tinyint | 是 |  |  | 教学水平 0-20 |
| `content_score` | tinyint | 是 |  |  | 内容充实 0-20 |
| `attitude_score` | tinyint | 是 |  |  | 教学态度 0-20 |
| `total_score` | decimal(5,2) | 否 |  |  | 总分(百分制) |
| `comment` | varchar(500) | 是 |  |  | 评语(匿名) |
| `status` | tinyint | 否 |  | `1` | 1有效 0无效 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## exam

**考试安排表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `semester_id` | bigint | 否 | MUL |  | 学期ID |
| `course_id` | bigint | 否 |  |  | 课程ID |
| `schedule_id` | bigint | 是 |  |  | 排课ID |
| `exam_name` | varchar(100) | 否 |  |  | 考试名称 |
| `exam_type` | tinyint | 否 |  | `1` | 1期末 2补考 3重修 |
| `exam_date` | date | 否 | MUL |  | 考试日期 |
| `start_time` | time | 否 |  |  | 开始时间 |
| `end_time` | time | 否 |  |  | 结束时间 |
| `room` | varchar(50) | 是 |  |  | 考场 |
| `seat_count` | int | 是 |  |  | 座位数 |
| `status` | tinyint | 否 |  | `1` | 状态 0取消 1正常 2已结束 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## exam_invigilator

**监考安排表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `exam_id` | bigint | 否 | MUL |  | 考试ID |
| `teacher_id` | bigint | 否 | MUL |  | 监考教师ID |
| `invig_role` | tinyint | 否 |  | `1` | 1主监考 2副监考 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## leave_apply

**请假申请表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `apply_no` | varchar(32) | 否 | UNI |  | 申请单号 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `clazz_id` | bigint | 否 | MUL |  | 班级ID（冗余，数据权限用） |
| `leave_type` | tinyint | 否 |  | `1` | 1事假 2病假 3其他 |
| `start_time` | datetime | 否 |  |  | 请假开始时间 |
| `end_time` | datetime | 否 |  |  | 请假结束时间 |
| `days` | decimal(4,1) | 否 |  |  | 请假天数 |
| `reason` | varchar(1000) | 否 |  |  | 请假事由 |
| `file_url` | varchar(1000) | 是 |  |  | 附件URL（多个逗号分隔） |
| `status` | tinyint | 否 | MUL | `0` | 0草稿 1辅导员审批中 2学工审批中 3通过 4驳回 5已销假 |
| `current_approver` | bigint | 是 | MUL |  | 当前审批人 user_id |
| `approve_opinion` | varchar(500) | 是 |  |  | 审批意见 |
| `approve_time` | datetime | 是 |  |  | 审批完成时间 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## major

**专业表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `dept_id` | bigint | 否 | MUL |  | 所属院系 |
| `major_name` | varchar(100) | 否 |  |  | 专业名称 |
| `major_code` | varchar(50) | 否 | MUL |  | 专业代码 |
| `length_school` | tinyint | 否 |  | `4` | 学制(年) |
| `degree` | varchar(20) | 是 |  |  | 授予学位 |
| `status` | tinyint | 否 |  | `1` | 状态 0停招 1正常 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## notice

**通知公告表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `title` | varchar(200) | 否 |  |  | 标题 |
| `content` | mediumtext | 否 |  |  | 正文(富文本) |
| `notice_type` | tinyint | 否 |  | `1` | 1通知 2公告 3制度 |
| `receive_scope` | tinyint | 否 | MUL | `0` | 0全校 1院系 2班级 3指定人 |
| `receive_dept_id` | bigint | 是 |  |  | 接收院系ID |
| `receive_clazz_id` | bigint | 是 |  |  | 接收班级ID |
| `attachment` | varchar(1000) | 是 |  |  | 附件URL |
| `is_top` | tinyint | 否 |  | `0` | 是否置顶 |
| `publish_by` | bigint | 是 |  |  | 发布人 user_id |
| `publish_time` | datetime | 是 | MUL | `CURRENT_TIMESTAMP` | 发布时间 |
| `status` | tinyint | 否 |  | `0` | 0草稿 1已发布 2已撤回 |
| `read_count` | int | 否 |  | `0` | 阅读数 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## notice_read

**公告已读记录表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `notice_id` | bigint | 否 | MUL |  | 公告ID |
| `user_id` | bigint | 否 |  |  | 用户ID |
| `read_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` | 阅读时间 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## repair_order

**报修工单表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `order_no` | varchar(32) | 否 | UNI |  | 工单号 |
| `student_id` | bigint | 否 | MUL |  | 报修学生ID |
| `room_id` | bigint | 是 |  |  | 宿舍房间ID |
| `address` | varchar(255) | 否 |  |  | 报修地址 |
| `repair_type` | tinyint | 否 |  | `1` | 1水电 2门窗 3家具 4网络 5其他 |
| `content` | varchar(1000) | 否 |  |  | 报修内容 |
| `file_url` | varchar(1000) | 是 |  |  | 图片附件 |
| `status` | tinyint | 否 | MUL | `0` | 0待派单 1维修中 2已完成 3取消 |
| `handler_id` | bigint | 是 | MUL |  | 维修人员ID |
| `dispatch_time` | datetime | 是 |  |  | 派单时间 |
| `finish_time` | datetime | 是 |  |  | 完成时间 |
| `finish_pic` | varchar(1000) | 是 |  |  | 维修后照片 |
| `student_confirm` | tinyint | 否 |  | `0` | 学生确认 0未确认 1已确认 |
| `score` | tinyint | 是 |  |  | 满意度 1-5 |
| `feedback` | varchar(500) | 是 |  |  | 反馈 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## room_booking

**场地预约表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `apply_no` | varchar(32) | 否 | UNI |  | 预约单号 |
| `classroom_id` | bigint | 否 | MUL |  | 场地ID |
| `applicant_id` | bigint | 否 | MUL |  | 申请人 user_id |
| `applicant_type` | tinyint | 否 |  | `1` | 1教师 2学生 3部门 |
| `booking_date` | date | 否 |  |  | 预约日期 |
| `start_time` | time | 否 |  |  | 开始时间 |
| `end_time` | time | 否 |  |  | 结束时间 |
| `purpose` | varchar(500) | 否 |  |  | 用途 |
| `people_count` | int | 是 |  |  | 人数 |
| `status` | tinyint | 否 |  | `1` | 0草稿 1审批中 2通过 3驳回 4已取消 |
| `current_approver` | bigint | 是 |  |  | 当前审批人 |
| `approve_opinion` | varchar(500) | 是 |  |  | 审批意见 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## schedule

**排课表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `semester_id` | bigint | 否 | MUL |  | 学期ID |
| `course_id` | bigint | 否 | MUL |  | 课程ID |
| `teacher_id` | bigint | 否 | MUL |  | 教师ID |
| `clazz_id` | bigint | 否 | MUL |  | 班级ID |
| `classroom_id` | bigint | 是 | MUL |  | 教室ID |
| `classroom` | varchar(50) | 是 |  |  | 教室名称（冗余） |
| `week_start` | tinyint | 否 |  | `1` | 起始周 |
| `week_end` | tinyint | 否 |  | `16` | 结束周 |
| `day_of_week` | tinyint | 否 |  |  | 星期几 1-7 |
| `section_start` | tinyint | 否 |  |  | 开始节次 |
| `section_end` | tinyint | 否 |  |  | 结束节次 |
| `max_num` | int | 否 |  | `60` | 选课人数上限 |
| `current_num` | int | 否 |  | `0` | 已选人数 |
| `status` | tinyint | 否 |  | `1` | 状态 0停排 1正常 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## score

**成绩表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `student_id` | bigint | 否 | MUL |  | 学生ID |
| `schedule_id` | bigint | 否 | MUL |  | 排课ID |
| `course_id` | bigint | 是 | MUL |  | 课程ID（冗余） |
| `semester_id` | bigint | 是 | MUL |  | 学期ID（冗余） |
| `exam_type` | tinyint | 否 |  | `1` | 1期末 2补考 3重修 4清考 |
| `score` | decimal(5,2) | 是 |  |  | 百分制成绩 |
| `grade_point` | decimal(3,2) | 是 |  |  | 绩点 |
| `status` | tinyint | 否 |  | `0` | 0待录入 1已录入 2已提交 3复核中 4已锁定 |
| `remark` | varchar(500) | 是 |  |  |  |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## semester

**学期学年表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `academic_year` | varchar(9) | 否 |  |  | 学年 2025-2026 |
| `term` | tinyint | 否 |  |  | 学期 1第一 2第二 3小学期 |
| `semester_name` | varchar(50) | 否 | MUL |  | 学期名称 2025-2026-1 |
| `start_date` | date | 否 |  |  | 开始日期 |
| `end_date` | date | 否 |  |  | 结束日期 |
| `select_start` | datetime | 是 |  |  | 选课开始时间 |
| `select_end` | datetime | 是 |  |  | 选课结束时间 |
| `is_current` | tinyint | 否 |  | `0` | 是否当前学期 0否 1是 |
| `status` | tinyint | 否 |  | `1` | 状态 0未开始 1进行中 2已结束 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## student

**学生表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `user_id` | bigint | 否 | MUL |  | 关联 sys_user.id |
| `stu_no` | varchar(30) | 否 | MUL |  | 学号 |
| `dept_id` | bigint | 是 | MUL |  | 院系（冗余） |
| `major_id` | bigint | 是 | MUL |  | 专业（冗余） |
| `clazz_id` | bigint | 否 | MUL |  | 班级ID |
| `gender` | tinyint | 是 |  |  | 性别 1男 2女 |
| `birth_date` | date | 是 |  |  | 出生日期 |
| `id_card` | varchar(32) | 是 |  |  | 身份证号 |
| `enrollment_date` | date | 是 |  |  | 入学日期 |
| `political_status` | varchar(20) | 是 |  |  | 政治面貌 |
| `nationality` | varchar(20) | 是 |  | `汉族` | 民族 |
| `home_address` | varchar(255) | 是 |  |  | 家庭住址 |
| `emergency_contact` | varchar(50) | 是 |  |  | 紧急联系人 |
| `emergency_phone` | varchar(20) | 是 |  |  | 紧急联系电话 |
| `photo_url` | varchar(255) | 是 |  |  | 证件照 URL |
| `is_in_school` | tinyint | 否 |  | `1` | 是否在校 1在读 2休学 3退学 4毕业 |
| `remark` | varchar(500) | 是 |  |  | 备注 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_dept

**院系/组织表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `parent_id` | bigint | 否 | MUL | `0` | 父级ID 0=根 |
| `ancestors` | varchar(500) | 否 |  | `0` | 祖级路径 0,1,2 |
| `dept_name` | varchar(100) | 否 |  |  | 院系/部门名称 |
| `dept_code` | varchar(50) | 否 | MUL |  | 院系编码 |
| `leader` | varchar(50) | 是 |  |  | 负责人 |
| `phone` | varchar(20) | 是 |  |  | 联系电话 |
| `sort` | int | 否 |  | `0` | 排序 |
| `status` | tinyint | 否 |  | `1` | 状态 0禁用 1正常 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_dict_data

**字典数据表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `dict_type` | varchar(100) | 否 | MUL |  | 字典类型编码 |
| `dict_label` | varchar(100) | 否 |  |  | 字典标签 |
| `dict_value` | varchar(100) | 否 |  |  | 字典值 |
| `dict_class` | varchar(50) | 是 |  |  | 样式类 primary/success/danger |
| `sort` | int | 否 |  | `0` | 排序 |
| `status` | tinyint | 否 |  | `1` | 状态 0禁用 1正常 |
| `remark` | varchar(500) | 是 |  |  |  |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_dict_type

**字典类型表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `dict_name` | varchar(100) | 否 |  |  | 字典名称 |
| `dict_type` | varchar(100) | 否 | MUL |  | 字典类型编码 |
| `status` | tinyint | 否 |  | `1` | 状态 0禁用 1正常 |
| `remark` | varchar(500) | 是 |  |  |  |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_file

**文件表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `file_name` | varchar(255) | 否 |  |  | 存储文件名 |
| `original_name` | varchar(255) | 是 |  |  | 原始文件名 |
| `object_name` | varchar(500) | 否 |  |  | 对象路径 avatar/xxx.jpg |
| `bucket` | varchar(100) | 否 |  | `campus` | 桶名 |
| `file_type` | varchar(50) | 是 |  |  | MIME |
| `file_size` | bigint | 是 |  |  | 字节数 |
| `url` | varchar(500) | 是 |  |  | 访问地址 |
| `biz_type` | varchar(50) | 是 | MUL |  | 业务类型 avatar/apply/repair/notice |
| `biz_id` | bigint | 是 |  |  | 业务ID |
| `upload_by` | bigint | 是 | MUL |  | 上传人 user_id |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_login_log

**登录日志表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `username` | varchar(50) | 是 | MUL |  | 登录账号 |
| `ip` | varchar(64) | 是 |  |  | 登录IP |
| `location` | varchar(100) | 是 |  |  | 登录地点 |
| `browser` | varchar(100) | 是 |  |  | 浏览器 |
| `os` | varchar(100) | 是 |  |  | 操作系统 |
| `login_time` | datetime | 是 | MUL | `CURRENT_TIMESTAMP` | 登录时间 |
| `status` | tinyint | 否 |  | `1` | 状态 0失败 1成功 |
| `msg` | varchar(255) | 是 |  |  | 提示信息 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_menu

**菜单表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `parent_id` | bigint | 否 | MUL | `0` | 父菜单ID 0=根 |
| `menu_name` | varchar(50) | 否 |  |  | 菜单名称 |
| `menu_type` | char(1) | 否 |  | `C` | M目录 C菜单 F按钮 |
| `path` | varchar(200) | 是 |  |  | 路由地址 |
| `component` | varchar(255) | 是 |  |  | 组件路径 |
| `perms` | varchar(100) | 是 | MUL |  | 权限标识 edu:course:list |
| `icon` | varchar(100) | 是 |  |  | 图标 |
| `sort` | int | 否 |  | `0` | 排序 |
| `visible` | tinyint | 否 |  | `1` | 是否显示 0隐藏 1显示 |
| `keep_alive` | tinyint | 否 |  | `0` | 是否缓存 0否 1是 |
| `status` | tinyint | 否 |  | `1` | 状态 0禁用 1正常 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_message

**站内消息表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `receiver_id` | bigint | 否 | MUL |  | 接收人 user_id |
| `title` | varchar(200) | 否 |  |  | 标题 |
| `content` | varchar(1000) | 是 |  |  | 内容 |
| `msg_type` | tinyint | 否 |  | `1` | 1系统 2待办 3审批结果 4公告 |
| `biz_type` | varchar(50) | 是 |  |  | 业务类型 leave/aid/repair |
| `biz_id` | bigint | 是 |  |  | 业务ID |
| `is_read` | tinyint | 否 |  | `0` | 是否已读 0否 1是 |
| `read_time` | datetime | 是 |  |  | 阅读时间 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_oper_log

**操作日志表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `username` | varchar(50) | 是 | MUL |  | 操作人 |
| `oper_module` | varchar(100) | 是 | MUL |  | 模块 |
| `oper_type` | varchar(20) | 是 |  |  | 操作类型 INSERT/UPDATE/DELETE/EXPORT/APPROVE |
| `method` | varchar(255) | 是 |  |  | 方法名 |
| `request_uri` | varchar(255) | 是 |  |  | 请求地址 |
| `request_method` | varchar(10) | 是 |  |  | 请求方式 |
| `ip` | varchar(64) | 是 |  |  | 操作IP |
| `oper_time` | datetime | 是 | MUL | `CURRENT_TIMESTAMP` | 操作时间 |
| `request_param` | mediumtext | 是 |  |  | 请求参数 |
| `result` | mediumtext | 是 |  |  | 返回结果 |
| `cost_time` | bigint | 是 |  |  | 耗时(ms) |
| `status` | tinyint | 否 |  | `1` | 状态 0失败 1成功 |
| `error_msg` | text | 是 |  |  | 异常信息 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_role

**角色表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `role_name` | varchar(50) | 否 |  |  | 角色名称 |
| `role_code` | varchar(64) | 否 | MUL |  | 角色标识 ROLE_XXX |
| `data_scope` | tinyint | 否 |  | `4` | 数据权限 1全部 2本院系 3本班 4仅本人 5本部门 |
| `sort` | int | 否 |  | `0` | 排序 |
| `status` | tinyint | 否 |  | `1` | 状态 0禁用 1正常 |
| `remark` | varchar(500) | 是 |  |  | 备注 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_role_menu

**角色菜单关联表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `role_id` | bigint | 否 | MUL |  | 角色ID |
| `menu_id` | bigint | 否 | MUL |  | 菜单ID |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## sys_user

**用户表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `username` | varchar(50) | 否 | MUL |  | 登录账号 |
| `password` | varchar(100) | 否 |  |  | BCrypt 密码 |
| `nickname` | varchar(50) | 否 |  |  | 姓名/昵称 |
| `real_name` | varchar(50) | 是 |  |  | 真实姓名 |
| `phone` | varchar(20) | 是 |  |  | 手机号 |
| `email` | varchar(100) | 是 |  |  | 邮箱 |
| `avatar` | varchar(255) | 是 |  |  | 头像 URL |
| `user_type` | tinyint | 否 | MUL | `6` | 1超管 2校级 3教务 4教师 5辅导员 6学生 7后勤 8维修 |
| `dept_id` | bigint | 是 | MUL |  | 所属院系ID |
| `status` | tinyint | 否 | MUL | `1` | 状态 0禁用 1正常 |
| `last_login_time` | datetime | 是 |  |  | 最后登录时间 |
| `last_login_ip` | varchar(64) | 是 |  |  | 最后登录IP |
| `remark` | varchar(500) | 是 |  |  | 备注 |
| `create_by` | varchar(64) | 是 |  |  | 创建人 |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` | 创建时间 |
| `update_by` | varchar(64) | 是 |  |  | 更新人 |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` | 更新时间 |
| `is_deleted` | tinyint | 否 |  | `0` | 逻辑删除 0否 1是 |

## sys_user_role

**用户角色关联表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `user_id` | bigint | 否 | MUL |  | 用户ID |
| `role_id` | bigint | 否 | MUL |  | 角色ID |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |

## teacher

**教师表**

| 字段 | 类型 | 空 | 键 | 默认 | 说明 |
|---|---|---|---|---|---|
| `id` | bigint | 否 | PRI |  | 主键 |
| `user_id` | bigint | 否 | MUL |  | 关联 sys_user.id |
| `tea_no` | varchar(30) | 否 | MUL |  | 工号 |
| `dept_id` | bigint | 否 | MUL |  | 所属院系 |
| `title` | varchar(30) | 是 |  |  | 职称 教授/副教授/讲师/助教 |
| `gender` | tinyint | 是 |  |  | 性别 1男 2女 |
| `entry_date` | date | 是 |  |  | 入职日期 |
| `is_counselor` | tinyint | 否 |  | `0` | 是否兼任辅导员 |
| `status` | tinyint | 否 |  | `1` | 状态 0离职 1在职 |
| `create_by` | varchar(64) | 是 |  |  |  |
| `create_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `update_by` | varchar(64) | 是 |  |  |  |
| `update_time` | datetime | 是 |  | `CURRENT_TIMESTAMP` |  |
| `is_deleted` | tinyint | 否 |  | `0` |  |
