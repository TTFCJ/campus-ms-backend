# 接口清单

> 由 `src/main/java/com/campus/controller` 自动提取，共 203 个接口。

所有接口统一前缀 `/api`（`server.servlet.context-path`）。

返回结构：

```json
{ "code": 200, "msg": "success", "data": ..., "timestamp": 1730000000000 }
```

## 教务-调课与评教

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/adjust/page` | 调课分页 |
| `POST` | `/edu/adjust` | 提交调课申请 |
| `PUT` | `/edu/adjust/{id}/audit` | 审批调课（通过后自动改课表） |
| `GET` | `/edu/evaluation/page` | 评教记录分页 |
| `GET` | `/edu/evaluation/stat` | 评教统计（按教师） |
| `POST` | `/edu/evaluation` | 学生提交评教 |

## 学工-助学金

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/study/aid/page` | 助学金分页 |
| `GET` | `/study/aid/my` | 我的助学金申请 |
| `POST` | `/study/aid` | 提交申请 |
| `PUT` | `/study/aid/{id}/audit` | 审批助学金 |
| `PUT` | `/study/aid/{id}/public` | 公示 |

## 课堂签到

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/attend/start` | 发起点名（返回 6 位签到码） |
| `GET` | `/attend/session/{id}` | 场次详情（已签 / 未签名单） |
| `GET` | `/attend/my` | 我的教学班点名历史 |
| `PUT` | `/attend/session/{id}/close` | 提前结束点名 |
| `GET` | `/attend/stat` | 我的出勤统计（教师） |
| `POST` | `/attend/checkin` | 学生签到 |
| `GET` | `/attend/my-records` | 我的签到记录 |
| `GET` | `/attend/edu/stat` | 教务：出勤总览 |
| `GET` | `/attend/edu/absent` | 教务：缺勤名单（排除已批准请假） |
| `PUT` | `/attend/record/{id}/mark` | 标记状态（正常 / 迟到 / 早退） |
| `POST` | `/attend/session/{id}/mark-student` | 教师手动补录（把未签的学生标为正常或请假） |

## 认证中心

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/login` | 登录 |
| `POST` | `/logout` | 退出登录 |
| `GET` | `/captcha` | 图形验证码 |
| `GET` | `/info` | 当前用户信息 |
| `GET` | `/routers` | 当前用户菜单路由 |
| `PUT` | `/profile/password` | 修改自己的密码 |

## 学工-奖惩

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/study/award/page` | 奖惩分页 |
| `POST` | `/study/award` | 新增奖惩 |
| `PUT` | `/study/award` | 修改奖惩 |
| `DELETE` | `/study/award/{id}` | 删除奖惩 |

## 学生关爱

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/care/target/page` | 关爱对象分页 |
| `GET` | `/care/target/{id}` | 关爱对象详情 |
| `POST` | `/care/target` | 纳入关爱 |
| `PUT` | `/care/target` | 修改关爱档案 |
| `PUT` | `/care/target/{id}/release` | 解除关爱 |
| `GET` | `/care/record/list` | 关爱记录列表 |
| `POST` | `/care/record` | 新增关爱记录 |
| `DELETE` | `/care/record/{id}` | 删除关爱记录 |

## 教务-教室

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/classroom/page` | 教室分页 |
| `GET` | `/edu/classroom/list` | 可用教室下拉 |
| `POST` | `/edu/classroom` | 新增教室 |
| `PUT` | `/edu/classroom` | 修改教室 |
| `DELETE` | `/edu/classroom/{id}` | 删除教室 |

## 系统管理-班级

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/clazz/page` | 班级分页 |
| `GET` | `/system/clazz/list` | 班级下拉（排课/学生管理用） |
| `POST` | `/system/clazz` | 新增班级 |
| `PUT` | `/system/clazz` | 修改班级 |
| `DELETE` | `/system/clazz/{id}` | 删除班级 |

## 教务-课程库

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/course/page` | 课程分页 |
| `GET` | `/edu/course/list` | 课程下拉列表 |
| `GET` | `/edu/course/{id}` | 课程详情 |
| `POST` | `/edu/course` | 新增课程 |
| `PUT` | `/edu/course` | 修改课程 |
| `DELETE` | `/edu/course/{id}` | 删除课程 |

## 系统-数据字典

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/dict/type/list` | 字典类型列表 |
| `POST` | `/system/dict/type` | 新增字典类型 |
| `PUT` | `/system/dict/type` | 修改字典类型 |
| `DELETE` | `/system/dict/type/{id}` | 删除字典类型 |
| `GET` | `/system/dict/data/{dictType}` | 按类型查字典数据（前端下拉用） |
| `GET` | `/system/dict/data/list` | 字典数据列表 |
| `POST` | `/system/dict/data` | 新增字典数据 |
| `PUT` | `/system/dict/data` | 修改字典数据 |
| `DELETE` | `/system/dict/data/{id}` | 删除字典数据 |

## 宿舍归寝

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/dorm/checkin/page` | 归寝记录分页 |
| `POST` | `/dorm/checkin` | 登记归寝（已有记录则覆盖） |
| `DELETE` | `/dorm/checkin/{id}` | 删除归寝记录 |
| `GET` | `/dorm/checkin/stat` | 归寝统计（正常/晚归/未归/请假） |
| `GET` | `/dorm/checkin/alerts` | 异常预警（近 N 天晚归/未归） |
| `GET` | `/dorm/checkin/my` | 我的归寝记录（学生） |

## 后勤-宿舍资源

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/logistics/building/page` | 宿舍楼分页 |
| `GET` | `/logistics/building/list` | 宿舍楼下拉 |
| `POST` | `/logistics/building` | 新增宿舍楼 |
| `PUT` | `/logistics/building` | 修改宿舍楼 |
| `DELETE` | `/logistics/building/{id}` | 删除宿舍楼 |
| `GET` | `/logistics/room/page` | 房间分页 |
| `GET` | `/logistics/room/list` | 房间下拉 |
| `POST` | `/logistics/room` | 新增房间 |
| `PUT` | `/logistics/room` | 修改房间 |
| `DELETE` | `/logistics/room/{id}` | 删除房间 |
| `GET` | `/logistics/dorm/page` | 宿舍分配分页 |
| `POST` | `/logistics/dorm` | 分配宿舍 |
| `PUT` | `/logistics/dorm/{id}/checkout` | 退宿 |

## 第二课堂-签到

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/ec/checkin/open` | 开启活动签到（生成 6 位码） |
| `PUT` | `/ec/checkin/close/{activityId}` | 关闭签到 |
| `GET` | `/ec/checkin/list/{activityId}` | 签到名单（已签 / 未签） |
| `POST` | `/ec/checkin/confirm` | 批量确认参加（补录） |
| `GET` | `/ec/checkin/preview/{code}` | 按签到码查活动（签到前预览） |
| `POST` | `/ec/checkin/do` | 学生扫码签到 |
| `GET` | `/ec/checkin/mine` | 我负责的活动（组织者） |

## 第二课堂

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/ec/activity/page` | 活动分页 |
| `GET` | `/ec/activity/open` | 报名中的活动（学生端） |
| `GET` | `/ec/activity/{id}` | 活动详情 |
| `POST` | `/ec/activity` | 新增活动 |
| `PUT` | `/ec/activity` | 修改活动 |
| `POST` | `/ec/activity/{id}/publish` | 发布活动（开放报名） |
| `DELETE` | `/ec/activity/{id}` | 删除活动 |
| `POST` | `/ec/activity/{id}/enroll` | 报名 |
| `DELETE` | `/ec/activity/{id}/enroll` | 取消报名 |
| `POST` | `/ec/activity/{id}/checkin` | 签到（管理端） |
| `GET` | `/ec/enroll/my` | 我的报名 |
| `GET` | `/ec/activity/{id}/enrolls` | 活动报名名单（管理端） |
| `GET` | `/ec/credit/my` | 学分统计 |

## 教务-选课

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/elect/available` | 可选课程列表 |
| `POST` | `/edu/elect/{scheduleId}` | 选课 |
| `DELETE` | `/edu/elect/{scheduleId}` | 退课 |
| `GET` | `/edu/elect/my` | 我的已选课程 |

## 教务-考试与培养方案

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/exam/page` | 考试分页 |
| `POST` | `/edu/exam` | 新增考试 |
| `PUT` | `/edu/exam` | 修改考试 |
| `DELETE` | `/edu/exam/{id}` | 删除考试 |
| `POST` | `/edu/exam/{id}/invigilators` | 监考安排 |
| `GET` | `/edu/exam/{id}/invigilators` | 监考名单 |
| `GET` | `/edu/plan/page` | 培养方案分页 |
| `POST` | `/edu/plan` | 新增培养方案 |
| `DELETE` | `/edu/plan/{id}` | 删除培养方案 |

## 学工-请假

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/study/leave/page` | 请假分页 |
| `GET` | `/study/leave/my` | 我的请假（学生） |
| `POST` | `/study/leave` | 提交请假 |
| `PUT` | `/study/leave/{id}/audit` | 审批请假 |
| `GET` | `/study/leave/{id}/qrcode` | 请假二维码载荷（仅已通过的请假） |

## 系统-日志

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/log/oper/page` | 操作日志分页 |
| `GET` | `/system/log/login/page` | 登录日志分页 |

## 系统管理-专业

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/major/page` | 专业分页 |
| `GET` | `/system/major/list` | 专业下拉 |
| `POST` | `/system/major` | 新增专业 |
| `PUT` | `/system/major` | 修改专业 |
| `DELETE` | `/system/major/{id}` | 删除专业 |

## 消息待办

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/message/count` | 未读 / 待办数量（底部 Tab 角标） |

## 通知公告

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/notice/page` | 公告分页 |
| `GET` | `/notice/{id}` | 公告详情 |
| `POST` | `/notice` | 新增公告 |
| `PUT` | `/notice` | 修改公告 |
| `POST` | `/notice/{id}/publish` | 发布公告 |
| `POST` | `/notice/{id}/withdraw` | 撤回公告 |
| `DELETE` | `/notice/{id}` | 删除公告 |

## 后勤-报修与场地预约

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/logistics/repair/page` | 报修分页 |
| `POST` | `/logistics/repair` | 提交报修 |
| `PUT` | `/logistics/repair/{id}/dispatch` | 派单 |
| `PUT` | `/logistics/repair/{id}/finish` | 维修完成 |
| `PUT` | `/logistics/repair/{id}/confirm` | 学生确认验收 |
| `GET` | `/logistics/booking/page` | 场地预约分页 |
| `POST` | `/logistics/booking` | 提交预约 |
| `PUT` | `/logistics/booking/{id}/audit` | 审批预约 |

## 统计报表

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/report/student` | 学生分布（按院系） |
| `GET` | `/report/score` | 成绩统计（分数段 + 挂科率） |
| `GET` | `/report/aid` | 资助统计 |
| `GET` | `/report/repair` | 报修统计 |
| `GET` | `/report/screen` | 总览（大屏用） |

## 教务-排课与课表

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/schedule/page` | 排课分页 |
| `POST` | `/edu/schedule` | 新增排课（自动做班级/教师/教室冲突检测） |
| `PUT` | `/edu/schedule` | 修改排课 |
| `DELETE` | `/edu/schedule/{id}` | 删除排课 |
| `GET` | `/edu/timetable/clazz` | 班级课表 |
| `GET` | `/edu/timetable/teacher` | 教师课表 |
| `GET` | `/edu/timetable/my` | 我的课表（学生） |

## 教务-成绩

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/score/page` | 成绩分页（教务） |
| `GET` | `/edu/score/schedule/{scheduleId}` | 教学班成绩名单（含未录入的学生） |
| `PUT` | `/edu/score/schedule/{scheduleId}` | 批量录入/修改成绩 |
| `POST` | `/edu/score/schedule/{scheduleId}/submit` | 提交成绩（提交后教师不可改） |
| `POST` | `/edu/score/schedule/{scheduleId}/review` | 教务复核：锁定 / 解锁 |
| `GET` | `/edu/score/my` | 我的成绩（学生） |

## 教务-学期

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/edu/semester/current` | 当前学期 |
| `GET` | `/edu/semester/list` | 学期列表 |
| `POST` | `/edu/semester` | 新增学期 |
| `PUT` | `/edu/semester` | 修改学期 |
| `DELETE` | `/edu/semester/{id}` | 删除学期 |
| `PUT` | `/edu/semester/{id}/current` | 设为当前学期 |

## 学工-学生档案

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/study/student/page` | 学生分页 |
| `GET` | `/study/student/{id}` | 学生详情 |
| `POST` | `/study/student` | 新增学生 |
| `PUT` | `/study/student` | 修改学生 |
| `DELETE` | `/study/student/{id}` | 删除学生 |

## 系统管理-院系

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/dept/tree` | 院系树 |
| `GET` | `/system/dept/list` | 院系平铺列表 |
| `POST` | `/system/dept` | 新增院系 |
| `PUT` | `/system/dept` | 修改院系 |
| `DELETE` | `/system/dept/{id}` | 删除院系 |

## 系统管理-菜单

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/menu/tree` | 菜单树 |
| `GET` | `/system/menu/list` | 菜单列表（平铺） |
| `POST` | `/system/menu` | 新增菜单 |
| `PUT` | `/system/menu` | 修改菜单 |
| `DELETE` | `/system/menu/{id}` | 删除菜单 |

## 系统管理-角色

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/role/page` | 角色分页 |
| `GET` | `/system/role/list` | 全部角色（下拉用） |
| `POST` | `/system/role` | 新增角色 |
| `PUT` | `/system/role` | 修改角色 |
| `DELETE` | `/system/role/{id}` | 删除角色 |
| `GET` | `/system/role/{id}/menuIds` | 角色已授权菜单ID |
| `PUT` | `/system/role/{id}/menus` | 给角色分配菜单 |

## 系统管理-用户

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/system/user/page` | 用户分页列表 |
| `GET` | `/system/user/{id}` | 用户详情 |
| `POST` | `/system/user` | 新增用户 |
| `PUT` | `/system/user` | 修改用户 |
| `DELETE` | `/system/user/{id}` | 删除用户 |
| `PUT` | `/system/user/{id}/status/{status}` | 启用/禁用 |
| `PUT` | `/system/user/{id}/resetPwd` | 重置密码 |
| `GET` | `/system/user/{id}/roles` | 查询用户角色 |
| `PUT` | `/system/user/{id}/roles` | 分配角色 |

## 教师-课堂请假

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/teacher/leave/schedules` | 我的教学班（含今日请假人数） |
| `GET` | `/teacher/leave/list` | 某节课的请假名单（按日期） |
| `POST` | `/teacher/leave/verify` | 核验请假二维码 |
