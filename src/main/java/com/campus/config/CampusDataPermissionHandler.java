package com.campus.config;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限处理器（MyBatis-Plus 自定义 SQL 拦截器）
 * <p>
 * 规则：
 * data_scope=1 全部数据 → 不加条件
 * data_scope=2 本院系   → dept_id = 当前院系
 * data_scope=3 本班     → clazz_id in (辅导员所带班级)
 * data_scope=4 仅本人   → 学生表按 student_id / user_id 过滤
 * <p>
 * 只对 <b>标注了 @DataScope 的 Service 方法</b> 生效，避免登录查询被误伤。
 */
@Slf4j
public class CampusDataPermissionHandler implements MultiDataPermissionHandler {

    /** 学生维度表：列名 student_id */
    private static final Set<String> STUDENT_TABLES = Set.of(
            "leave_apply", "aid_apply", "repair_order", "elect_course", "score",
            "evaluation", "award_punish", "dorm_assignment", "dorm_change"
    );

    /** 班级维度表：列名 clazz_id */
    private static final Set<String> CLAZZ_TABLES = Set.of(
            "student", "leave_apply", "aid_apply"
    );

    /** 院系维度表：列名 dept_id */
    private static final Set<String> DEPT_TABLES = Set.of(
            "sys_user", "student", "teacher", "clazz", "course", "major", "schedule"
    );

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        // 未开启数据权限，不处理
        if (!UserContext.scopeEnabled()) {
            return null;
        }
        LoginUser user = UserContext.get();
        if (user == null || user.getDataScope() == null) {
            return null;
        }
        int scope = user.getDataScope();
        if (scope == 1) {
            return null;
        }
        String t = table.getName().toLowerCase().replace("`", "");
        try {
            switch (scope) {
                case 4 -> {
                    return buildSelf(user, t);
                }
                case 3 -> {
                    return buildClazz(user, t);
                }
                case 2 -> {
                    return buildDept(user, t);
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("[数据权限] 拼接 SQL 失败, table={}, scope={}", t, scope, e);
            return null;
        }
    }

    /** 仅本人 */
    private Expression buildSelf(LoginUser user, String table) {
        if ("sys_user".equals(table)) {
            return eq("id", user.getUserId());
        }
        if ("student".equals(table)) {
            return eq("id", user.getStudentId());
        }
        if ("teacher".equals(table)) {
            return eq("id", user.getTeacherId());
        }
        if (STUDENT_TABLES.contains(table) && user.getStudentId() != null) {
            return eq("student_id", user.getStudentId());
        }
        return null;
    }

    /** 本班 */
    private Expression buildClazz(LoginUser user, String table) {
        List<Long> clazzIds = user.getCounselorClazzIds();
        if (clazzIds == null || clazzIds.isEmpty()) {
            // 没有带班，返回永假条件，防止看到任何数据
            return parse("1 = 0");
        }
        if ("student".equals(table)) {
            return in("clazz_id", clazzIds);
        }
        if (CLAZZ_TABLES.contains(table)) {
            return in("clazz_id", clazzIds);
        }
        return null;
    }

    /** 本院系 */
    private Expression buildDept(LoginUser user, String table) {
        if (user.getDeptId() == null) {
            return null;
        }
        if (DEPT_TABLES.contains(table)) {
            return eq("dept_id", user.getDeptId());
        }
        return null;
    }

    private Expression eq(String column, Long value) {
        if (value == null) {
            return null;
        }
        return parse(column + " = " + value);
    }

    private Expression in(String column, List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String joined = values.stream().map(String::valueOf).collect(Collectors.joining(","));
        return parse(column + " IN (" + joined + ")");
    }

    private Expression parse(String sql) {
        try {
            return CCJSqlParserUtil.parseCondExpression(sql);
        } catch (Exception e) {
            log.error("[数据权限] 解析条件失败: {}", sql, e);
            return null;
        }
    }
}
