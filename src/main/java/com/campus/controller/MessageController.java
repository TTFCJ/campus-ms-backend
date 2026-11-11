package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.Result;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息 / 待办统计
 *
 * 角标数字来源：
 *   学生  → 自己提交后仍未审批完的单据
 *   教师  → 自己提交且待审批的调课
 *   管理  → 全系统待审批 / 待处理
 */
@Tag(name = "消息待办")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final LeaveApplyMapper leaveMapper;
    private final AidApplyMapper aidMapper;
    private final RepairOrderMapper repairMapper;
    private final AdjustApplyMapper adjustMapper;
    private final NoticeMapper noticeMapper;

    @Operation(summary = "未读 / 待办数量（底部 Tab 角标）")
    @GetMapping("/count")
    public Result<Map<String, Object>> count() {
        LoginUser u = UserContext.require();

        long leave = 0;
        long aid = 0;
        long repair = 0;
        long adjust = 0;

        boolean isStudent = u.getStudentId() != null;
        boolean isTeacher = u.getTeacherId() != null;
        boolean isManager = u.getUserType() != null
                && (u.getUserType() == 1 || u.getUserType() == 2 || u.getUserType() == 3
                || u.getUserType() == 5 || u.getUserType() == 6);

        if (isStudent) {
            Long sid = u.getStudentId();
            leave = nz(leaveMapper.selectCount(Wrappers.<LeaveApply>lambdaQuery()
                    .eq(LeaveApply::getStudentId, sid)
                    .in(LeaveApply::getStatus, 1, 2)));
            aid = nz(aidMapper.selectCount(Wrappers.<AidApply>lambdaQuery()
                    .eq(AidApply::getStudentId, sid)
                    .in(AidApply::getStatus, 1, 2, 3)));
            repair = nz(repairMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery()
                    .eq(RepairOrder::getStudentId, sid)
                    .eq(RepairOrder::getStatus, 2)
                    .eq(RepairOrder::getStudentConfirm, 0)));
        } else if (isTeacher) {
            adjust = nz(adjustMapper.selectCount(Wrappers.<AdjustApply>lambdaQuery()
                    .eq(AdjustApply::getTeacherId, u.getTeacherId())
                    .eq(AdjustApply::getStatus, 1)));
        } else if (isManager) {
            leave = nz(leaveMapper.selectCount(Wrappers.<LeaveApply>lambdaQuery()
                    .in(LeaveApply::getStatus, 1, 2)));
            aid = nz(aidMapper.selectCount(Wrappers.<AidApply>lambdaQuery()
                    .in(AidApply::getStatus, 1, 2, 3)));
            repair = nz(repairMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery()
                    .eq(RepairOrder::getStatus, 0)));
            adjust = nz(adjustMapper.selectCount(Wrappers.<AdjustApply>lambdaQuery()
                    .eq(AdjustApply::getStatus, 1)));
        }

        long notice = nz(noticeMapper.selectCount(Wrappers.<Notice>lambdaQuery()
                .eq(Notice::getStatus, 1)));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("leave", leave);
        out.put("aid", aid);
        out.put("repair", repair);
        out.put("adjust", adjust);
        out.put("notice", notice);
        out.put("total", leave + aid + repair + adjust);
        return Result.ok(out);
    }

    private long nz(Long v) {
        return v == null ? 0 : v;
    }
}
