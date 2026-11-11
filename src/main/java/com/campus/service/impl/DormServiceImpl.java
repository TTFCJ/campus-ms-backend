package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import com.campus.service.DormService;
import com.campus.vo.DormCheckinVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DormServiceImpl implements DormService {

    private final DormCheckinMapper checkinMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;
    private final ClazzMapper clazzMapper;
    private final DormRoomMapper roomMapper;

    @Override
    public PageResult<DormCheckinVO> page(LocalDate date, Integer status, Long clazzId,
                                          String keyword, Long current, Long size) {
        Page<DormCheckin> page = new Page<>(current == null ? 1 : current,
                size == null ? 10 : size);

        Set<Long> allowedStudentIds = null;
        if (clazzId != null || StringUtils.hasText(keyword)) {
            allowedStudentIds = filterStudentIds(clazzId, keyword);
            if (allowedStudentIds.isEmpty()) {
                return PageResult.empty(current, size);
            }
        }

        var w = Wrappers.<DormCheckin>lambdaQuery()
                .eq(date != null, DormCheckin::getCheckDate, date)
                .eq(status != null, DormCheckin::getStatus, status)
                .in(allowedStudentIds != null, DormCheckin::getStudentId, allowedStudentIds)
                .orderByDesc(DormCheckin::getCheckDate)
                .orderByAsc(DormCheckin::getStatus);
        Page<DormCheckin> result = checkinMapper.selectPage(page, w);
        return new PageResult<>(result.getTotal(), result.getCurrent(),
                result.getSize(), toVOs(result.getRecords()));
    }

    @Override
    public void save(DormCheckin body) {
        if (body.getStudentId() == null) {
            throw new BizException("学生不能为空");
        }
        LocalDate date = body.getCheckDate() == null ? LocalDate.now() : body.getCheckDate();

        DormCheckin exist = checkinMapper.selectOne(Wrappers.<DormCheckin>lambdaQuery()
                .eq(DormCheckin::getStudentId, body.getStudentId())
                .eq(DormCheckin::getCheckDate, date)
                .last("LIMIT 1"));

        if (exist == null) {
            body.setCheckDate(date);
            body.setCheckTime(body.getCheckTime() == null
                    ? LocalDateTime.now() : body.getCheckTime());
            if (body.getStatus() == null) {
                body.setStatus(1);
            }
            if (body.getSource() == null) {
                body.setSource(2);
            }
            checkinMapper.insert(body);
        } else {
            checkinMapper.update(null, Wrappers.<DormCheckin>lambdaUpdate()
                    .set(DormCheckin::getStatus, body.getStatus())
                    .set(DormCheckin::getCheckTime, body.getCheckTime())
                    .set(DormCheckin::getSource, body.getSource())
                    .set(DormCheckin::getRemark, body.getRemark())
                    .set(DormCheckin::getRoomId, body.getRoomId())
                    .eq(DormCheckin::getId, exist.getId()));
        }
    }

    @Override
    public void remove(Long id) {
        checkinMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> stat(LocalDate date) {
        LocalDate d = date == null ? LocalDate.now() : date;
        List<Map<String, Object>> rows = checkinMapper.countByStatus(d);

        int normal = 0, late = 0, absent = 0, leave = 0;
        for (Map<String, Object> row : rows) {
            int status = ((Number) row.get("status")).intValue();
            int cnt = ((Number) row.get("cnt")).intValue();
            switch (status) {
                case 1 -> normal = cnt;
                case 2 -> late = cnt;
                case 3 -> absent = cnt;
                case 4 -> leave = cnt;
                default -> { }
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("date", d.toString());
        out.put("normal", normal);
        out.put("late", late);
        out.put("absent", absent);
        out.put("leave", leave);
        out.put("total", normal + late + absent + leave);
        return out;
    }

    @Override
    public List<DormCheckinVO> alerts(int days) {
        LocalDate from = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        return toVOs(checkinMapper.selectAlerts(from));
    }

    @Override
    public List<DormCheckinVO> myCheckins(int days) {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("当前账号不是学生");
        }
        LocalDate from = LocalDate.now().minusDays(Math.max(days, 1) - 1L);
        List<DormCheckin> list = checkinMapper.selectList(Wrappers.<DormCheckin>lambdaQuery()
                .eq(DormCheckin::getStudentId, u.getStudentId())
                .ge(DormCheckin::getCheckDate, from)
                .orderByDesc(DormCheckin::getCheckDate));
        return toVOs(list);
    }

    // ------------------------------------------------------------ 组装

    private Set<Long> filterStudentIds(Long clazzId, String keyword) {
        var w = Wrappers.<Student>lambdaQuery();
        if (clazzId != null) {
            w.eq(Student::getClazzId, clazzId);
        }
        if (StringUtils.hasText(keyword)) {
            w.like(Student::getStuNo, keyword);
        }
        return studentMapper.selectList(w).stream()
                .map(Student::getId).collect(Collectors.toSet());
    }

    private List<DormCheckinVO> toVOs(List<DormCheckin> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> sids = list.stream().map(DormCheckin::getStudentId).collect(Collectors.toSet());
        Map<Long, Student> stuMap = studentMapper.selectBatchIds(sids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        Set<Long> uids = stuMap.values().stream().map(Student::getUserId).collect(Collectors.toSet());
        Map<Long, String> names = new HashMap<>();
        if (!uids.isEmpty()) {
            for (SysUser u : userMapper.selectBatchIds(uids)) {
                names.put(u.getId(), u.getNickname());
            }
        }
        Set<Long> cids = stuMap.values().stream().map(Student::getClazzId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = cids.isEmpty() ? Map.of()
                : clazzMapper.selectBatchIds(cids).stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        Set<Long> rids = list.stream().map(DormCheckin::getRoomId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, DormRoom> roomMap = rids.isEmpty() ? Map.of()
                : roomMapper.selectBatchIds(rids).stream()
                .collect(Collectors.toMap(DormRoom::getId, Function.identity()));

        List<DormCheckinVO> out = new ArrayList<>();
        for (DormCheckin c : list) {
            DormCheckinVO vo = new DormCheckinVO();
            vo.setId(c.getId());
            vo.setStudentId(c.getStudentId());
            vo.setRoomId(c.getRoomId());
            vo.setCheckDate(c.getCheckDate());
            vo.setCheckTime(c.getCheckTime());
            vo.setStatus(c.getStatus());
            vo.setSource(c.getSource());
            vo.setRemark(c.getRemark());

            Student stu = stuMap.get(c.getStudentId());
            if (stu != null) {
                vo.setStudentName(names.getOrDefault(stu.getUserId(), "学生" + stu.getId()));
                vo.setStuNo(stu.getStuNo());
                Clazz cl = stu.getClazzId() == null ? null : clazzMap.get(stu.getClazzId());
                vo.setClazzName(cl == null ? null : cl.getClazzName());
            }
            DormRoom room = c.getRoomId() == null ? null : roomMap.get(c.getRoomId());
            vo.setRoomNo(room == null ? null : room.getRoomNo());

            vo.setStatusName(switch (c.getStatus() == null ? 1 : c.getStatus()) {
                case 2 -> "晚归";
                case 3 -> "未归";
                case 4 -> "请假";
                default -> "正常";
            });
            vo.setSourceName(switch (c.getSource() == null ? 2 : c.getSource()) {
                case 1 -> "门禁";
                case 3 -> "扫码";
                default -> "手动";
            });
            out.add(vo);
        }
        return out;
    }
}
