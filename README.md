# 高校综合管理系统 · 后端

基于 **SpringBoot 3 + MyBatis-Plus + MySQL 8 + Redis** 的高校综合管理系统后端服务。

覆盖教务、学工、后勤、第二课堂、统计报表等业务域，提供 RBAC 权限体系与数据权限隔离。

---

## 技术栈

| 组件 | 版本 | 用途 |
|---|---|---|
| JDK | 17 | 运行时 |
| SpringBoot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM、分页、逻辑删除 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.x | 登录态、缓存、分布式锁 |
| JJWT | 0.12.5 | Token 签发与校验 |
| BCrypt | spring-security-crypto | 密码哈希 |
| Knife4j | 4.5.0 | 接口文档 |
| EasyExcel | 3.3.4 | Excel 导入导出 |
| MinIO | 8.5.9 | 对象存储 |

---

## 功能模块

### 系统管理
- 用户 / 角色 / 菜单 / 院系 / 专业 / 班级 / 学期
- 数据字典、操作日志、登录日志

### 教务管理
- 课程库、教室管理
- 排课（班级 / 教师 / 教室三重冲突检测）
- 课表查询（学生 / 教师 / 班级三视角）
- 选课（Redis 锁 + 原子扣减防超员）
- 成绩管理（5 状态机：待录入 → 已录入 → 已提交 → 已审核 → 已锁定）
- 考试安排、监考安排、培养方案
- 调课申请与审批、教学评教

### 学工管理
- 学生档案
- 请假申请（多级审批，≤3 天辅导员终审）
- 助学金申请（三级审批 + 公示）
- 奖惩记录
- **学生关爱平台**（关爱对象、风险分级、关爱记录）
- **宿舍归寝**（正常 / 晚归 / 未归 / 请假，支持门禁对接）

### 后勤管理
- 宿舍楼、房间、宿舍分配
- 报修工单（待派单 → 维修中 → 已完成 → 学生确认验收）
- 场地预约（教室借用审批）

### 第二课堂
- 活动发布、报名、签到
- 学分统计（按团中央"第二课堂成绩单"制度七模块）

### 课堂考勤
- 教师发起点名 → 6 位签到码
- 学生签到，自动判定 **正常 / 迟到 / 早退 / 缺勤 / 请假**
- 教务端出勤统计与缺勤名单

### 统计报表
- 学生分布、成绩分布、资助统计、报修统计
- 数据大屏（总览指标）

---

## 权限体系

### 功能权限

```java
@RequiresPerm("edu:course:add")
@PostMapping("/edu/course")
public Result<Void> add(@RequestBody Course course) { ... }
```

基于 AOP 的注解式校验，权限标识在 `sys_menu.perms` 中维护，通过角色关联。

### 数据权限

四档隔离，由 `@DataScope` + MyBatis-Plus `DataPermissionInterceptor` 实现：

| 档位 | 范围 |
|---|---|
| 1 | 全部数据 |
| 2 | 本院系 |
| 3 | 本班 |
| 4 | 仅本人 |

### 登录态

- JWT 签发 Token，Redis 存储 `login:token:{token}` → `LoginUser`（含权限集合）
- 支持强制下线（`kickOut`）
- 登录失败 5 次锁定 10 分钟

---

## 快速开始

### 1. 环境要求

```
JDK 17+
Maven 3.8+
MySQL 8.0
Redis 7.x
```

### 2. 建库

```bash
mysql -uroot -p -e "CREATE DATABASE campus_ms DEFAULT CHARSET utf8mb4;"
mysql -uroot -p campus_ms < ../db/01_schema.sql
mysql -uroot -p campus_ms < ../db/02_init_data.sql
```

### 3. 改配置

`src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_ms?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动

```bash
mvn clean package -DskipTests
java -jar target/campus-ms.jar
```

访问接口文档：http://localhost:8080/api/doc.html

---

## 默认账号

密码统一为 `123456`。

| 账号 | 角色 | 说明 |
|---|---|---|
| admin | 超级管理员 | 全部权限 |
| school | 校级管理员 | 全校数据 |
| edu | 教务管理员 | 教务业务 |
| study | 学工管理员 | 学工业务 |
| logistics | 后勤管理员 | 后勤业务 |
| repair | 维修人员 | 报修工单 |
| c_wang | 辅导员 | 本班学生 |
| t_zhang / t_li / t_zhao | 教师 | 授课、成绩录入 |
| stu_001 ~ stu_004 | 学生 | 课表、选课、成绩 |

> 权限变更后需重新登录（权限缓存在 Redis 的 LoginUser 中）。

---

## 目录结构

```
src/main/java/com/campus/
├── common/          通用返回、异常、工具（Result / PageResult / BizException / JwtUtil / RedisUtil）
├── config/          配置类（MyBatis-Plus、Redis、CORS、密码编码器）
├── security/        JWT 拦截器、权限注解、数据权限切面、UserContext
├── entity/          实体（42 个，对应数据库表）
├── mapper/          MyBatis-Plus Mapper（41 个）
├── service/         业务接口与实现（14 个）
├── controller/      REST 接口（32 个）
├── dto/             请求对象
└── vo/              响应对象
```

---

## 核心设计

### 排课冲突检测

```java
// 班级 / 教师 / 教室三重校验
if (存在时间重叠的排课) {
    throw new BizException("该班级在此时间段已有排课");
}
```

时间重叠判定：`w.le(SectionStart, newEnd).ge(SectionEnd, newStart)`

### 选课防超员

```java
// Redis 分布式锁 + SQL 原子扣减
int rows = scheduleMapper.update(null, Wrappers.<Schedule>lambdaUpdate()
        .setSql("current_num = current_num + 1")
        .eq(Schedule::getId, id)
        .apply("current_num < max_num"));
if (rows == 0) throw new BizException("报名人数已满");
```

### 成绩状态机

```
0 待录入 → 1 已录入 → 2 已提交 → 3 已审核 → 4 已锁定
```

绩点计算：`(score - 50) / 10`，60 分以下为 0。

### 课堂签到五态

| 状态 | 判定逻辑 |
|---|---|
| 正常 | 签到码开放后 2 分钟内签到 |
| 迟到 | 超过 2 分钟签到 |
| 早退 | 教师手动标记 |
| 请假 | 当天有 `status=3` 的请假单 |
| 缺勤 | 未签到且无请假 |

缺勤实时计算（不落库），避免定时任务。

### 第二课堂学分

按《高校共青团"第二课堂成绩单"制度实施办法》（中青联发〔2018〕5 号）七模块：

| 模块 | 最低学分 |
|---|---|
| 思想成长 | 1.0 |
| 实践实习 | 1.0 |
| 志愿公益 | 1.0 |
| 创新创业 | 1.0 |
| 文体活动 | 0.5 |
| 工作履历 | 0.5 |
| 技能特长 | 0.5 |
| 合计 | 5.5 |

---

## 数据库

39 张业务表，主要分组：

| 分组 | 表 |
|---|---|
| 系统 | sys_user / sys_role / sys_menu / sys_role_menu / sys_user_role / sys_dept / sys_oper_log / sys_login_log / sys_dict_type / sys_dict_data |
| 基础 | major / clazz / student / teacher |
| 教务 | course / classroom / schedule / semester / score / elect_course / exam / exam_invigilator / evaluation / course_plan / adjust_apply |
| 学工 | leave_apply / aid_apply / award_punish / care_target / care_record |
| 后勤 | dorm_building / dorm_room / dorm_assignment / dorm_checkin / repair_order / room_booking |
| 二课 | ec_activity / ec_enroll |
| 考勤 | attend_session / attend_record |
| 资讯 | notice |

详细表结构见 [docs/DATABASE.md](docs/DATABASE.md)。

---

## 接口文档

启动后访问：

- Knife4j：http://localhost:8080/api/doc.html
- OpenAPI JSON：http://localhost:8080/api/v3/api-docs

接口清单见 [docs/API.md](docs/API.md)。

---

## 开发说明

### 新增一个业务模块

1. 建表（`db/` 下加 SQL）
2. 写 Entity（继承 `BaseEntity`，含 `create_by/create_time/update_by/update_time/is_deleted`）
3. 写 Mapper（`extends BaseMapper<T>` + `@Mapper`）
4. 写 Service + Impl
5. 写 Controller（加 `@RequiresPerm`）
6. 在 `sys_menu` 加菜单与按钮，授权给角色

### 分页查询注意

```java
// 泛型嵌套时需显式指定
Page<Course> result = courseMapper.selectPage(page, w);
return Result.ok(PageResult.<Course>of(result));
```

`Result<PageResult<T>>` 嵌套时 Java 无法推断 T。

---

## License

MIT
