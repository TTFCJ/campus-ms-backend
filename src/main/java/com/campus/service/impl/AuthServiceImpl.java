package com.campus.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.common.ResultCode;
import com.campus.common.constant.RedisKeys;
import com.campus.common.exception.BizException;
import com.campus.common.util.IpUtil;
import com.campus.common.util.JwtUtil;
import com.campus.common.util.RedisUtil;
import com.campus.dto.LoginDTO;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.security.LoginUser;
import com.campus.security.UserContext;
import com.campus.service.AuthService;
import com.campus.vo.LoginVO;
import com.campus.vo.RouteVO;
import com.campus.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final ClazzMapper clazzMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${campus.login.max-retry:5}")
    private int maxRetry;

    @Value("${campus.login.lock-minutes:10}")
    private int lockMinutes;

    @Value("${campus.login.captcha-enabled:true}")
    private boolean captchaEnabled;

    // ------------------------------------------------------------ 登录
    @Override
    public LoginVO login(LoginDTO dto, HttpServletRequest request) {
        String ip = IpUtil.getIp(request);
        String username = dto.getUsername().trim();

        // 1. 验证码
        if (captchaEnabled) {
            checkCaptcha(dto);
        }

        // 2. 锁定校验
        String retryKey = RedisKeys.LOGIN_RETRY + username;
        long retry = toLong(redisUtil.get(retryKey));
        if (retry >= maxRetry) {
            long ttl = Optional.ofNullable(redisUtil.getExpire(retryKey)).orElse((long) lockMinutes * 60);
            throw new BizException(ResultCode.USER_LOCKED.getCode(),
                    "账号已锁定，请 " + Math.max(ttl / 60, 1) + " 分钟后重试");
        }

        // 3. 用户校验
        SysUser user = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (user == null) {
            recordFail(username, ip, "账号不存在");
            increaseRetry(retryKey);
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            recordFail(username, ip, "账号已禁用");
            throw new BizException(ResultCode.USER_DISABLED);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordFail(username, ip, "密码错误");
            increaseRetry(retryKey);
            long left = maxRetry - toLong(redisUtil.get(retryKey));
            throw new BizException(ResultCode.PASSWORD_ERROR.getCode(),
                    left > 0 ? "账号或密码错误，还剩 " + left + " 次机会" : ResultCode.PASSWORD_ERROR.getMsg());
        }

        // 4. 登录成功，清空失败计数
        redisUtil.delete(retryKey);

        // 5. 构建登录态
        LoginUser loginUser = buildLoginUser(user);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setLoginIp(ip);

        String token = jwtUtil.createToken(user.getId(), user.getUsername());
        long seconds = jwtUtil.getExpireSeconds();
        redisUtil.set(RedisKeys.LOGIN_TOKEN + token, loginUser, seconds);
        redisUtil.sAdd(RedisKeys.USER_TOKENS + user.getId(), token);

        // 6. 更新最后登录时间 + 记录日志
        SysUser upd = new SysUser();
        upd.setId(user.getId());
        upd.setLastLoginTime(LocalDateTime.now());
        upd.setLastLoginIp(ip);
        userMapper.updateById(upd);
        recordSuccess(username, ip, request);

        log.info("[登录成功] user={} ip={}", username, ip);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(seconds);
        return vo;
    }

    // ------------------------------------------------------------ 退出
    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        LoginUser lu = redisUtil.get(RedisKeys.LOGIN_TOKEN + token);
        redisUtil.delete(RedisKeys.LOGIN_TOKEN + token);
        if (lu != null) {
            redisUtil.sRemove(RedisKeys.USER_TOKENS + lu.getUserId(), token);
        }
    }

    // ------------------------------------------------------------ 验证码
    @Override
    public Map<String, String> captcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 30);
        String key = UUID.randomUUID().toString().replace("-", "");
        redisUtil.set(RedisKeys.CAPTCHA + key, captcha.getCode(), 120);
        Map<String, String> map = new HashMap<>(4);
        map.put("captchaKey", key);
        map.put("captchaImage", captcha.getImageBase64Data());
        return map;
    }

    // ------------------------------------------------------------ 当前用户
    @Override
    public UserInfoVO getUserInfo() {
        LoginUser lu = UserContext.require();
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(lu.getUserId());
        vo.setUsername(lu.getUsername());
        vo.setNickname(lu.getNickname());
        vo.setAvatar(lu.getAvatar());
        vo.setUserType(lu.getUserType());
        vo.setDeptId(lu.getDeptId());
        vo.setStudentId(lu.getStudentId());
        vo.setTeacherId(lu.getTeacherId());
        vo.setClazzId(lu.getClazzId());
        vo.setRoles(lu.getRoles());
        vo.setPerms(lu.getPerms());
        vo.setDataScope(lu.getDataScope());
        vo.setCounselorClazzIds(lu.getCounselorClazzIds());

        if (lu.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(lu.getDeptId());
            vo.setDeptName(dept == null ? null : dept.getDeptName());
        }
        if (lu.getClazzId() != null) {
            Clazz clazz = clazzMapper.selectById(lu.getClazzId());
            vo.setClazzName(clazz == null ? null : clazz.getClazzName());
        }
        return vo;
    }

    // ------------------------------------------------------------ 路由
    @Override
    public List<RouteVO> getRouters() {
        LoginUser lu = UserContext.require();
        List<SysMenu> menus = Integer.valueOf(1).equals(lu.getUserType())
                ? menuMapper.selectAllMenus()
                : menuMapper.selectMenusByUserId(lu.getUserId());
        return buildRouteTree(menus, 0L, "");
    }

    // ------------------------------------------------------------ 强制下线
    @Override
    public void kickOut(Long userId) {
        String setKey = RedisKeys.USER_TOKENS + userId;
        Set<Object> tokens = redisUtil.get(setKey);
        if (tokens != null) {
            tokens.forEach(t -> redisUtil.delete(RedisKeys.LOGIN_TOKEN + t));
        }
        redisUtil.delete(setKey);
        log.info("[强制下线] userId={}", userId);
    }

    // ------------------------------------------------------------ 修改密码
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPwd, String newPwd) {
        LoginUser lu = UserContext.require();
        SysUser user = userMapper.selectById(lu.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BizException(ResultCode.OLD_PASSWORD_ERROR);
        }
        SysUser upd = new SysUser();
        upd.setId(user.getId());
        upd.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(upd);
        // 改密后踢下线，强制重新登录
        kickOut(user.getId());
    }

    // ============================================================ 私有方法

    private void checkCaptcha(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getCaptchaKey()) || !StringUtils.hasText(dto.getCaptcha())) {
            throw new BizException(ResultCode.CAPTCHA_ERROR.getCode(), "请输入验证码");
        }
        String key = RedisKeys.CAPTCHA + dto.getCaptchaKey();
        String code = redisUtil.get(key);
        redisUtil.delete(key);
        if (code == null) {
            throw new BizException(ResultCode.CAPTCHA_ERROR.getCode(), "验证码已失效，请刷新");
        }
        if (!code.equalsIgnoreCase(dto.getCaptcha().trim())) {
            throw new BizException(ResultCode.CAPTCHA_ERROR);
        }
    }

    private LoginUser buildLoginUser(SysUser user) {
        LoginUser lu = new LoginUser();
        lu.setUserId(user.getId());
        lu.setUsername(user.getUsername());
        lu.setNickname(user.getNickname());
        lu.setAvatar(user.getAvatar());
        lu.setUserType(user.getUserType());
        lu.setDeptId(user.getDeptId());

        // 角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        lu.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toSet()));
        // 数据范围取最宽（数值最小）
        lu.setDataScope(roles.stream()
                .map(SysRole::getDataScope)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(4));

        // 权限标识
        Set<String> perms = new HashSet<>(menuMapper.selectPermsByUserId(user.getId()));
        if (Integer.valueOf(1).equals(user.getUserType())) {
            perms.addAll(menuMapper.selectAllPerms());
        }
        lu.setPerms(perms);

        // 学生 / 教师 / 辅导员身份
        Student stu = studentMapper.selectByUserId(user.getId());
        if (stu != null) {
            lu.setStudentId(stu.getId());
            lu.setClazzId(stu.getClazzId());
        }
        Teacher tea = teacherMapper.selectByUserId(user.getId());
        if (tea != null) {
            lu.setTeacherId(tea.getId());
        }
        List<Long> counselorClazzIds = clazzMapper.selectIdsByCounselorId(user.getId());
        if (!counselorClazzIds.isEmpty()) {
            lu.setCounselorClazzIds(counselorClazzIds);
            if (lu.getClazzId() == null) {
                lu.setClazzId(counselorClazzIds.get(0));
            }
        }
        return lu;
    }

    /** 菜单 -> 前端路由树 */
    private List<RouteVO> buildRouteTree(List<SysMenu> menus, Long parentId, String parentPath) {
        List<RouteVO> list = new ArrayList<>();
        menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .sorted(Comparator.comparing(m -> Optional.ofNullable(m.getSort()).orElse(0)))
                .forEach(m -> {
                    RouteVO r = new RouteVO();
                    r.setId(m.getId());
                    String childPath = StringUtils.hasText(m.getPath()) ? m.getPath() : "";
                    String fullPath = childPath.startsWith("/") ? childPath
                            : (parentPath + "/" + childPath).replaceAll("/+", "/");
                    r.setPath(fullPath);
                    r.setName(toRouteName(fullPath));
                    r.setComponent("M".equals(m.getMenuType()) ? "Layout" : m.getComponent());
                    r.setMeta(new RouteVO.Meta(m.getMenuName(), m.getIcon(),
                            m.getSort(), Integer.valueOf(0).equals(m.getVisible()) ? false : true,
                            Integer.valueOf(1).equals(m.getKeepAlive())));
                    List<RouteVO> children = buildRouteTree(menus, m.getId(), fullPath);
                    r.setChildren(children);
                    if ("M".equals(m.getMenuType()) && !children.isEmpty()) {
                        r.setRedirect(fullPath + "/" + children.get(0).getPath()
                                .substring(children.get(0).getPath().lastIndexOf('/') + 1));
                    }
                    list.add(r);
                });
        return list;
    }

    private String toRouteName(String path) {
        StringBuilder sb = new StringBuilder();
        for (String seg : path.split("/")) {
            if (seg.isBlank()) {
                continue;
            }
            sb.append(Character.toUpperCase(seg.charAt(0))).append(seg.substring(1));
        }
        return sb.toString();
    }

    private void increaseRetry(String key) {
        long v = redisUtil.increment(key, 1);
        if (v == 1) {
            redisUtil.expire(key, (long) lockMinutes * 60);
        }
    }

    private long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void recordSuccess(String username, String ip, HttpServletRequest request) {
        SysLoginLog lg = new SysLoginLog();
        lg.setUsername(username);
        lg.setIp(ip);
        lg.setBrowser(IpUtil.getBrowser(request));
        lg.setOs(IpUtil.getOs(request));
        lg.setLoginTime(LocalDateTime.now());
        lg.setStatus(1);
        lg.setMsg("登录成功");
        loginLogMapper.insert(lg);
    }

    private void recordFail(String username, String ip, String msg) {
        SysLoginLog lg = new SysLoginLog();
        lg.setUsername(username);
        lg.setIp(ip);
        lg.setLoginTime(LocalDateTime.now());
        lg.setStatus(0);
        lg.setMsg(msg);
        loginLogMapper.insert(lg);
    }
}
