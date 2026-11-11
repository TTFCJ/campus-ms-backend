package com.campus.service;

import com.campus.dto.LoginDTO;
import com.campus.vo.LoginVO;
import com.campus.vo.RouteVO;
import com.campus.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface AuthService {

    /** 登录，返回 token */
    LoginVO login(LoginDTO dto, HttpServletRequest request);

    /** 退出登录（踢掉当前 token） */
    void logout(String token);

    /** 图形验证码 */
    Map<String, String> captcha();

    /** 当前登录用户信息 */
    UserInfoVO getUserInfo();

    /** 当前用户菜单路由树 */
    List<RouteVO> getRouters();

    /** 强制某用户下线 */
    void kickOut(Long userId);

    /** 修改自己的密码 */
    void changePassword(String oldPwd, String newPwd);
}
