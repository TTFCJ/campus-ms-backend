package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.common.exception.BizException;
import com.campus.entity.DormAssignment;
import com.campus.entity.DormBuilding;
import com.campus.entity.DormRoom;
import com.campus.mapper.DormAssignmentMapper;
import com.campus.mapper.DormBuildingMapper;
import com.campus.mapper.DormRoomMapper;
import com.campus.security.RequiresPerm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后勤-宿舍资源")
@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class DormResourceController {

    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormAssignmentMapper assignMapper;

    // ---------------- 宿舍楼 ----------------

    @Operation(summary = "宿舍楼分页")
    @RequiresPerm("logistics:building:list")
    @GetMapping("/building/page")
    public Result<PageResult<DormBuilding>> buildingPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<DormBuilding> page = new Page<>(current, size);
        Page<DormBuilding> result = buildingMapper.selectPage(page,
                Wrappers.<DormBuilding>lambdaQuery().orderByAsc(DormBuilding::getBuildingNo));
        return Result.ok(PageResult.<DormBuilding>of(result));
    }

    @Operation(summary = "宿舍楼下拉")
    @GetMapping("/building/list")
    public Result<List<DormBuilding>> buildingList() {
        return Result.ok(buildingMapper.selectList(Wrappers.<DormBuilding>lambdaQuery()
                .eq(DormBuilding::getStatus, 1)
                .orderByAsc(DormBuilding::getBuildingNo)));
    }

    @Operation(summary = "新增宿舍楼")
    @RequiresPerm("logistics:building:add")
    @OperLog(module = "宿舍管理", type = "INSERT")
    @PostMapping("/building")
    public Result<Void> addBuilding(@RequestBody DormBuilding body) {
        buildingMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改宿舍楼")
    @RequiresPerm("logistics:building:edit")
    @PutMapping("/building")
    public Result<Void> updateBuilding(@RequestBody DormBuilding body) {
        buildingMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除宿舍楼")
    @RequiresPerm("logistics:building:remove")
    @DeleteMapping("/building/{id}")
    public Result<Void> removeBuilding(@PathVariable Long id) {
        Long rooms = roomMapper.selectCount(Wrappers.<DormRoom>lambdaQuery()
                .eq(DormRoom::getBuildingId, id));
        if (rooms != null && rooms > 0) {
            throw new BizException("该楼栋下还有 " + rooms + " 个房间，无法删除");
        }
        buildingMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }

    // ---------------- 房间 ----------------

    @Operation(summary = "房间分页")
    @RequiresPerm("logistics:room:list")
    @GetMapping("/room/page")
    public Result<PageResult<DormRoom>> roomPage(
            @RequestParam(required = false) Long buildingId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<DormRoom> page = new Page<>(current, size);
        var w = Wrappers.<DormRoom>lambdaQuery()
                .eq(buildingId != null, DormRoom::getBuildingId, buildingId)
                .orderByAsc(DormRoom::getBuildingId)
                .orderByAsc(DormRoom::getRoomNo);
        Page<DormRoom> result = roomMapper.selectPage(page, w);
        return Result.ok(PageResult.<DormRoom>of(result));
    }

    @Operation(summary = "房间下拉")
    @GetMapping("/room/list")
    public Result<List<DormRoom>> roomList() {
        return Result.ok(roomMapper.selectList(Wrappers.<DormRoom>lambdaQuery()
                .orderByAsc(DormRoom::getBuildingId)
                .orderByAsc(DormRoom::getRoomNo)));
    }

    @Operation(summary = "新增房间")
    @RequiresPerm("logistics:room:add")
    @PostMapping("/room")
    public Result<Void> addRoom(@RequestBody DormRoom body) {
        roomMapper.insert(body);
        return Result.ok("新增成功", null);
    }

    @Operation(summary = "修改房间")
    @RequiresPerm("logistics:room:edit")
    @PutMapping("/room")
    public Result<Void> updateRoom(@RequestBody DormRoom body) {
        roomMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除房间")
    @RequiresPerm("logistics:room:remove")
    @DeleteMapping("/room/{id}")
    public Result<Void> removeRoom(@PathVariable Long id) {
        roomMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }

    // ---------------- 分配 ----------------

    @Operation(summary = "宿舍分配分页")
    @RequiresPerm("logistics:dorm:list")
    @GetMapping("/dorm/page")
    public Result<PageResult<DormAssignment>> assignPage(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<DormAssignment> page = new Page<>(current, size);
        var w = Wrappers.<DormAssignment>lambdaQuery()
                .eq(status != null, DormAssignment::getStatus, status)
                .orderByDesc(DormAssignment::getCreateTime);
        Page<DormAssignment> result = assignMapper.selectPage(page, w);
        return Result.ok(PageResult.<DormAssignment>of(result));
    }

    @Operation(summary = "分配宿舍")
    @RequiresPerm("logistics:dorm:assign")
    @OperLog(module = "宿舍管理", type = "INSERT")
    @PostMapping("/dorm")
    public Result<Void> assign(@RequestBody DormAssignment body) {
        body.setId(null);
        body.setStatus(1);
        body.setCheckInTime(java.time.LocalDateTime.now());
        assignMapper.insert(body);

        // 房间已住人数 +1
        roomMapper.update(null, Wrappers.<DormRoom>lambdaUpdate()
                .setSql("used_count = used_count + 1")
                .eq(DormRoom::getId, body.getRoomId()));
        return Result.ok("分配成功", null);
    }

    @Operation(summary = "退宿")
    @RequiresPerm("logistics:dorm:assign")
    @PutMapping("/dorm/{id}/checkout")
    public Result<Void> checkout(@PathVariable Long id) {
        DormAssignment a = assignMapper.selectById(id);
        if (a == null) {
            throw new BizException("分配记录不存在");
        }
        a.setStatus(2);
        a.setCheckOutTime(java.time.LocalDateTime.now());
        assignMapper.updateById(a);

        roomMapper.update(null, Wrappers.<DormRoom>lambdaUpdate()
                .setSql("used_count = used_count - 1")
                .eq(DormRoom::getId, a.getRoomId())
                .apply("used_count > 0"));
        return Result.ok("已退宿", null);
    }
}
