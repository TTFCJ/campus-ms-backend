package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.RepairOrder;
import com.campus.entity.RoomBooking;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.mapper.RepairOrderMapper;
import com.campus.mapper.RoomBookingMapper;
import com.campus.mapper.StudentMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.security.LoginUser;
import com.campus.security.RequiresPerm;
import com.campus.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "后勤-报修与场地预约")
@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class RepairController {

    private final RepairOrderMapper repairMapper;
    private final RoomBookingMapper bookingMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;

    // ---------------- 报修 ----------------

    @Operation(summary = "报修分页")
    @RequiresPerm({"logistics:repair:list", "student:repair:list"})
    @GetMapping("/repair/page")
    public Result<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<RepairOrder> page = new Page<>(current, size);
        var w = Wrappers.<RepairOrder>lambdaQuery()
                .eq(status != null, RepairOrder::getStatus, status)
                .eq(handlerId != null, RepairOrder::getHandlerId, handlerId)
                .orderByDesc(RepairOrder::getCreateTime);
        Page<RepairOrder> result = repairMapper.selectPage(page, w);
        return Result.ok(new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toRows(result.getRecords())));
    }

    @Operation(summary = "提交报修")
    @RequiresPerm("student:repair:add")
    @OperLog(module = "报修", type = "INSERT")
    @PostMapping("/repair")
    public Result<Void> submit(@RequestBody RepairOrder body) {
        LoginUser u = UserContext.require();
        Long sid = body.getStudentId() != null ? body.getStudentId() : u.getStudentId();
        if (sid == null) {
            throw new BizException("缺少学生信息");
        }
        body.setId(null);
        body.setStudentId(sid);
        body.setOrderNo("RP" + System.currentTimeMillis());
        body.setStatus(0);
        body.setStudentConfirm(0);
        repairMapper.insert(body);
        return Result.ok("报修已提交", null);
    }

    @Operation(summary = "派单")
    @RequiresPerm("logistics:repair:dispatch")
    @OperLog(module = "报修", type = "UPDATE")
    @PutMapping("/repair/{id}/dispatch")
    public Result<Void> dispatch(@PathVariable Long id,
                                 @RequestBody Map<String, Object> body) {
        RepairOrder order = repairMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        Long handlerId = body.get("handlerId") instanceof Number n ? n.longValue() : null;
        order.setHandlerId(handlerId);
        order.setStatus(1);
        order.setDispatchTime(LocalDateTime.now());
        repairMapper.updateById(order);
        return Result.ok("已派单", null);
    }

    @Operation(summary = "维修完成")
    @RequiresPerm("logistics:repair:finish")
    @OperLog(module = "报修", type = "UPDATE")
    @PutMapping("/repair/{id}/finish")
    public Result<Void> finish(@PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        RepairOrder order = repairMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        order.setStatus(2);
        order.setFinishTime(LocalDateTime.now());
        order.setFinishPic((String) body.get("finishPic"));
        repairMapper.updateById(order);
        return Result.ok("已标记完成，等待学生确认", null);
    }

    @Operation(summary = "学生确认验收")
    @RequiresPerm("student:repair:confirm")
    @OperLog(module = "报修", type = "UPDATE")
    @PutMapping("/repair/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id,
                                @RequestBody Map<String, Object> body) {
        RepairOrder order = repairMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        order.setStudentConfirm(1);
        if (body.get("score") instanceof Number n) {
            order.setScore(n.intValue());
        }
        order.setFeedback((String) body.get("feedback"));
        repairMapper.updateById(order);
        return Result.ok("已确认验收", null);
    }

    // ---------------- 场地预约 ----------------

    @Operation(summary = "场地预约分页")
    @RequiresPerm("logistics:booking:list")
    @GetMapping("/booking/page")
    public Result<PageResult<RoomBooking>> bookingPage(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<RoomBooking> page = new Page<>(current, size);
        var w = Wrappers.<RoomBooking>lambdaQuery()
                .eq(status != null, RoomBooking::getStatus, status)
                .orderByDesc(RoomBooking::getCreateTime);
        Page<RoomBooking> result = bookingMapper.selectPage(page, w);
        return Result.ok(PageResult.<RoomBooking>of(result));
    }

    @Operation(summary = "提交预约")
    @PostMapping("/booking")
    public Result<Void> submitBooking(@RequestBody RoomBooking body) {
        LoginUser u = UserContext.require();
        body.setId(null);
        body.setApplicantId(u.getUserId());
        body.setApplyNo("BK" + System.currentTimeMillis());
        body.setStatus(1);
        bookingMapper.insert(body);
        return Result.ok("已提交，等待审批", null);
    }

    @Operation(summary = "审批预约")
    @RequiresPerm("logistics:booking:audit")
    @OperLog(module = "场地预约", type = "APPROVE")
    @PutMapping("/booking/{id}/audit")
    public Result<Void> auditBooking(@PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        RoomBooking b = bookingMapper.selectById(id);
        if (b == null) {
            throw new BizException("预约不存在");
        }
        int result = body.get("result") instanceof Number n ? n.intValue() : 1;
        b.setStatus(result == 1 ? 2 : 3);
        b.setApproveOpinion((String) body.getOrDefault("opinion", ""));
        bookingMapper.updateById(b);
        return Result.ok(result == 1 ? "已通过" : "已驳回", null);
    }

    // ---------------- 组装 ----------------

    private List<Map<String, Object>> toRows(List<RepairOrder> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(RepairOrder::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Set<Long> uids = stuMap.values().stream().map(Student::getUserId).collect(Collectors.toSet());
        Set<Long> hids = list.stream().map(RepairOrder::getHandlerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        uids.addAll(hids);

        Map<Long, String> names = new HashMap<>();
        if (!uids.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(uids)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (RepairOrder r : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("orderNo", r.getOrderNo());
            m.put("studentId", r.getStudentId());
            Student s = stuMap.get(r.getStudentId());
            m.put("studentName", s == null ? "—" : names.getOrDefault(s.getUserId(), "学生"));
            m.put("address", r.getAddress());
            m.put("repairType", r.getRepairType());
            m.put("content", r.getContent());
            m.put("fileUrl", r.getFileUrl());
            m.put("status", r.getStatus());
            m.put("handlerId", r.getHandlerId());
            m.put("handlerName", r.getHandlerId() == null ? null : names.get(r.getHandlerId()));
            m.put("dispatchTime", r.getDispatchTime());
            m.put("finishTime", r.getFinishTime());
            m.put("studentConfirm", r.getStudentConfirm());
            m.put("score", r.getScore());
            m.put("createTime", r.getCreateTime());
            out.add(m);
        }
        return out;
    }
}
