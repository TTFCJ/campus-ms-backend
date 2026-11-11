package com.campus.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.common.annotation.OperLog;
import com.campus.entity.Notice;
import com.campus.mapper.NoticeMapper;
import com.campus.security.RequiresPerm;
import com.campus.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "通知公告")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;

    @Operation(summary = "公告分页")
    @RequiresPerm({"notice:list", "student:notice:list"})
    @GetMapping("/page")
    public Result<PageResult<Notice>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer noticeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Notice> page = new Page<>(current, size);
        var w = Wrappers.<Notice>lambdaQuery()
                .eq(noticeType != null, Notice::getNoticeType, noticeType)
                .eq(status != null, Notice::getStatus, status)
                .like(StringUtils.hasText(keyword), Notice::getTitle, keyword)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getPublishTime);
        Page<Notice> result = noticeMapper.selectPage(page, w);
        return Result.ok(PageResult.<Notice>of(result));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.ok(noticeMapper.selectById(id));
    }

    @Operation(summary = "新增公告")
    @RequiresPerm("notice:add")
    @OperLog(module = "通知公告", type = "INSERT")
    @PostMapping
    public Result<Void> add(@RequestBody Notice body) {
        body.setId(null);
        body.setStatus(0);
        body.setReadCount(0);
        body.setPublishBy(UserContext.getUserId());
        noticeMapper.insert(body);
        return Result.ok("草稿已保存", null);
    }

    @Operation(summary = "修改公告")
    @RequiresPerm("notice:edit")
    @OperLog(module = "通知公告", type = "UPDATE")
    @PutMapping
    public Result<Void> update(@RequestBody Notice body) {
        noticeMapper.updateById(body);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "发布公告")
    @RequiresPerm("notice:publish")
    @OperLog(module = "通知公告", type = "UPDATE")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        Notice n = new Notice();
        n.setId(id);
        n.setStatus(1);
        n.setPublishTime(LocalDateTime.now());
        noticeMapper.updateById(n);
        return Result.ok("已发布", null);
    }

    @Operation(summary = "撤回公告")
    @RequiresPerm("notice:publish")
    @OperLog(module = "通知公告", type = "UPDATE")
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        Notice n = new Notice();
        n.setId(id);
        n.setStatus(2);
        noticeMapper.updateById(n);
        return Result.ok("已撤回", null);
    }

    @Operation(summary = "删除公告")
    @RequiresPerm("notice:remove")
    @OperLog(module = "通知公告", type = "DELETE")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }
}
