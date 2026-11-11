package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.exception.BizException;
import com.campus.entity.EcActivity;
import com.campus.entity.EcEnroll;
import com.campus.entity.Student;
import com.campus.entity.SysUser;
import com.campus.mapper.EcActivityMapper;
import com.campus.mapper.EcEnrollMapper;
import com.campus.mapper.StudentMapper;
import com.campus.mapper.SysUserMapper;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import com.campus.service.EcService;
import com.campus.vo.EcEnrollVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EcServiceImpl implements EcService {

    private final EcActivityMapper activityMapper;
    private final EcEnrollMapper enrollMapper;
    private final StudentMapper studentMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResult<EcActivity> activityPage(Integer status, Integer category,
                                               String keyword, Long current, Long size) {
        Page<EcActivity> page = new Page<>(current == null ? 1 : current,
                size == null ? 10 : size);
        var w = Wrappers.<EcActivity>lambdaQuery()
                .eq(status != null, EcActivity::getStatus, status)
                .eq(category != null, EcActivity::getCategory, category)
                .like(StringUtils.hasText(keyword), EcActivity::getTitle, keyword)
                .orderByDesc(EcActivity::getStartTime);
        return PageResult.of(activityMapper.selectPage(page, w));
    }

    @Override
    public List<EcActivity> openActivities() {
        return activityMapper.selectList(Wrappers.<EcActivity>lambdaQuery()
                .eq(EcActivity::getStatus, 1)
                .ge(EcActivity::getEndTime, LocalDateTime.now())
                .orderByAsc(EcActivity::getStartTime));
    }

    @Override
    public EcActivity activityDetail(Long id) {
        EcActivity a = activityMapper.selectById(id);
        if (a == null) {
            throw new BizException("活动不存在");
        }
        return a;
    }

    @Override
    public void addActivity(EcActivity body) {
        body.setId(null);
        body.setEnrollCount(0);
        if (body.getStatus() == null) {
            body.setStatus(0);
        }
        body.setPublisherId(UserContext.getUserId());
        activityMapper.insert(body);
    }

    @Override
    public void updateActivity(EcActivity body) {
        if (body.getId() == null) {
            throw new BizException("活动ID不能为空");
        }
        activityMapper.updateById(body);
    }

    @Override
    public void publish(Long id) {
        EcActivity a = new EcActivity();
        a.setId(id);
        a.setStatus(1);
        activityMapper.updateById(a);
    }

    @Override
    public void removeActivity(Long id) {
        activityMapper.deleteById(id);
    }

    /**
     * 报名：
     *   · 状态必须是「报名中」
     *   · 容量用原子 UPDATE 扣减，防并发超员
     *   · 取消过再报名走 update，不新建记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enroll(Long activityId) {
        Long sid = requireStudentId();

        EcActivity a = activityMapper.selectById(activityId);
        if (a == null) {
            throw new BizException("活动不存在");
        }
        if (!Integer.valueOf(1).equals(a.getStatus())) {
            throw new BizException("活动未开放报名");
        }

        EcEnroll exist = enrollMapper.selectOne(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getActivityId, activityId)
                .eq(EcEnroll::getStudentId, sid)
                .last("LIMIT 1"));
        if (exist != null && Integer.valueOf(1).equals(exist.getStatus())) {
            throw new BizException("你已经报过名了");
        }

        int rows = activityMapper.update(null, Wrappers.<EcActivity>lambdaUpdate()
                .setSql("enroll_count = enroll_count + 1")
                .eq(EcActivity::getId, activityId)
                .apply("enroll_count < capacity"));
        if (rows == 0) {
            throw new BizException("报名人数已满");
        }

        if (exist != null) {
            enrollMapper.update(null, Wrappers.<EcEnroll>lambdaUpdate()
                    .set(EcEnroll::getStatus, 1)
                    .set(EcEnroll::getEnrollTime, LocalDateTime.now())
                    .eq(EcEnroll::getId, exist.getId()));
        } else {
            EcEnroll e = new EcEnroll();
            e.setActivityId(activityId);
            e.setStudentId(sid);
            e.setEnrollTime(LocalDateTime.now());
            e.setStatus(1);
            enrollMapper.insert(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long activityId) {
        Long sid = requireStudentId();

        EcEnroll exist = enrollMapper.selectOne(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getActivityId, activityId)
                .eq(EcEnroll::getStudentId, sid)
                .eq(EcEnroll::getStatus, 1)
                .last("LIMIT 1"));
        if (exist == null) {
            throw new BizException("没有找到报名记录");
        }

        enrollMapper.update(null, Wrappers.<EcEnroll>lambdaUpdate()
                .set(EcEnroll::getStatus, 3)
                .eq(EcEnroll::getId, exist.getId()));

        activityMapper.update(null, Wrappers.<EcActivity>lambdaUpdate()
                .setSql("enroll_count = enroll_count - 1")
                .eq(EcActivity::getId, activityId)
                .apply("enroll_count > 0"));
    }

    @Override
    public void checkin(Long activityId, Long studentId) {
        EcEnroll e = enrollMapper.selectOne(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getActivityId, activityId)
                .eq(EcEnroll::getStudentId, studentId)
                .last("LIMIT 1"));
        if (e == null) {
            throw new BizException("该学生没有报名此活动");
        }
        enrollMapper.update(null, Wrappers.<EcEnroll>lambdaUpdate()
                .set(EcEnroll::getStatus, 2)
                .set(EcEnroll::getCheckinTime, LocalDateTime.now())
                .eq(EcEnroll::getId, e.getId()));
    }

    @Override
    public List<EcEnrollVO> myEnrolls() {
        Long sid = requireStudentId();
        List<EcEnroll> list = enrollMapper.selectList(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getStudentId, sid)
                .orderByDesc(EcEnroll::getEnrollTime));
        return toVOList(list, false);
    }

    @Override
    public List<EcEnrollVO> activityEnrolls(Long activityId) {
        List<EcEnroll> list = enrollMapper.selectList(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getActivityId, activityId)
                .orderByAsc(EcEnroll::getEnrollTime));
        return toVOList(list, true);
    }

    /**
     * 学分统计 —— 按教育部 / 团中央《高校共青团"第二课堂成绩单"制度实施办法》
     * （中青联发〔2018〕5 号）的七个模块统计。
     *
     * 模块与最低要求（专科口径，合计 5.5）：
     *   思想成长 1.0 / 实践实习 1.0 / 志愿公益 1.0 / 创新创业 1.0
     *   文体活动 0.5 / 工作履历 0.5 / 技能特长 0.5
     */
    @Override
    public Map<String, Object> creditStat(Long studentId) {
        Long sid = studentId;
        if (sid == null) {
            sid = requireStudentId();
        }

        List<EcEnroll> all = enrollMapper.selectList(Wrappers.<EcEnroll>lambdaQuery()
                .eq(EcEnroll::getStudentId, sid)
                .eq(EcEnroll::getStatus, 2));

        Set<Long> aids = all.stream().map(EcEnroll::getActivityId).collect(Collectors.toSet());
        Map<Long, EcActivity> acts = aids.isEmpty() ? Map.of()
                : activityMapper.selectBatchIds(aids).stream()
                .collect(Collectors.toMap(EcActivity::getId, a -> a));

        // 七个模块：key / 名称 / 要求学分 / 累计 / 活动数
        LinkedHashMap<String, Map<String, Object>> modules = new LinkedHashMap<>();
        String[][] defs = {
                {"thought", "思想成长", "1.0"},
                {"practice", "实践实习", "1.0"},
                {"volunteer", "志愿公益", "1.0"},
                {"innovation", "创新创业", "1.0"},
                {"culture", "文体活动", "0.5"},
                {"work", "工作履历", "0.5"},
                {"skill", "技能特长", "0.5"},
        };
        for (String[] d : defs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", d[0]);
            m.put("name", d[1]);
            m.put("required", Double.parseDouble(d[2]));
            m.put("credit", 0.0);
            m.put("count", 0);
            modules.put(d[0], m);
        }

        double total = 0;
        for (EcActivity a : acts.values()) {
            String key = moduleKey(a.getCategory());
            Map<String, Object> m = modules.get(key);
            if (m == null) {
                continue;
            }
            double c = a.getCredit() == null ? 0 : a.getCredit().doubleValue();
            m.put("credit", (double) m.get("credit") + c);
            m.put("count", (int) m.get("count") + 1);
            total += c;
        }

        // 计算每模块是否达标
        double requiredTotal = 0;
        List<Map<String, Object>> moduleList = new ArrayList<>();
        for (Map<String, Object> m : modules.values()) {
            double credit = (double) m.get("credit");
            double required = (double) m.get("required");
            requiredTotal += required;
            m.put("achieved", credit >= required);
            m.put("percent", required <= 0 ? 100
                    : (int) Math.min(100, Math.round(credit / required * 100)));
            moduleList.add(m);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("studentId", sid);
        out.put("totalCredit", Math.round(total * 10) / 10.0);
        out.put("requiredCredit", requiredTotal);
        out.put("activityCount", acts.size());
        out.put("achieved", total >= requiredTotal);
        out.put("modules", moduleList);

        // 兼容旧字段
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Map<String, Object> m : moduleList) {
            byCategory.put((String) m.get("name"),
                    BigDecimal.valueOf((double) m.get("credit")));
        }
        out.put("byCategory", byCategory);
        return out;
    }

    /** category → 七个模块 key */
    private String moduleKey(Integer category) {
        if (category == null) {
            return "skill";
        }
        return switch (category) {
            case 1 -> "thought";      // 讲座报告 → 思想成长
            case 2 -> "volunteer";    // 志愿服务 → 志愿公益
            case 3 -> "work";         // 社团活动 → 工作履历
            case 4 -> "innovation";   // 学科竞赛 → 创新创业
            case 5 -> "practice";     // 社会实践 → 实践实习
            case 6 -> "culture";      // 文体活动 → 文体活动
            default -> "skill";       // 其他 → 技能特长
        };
    }

    // ------------------------------------------------------------ 内部

    private Long requireStudentId() {
        LoginUser u = UserContext.require();
        if (u.getStudentId() == null) {
            throw new BizException("只有学生账号可以操作");
        }
        return u.getStudentId();
    }

    private List<EcEnrollVO> toVOList(List<EcEnroll> list, boolean withStudent) {
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> aids = list.stream().map(EcEnroll::getActivityId).collect(Collectors.toSet());
        Map<Long, EcActivity> acts = activityMapper.selectBatchIds(aids).stream()
                .collect(Collectors.toMap(EcActivity::getId, a -> a));

        Map<Long, Student> stuMap = Map.of();
        Map<Long, String> names = Map.of();
        if (withStudent) {
            Set<Long> sids = list.stream().map(EcEnroll::getStudentId).collect(Collectors.toSet());
            stuMap = studentMapper.selectBatchIds(sids).stream()
                    .collect(Collectors.toMap(Student::getId, s -> s));
            Set<Long> uids = stuMap.values().stream().map(Student::getUserId).collect(Collectors.toSet());
            Map<Long, String> n = new HashMap<>();
            if (!uids.isEmpty()) {
                for (SysUser u : userMapper.selectBatchIds(uids)) {
                    n.put(u.getId(), u.getNickname());
                }
            }
            names = n;
        }

        List<EcEnrollVO> out = new ArrayList<>();
        for (EcEnroll e : list) {
            EcEnrollVO vo = new EcEnrollVO();
            vo.setId(e.getId());
            vo.setActivityId(e.getActivityId());
            vo.setStudentId(e.getStudentId());
            vo.setEnrollTime(e.getEnrollTime());
            vo.setCheckinTime(e.getCheckinTime());
            vo.setStatus(e.getStatus());

            EcActivity a = acts.get(e.getActivityId());
            if (a != null) {
                vo.setActivityTitle(a.getTitle());
                vo.setCategory(a.getCategory());
                vo.setCategoryName(categoryName(a.getCategory()));
                vo.setCredit(a.getCredit());
                vo.setStartTime(a.getStartTime());
                vo.setEndTime(a.getEndTime());
                vo.setLocation(a.getLocation());
            }
            if (withStudent) {
                Student s = stuMap.get(e.getStudentId());
                if (s != null) {
                    vo.setStudentName(names.getOrDefault(s.getUserId(), "学生" + s.getId()));
                    vo.setStuNo(s.getStuNo());
                }
            }
            vo.setStatusName(switch (e.getStatus() == null ? 1 : e.getStatus()) {
                case 2 -> "已签到";
                case 3 -> "已取消";
                default -> "已报名";
            });
            out.add(vo);
        }
        return out;
    }

    private String categoryName(Integer c) {
        if (c == null) {
            return "其他";
        }
        return switch (c) {
            case 1 -> "讲座报告";
            case 2 -> "志愿服务";
            case 3 -> "社团活动";
            case 4 -> "学科竞赛";
            case 5 -> "社会实践";
            case 6 -> "文体活动";
            default -> "其他";
        };
    }
}
