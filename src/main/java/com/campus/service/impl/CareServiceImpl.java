package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.dto.CareRecordDTO;
import com.campus.dto.CareTargetDTO;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.UserContext;
import com.campus.service.CareService;
import com.campus.vo.CareRecordVO;
import com.campus.vo.CareTargetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareServiceImpl implements CareService {

    private final CareTargetMapper targetMapper;
    private final CareRecordMapper recordMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final ClazzMapper clazzMapper;

    @Override
    public PageResult<CareTargetVO> targetPage(Integer riskLevel, Integer status,
                                               String keyword, Long current, Long size) {
        Page<CareTarget> page = new Page<>(current == null ? 1 : current,
                size == null ? 10 : size);

        // 关键字命中学生姓名时，先筛出学生 ID
        Set<Long> matchedStudentIds = null;
        if (StringUtils.hasText(keyword)) {
            matchedStudentIds = matchStudentIds(keyword);
            if (matchedStudentIds.isEmpty()) {
                return PageResult.empty(current, size);
            }
        }

        var w = Wrappers.<CareTarget>lambdaQuery()
                .eq(riskLevel != null, CareTarget::getRiskLevel, riskLevel)
                .eq(status != null, CareTarget::getStatus, status)
                .in(matchedStudentIds != null, CareTarget::getStudentId, matchedStudentIds)
                .orderByAsc(CareTarget::getRiskLevel)
                .orderByDesc(CareTarget::getId);
        Page<CareTarget> result = targetMapper.selectPage(page, w);
        return new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toTargetVOs(result.getRecords()));
    }

    @Override
    public CareTargetVO targetDetail(Long id) {
        CareTarget t = targetMapper.selectById(id);
        if (t == null) {
            throw new BizException("关爱对象不存在");
        }
        List<CareTargetVO> list = toTargetVOs(List.of(t));
        return list.get(0);
    }

    @Override
    public void addTarget(CareTargetDTO dto) {
        Long exists = targetMapper.selectCount(Wrappers.<CareTarget>lambdaQuery()
                .eq(CareTarget::getStudentId, dto.getStudentId())
                .eq(CareTarget::getStatus, 1));
        if (exists != null && exists > 0) {
            throw new BizException("该学生已在关爱名单中");
        }
        CareTarget t = new CareTarget();
        t.setStudentId(dto.getStudentId());
        t.setTags(dto.getTags());
        t.setRiskLevel(dto.getRiskLevel() == null ? 1 : dto.getRiskLevel());
        t.setReason(dto.getReason());
        t.setCounselorId(dto.getCounselorId());
        t.setStatus(1);
        targetMapper.insert(t);
    }

    @Override
    public void updateTarget(CareTargetDTO dto) {
        if (dto.getId() == null) {
            throw new BizException("关爱对象ID不能为空");
        }
        CareTarget t = new CareTarget();
        t.setId(dto.getId());
        t.setTags(dto.getTags());
        t.setRiskLevel(dto.getRiskLevel());
        t.setReason(dto.getReason());
        t.setCounselorId(dto.getCounselorId());
        targetMapper.updateById(t);
    }

    @Override
    public void releaseTarget(Long id) {
        CareTarget t = new CareTarget();
        t.setId(id);
        t.setStatus(2);
        targetMapper.updateById(t);
    }

    @Override
    public List<CareRecordVO> recordList(Long targetId, Long studentId) {
        var w = Wrappers.<CareRecord>lambdaQuery()
                .eq(targetId != null, CareRecord::getTargetId, targetId)
                .eq(studentId != null, CareRecord::getStudentId, studentId)
                .orderByDesc(CareRecord::getRecordTime);
        return toRecordVOs(recordMapper.selectList(w));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(CareRecordDTO dto) {
        CareTarget t = targetMapper.selectById(dto.getTargetId());
        if (t == null) {
            throw new BizException("关爱对象不存在");
        }
        CareRecord r = new CareRecord();
        r.setTargetId(dto.getTargetId());
        r.setStudentId(t.getStudentId());
        r.setRecordType(dto.getRecordType() == null ? 1 : dto.getRecordType());
        r.setTitle(dto.getTitle());
        r.setContent(dto.getContent());
        r.setResult(dto.getResult());
        r.setRecordTime(dto.getRecordTime() == null ? LocalDateTime.now() : dto.getRecordTime());
        r.setAttachment(dto.getAttachment());
        r.setRecorderId(UserContext.getUserId());
        recordMapper.insert(r);
    }

    @Override
    public void removeRecord(Long id) {
        recordMapper.deleteById(id);
    }

    // ------------------------------------------------------------ 组装

    private List<CareTargetVO> toTargetVOs(List<CareTarget> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(CareTarget::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        Set<Long> userIds = stuMap.values().stream()
                .map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(userIds)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        Set<Long> clazzIds = stuMap.values().stream()
                .map(Student::getClazzId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = clazzIds.isEmpty() ? Map.of()
                : clazzMapper.selectBatchIds(clazzIds).stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        // 每个对象的记录数
        Map<Long, Integer> recordCounts = new HashMap<>();
        for (CareTarget t : list) {
            Long n = recordMapper.selectCount(Wrappers.<CareRecord>lambdaQuery()
                    .eq(CareRecord::getTargetId, t.getId()));
            recordCounts.put(t.getId(), n == null ? 0 : n.intValue());
        }

        List<CareTargetVO> out = new ArrayList<>();
        for (CareTarget t : list) {
            CareTargetVO vo = new CareTargetVO();
            vo.setId(t.getId());
            vo.setStudentId(t.getStudentId());
            vo.setTags(t.getTags());
            vo.setRiskLevel(t.getRiskLevel());
            vo.setStatus(t.getStatus());
            vo.setReason(t.getReason());
            vo.setCounselorId(t.getCounselorId());
            vo.setCreateTime(t.getCreateTime());

            Student stu = stuMap.get(t.getStudentId());
            if (stu != null) {
                vo.setStudentName(names.getOrDefault(stu.getUserId(), "学生" + stu.getId()));
                vo.setStuNo(stu.getStuNo());
                Clazz c = stu.getClazzId() == null ? null : clazzMap.get(stu.getClazzId());
                vo.setClazzName(c == null ? null : c.getClazzName());
            }
            vo.setRecordCount(recordCounts.getOrDefault(t.getId(), 0));
            out.add(vo);
        }
        return out;
    }

    private List<CareRecordVO> toRecordVOs(List<CareRecord> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(CareRecord::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        Set<Long> userIds = new HashSet<>();
        stuMap.values().forEach(s -> userIds.add(s.getUserId()));
        list.stream().map(CareRecord::getRecorderId).filter(Objects::nonNull)
                .forEach(userIds::add);

        Map<Long, String> names = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(userIds)) {
                names.put(u.getId(), u.getNickname());
            }
        }

        List<CareRecordVO> out = new ArrayList<>();
        for (CareRecord r : list) {
            CareRecordVO vo = new CareRecordVO();
            vo.setId(r.getId());
            vo.setTargetId(r.getTargetId());
            vo.setStudentId(r.getStudentId());
            vo.setRecordType(r.getRecordType());
            vo.setTitle(r.getTitle());
            vo.setContent(r.getContent());
            vo.setResult(r.getResult());
            vo.setRecorderId(r.getRecorderId());
            vo.setRecordTime(r.getRecordTime());
            vo.setAttachment(r.getAttachment());

            Student stu = stuMap.get(r.getStudentId());
            if (stu != null) {
                vo.setStudentName(names.getOrDefault(stu.getUserId(), "学生" + stu.getId()));
                vo.setStuNo(stu.getStuNo());
            }
            if (r.getRecorderId() != null) {
                vo.setRecorderName(names.getOrDefault(r.getRecorderId(), "系统"));
            }
            vo.setRecordTypeName(switch (r.getRecordType() == null ? 1 : r.getRecordType()) {
                case 2 -> "家访";
                case 3 -> "帮扶";
                case 4 -> "预警处置";
                default -> "谈心谈话";
            });
            out.add(vo);
        }
        return out;
    }

    private Set<Long> matchStudentIds(String keyword) {
        Set<Long> out = new HashSet<>();
        // 学号直接匹配
        for (Student s : studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                .like(Student::getStuNo, keyword).last("LIMIT 200"))) {
            out.add(s.getId());
        }
        // 姓名匹配：先查 user
        List<SysUser> users = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .like(SysUser::getNickname, keyword).last("LIMIT 200"));
        if (!users.isEmpty()) {
            Set<Long> uids = users.stream().map(SysUser::getId).collect(Collectors.toSet());
            for (Student s : studentMapper.selectList(Wrappers.<Student>lambdaQuery()
                    .in(Student::getUserId, uids))) {
                out.add(s.getId());
            }
        }
        return out;
    }
}
