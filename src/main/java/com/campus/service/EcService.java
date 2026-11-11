package com.campus.service;

import com.campus.common.PageResult;
import com.campus.entity.EcActivity;
import com.campus.vo.EcEnrollVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EcService {

    PageResult<EcActivity> activityPage(Integer status, Integer category,
                                        String keyword, Long current, Long size);

    /** 学生端：报名中的活动 */
    List<EcActivity> openActivities();

    EcActivity activityDetail(Long id);

    void addActivity(EcActivity body);

    void updateActivity(EcActivity body);

    void publish(Long id);

    void removeActivity(Long id);

    /** 学生：报名 */
    void enroll(Long activityId);

    /** 学生：取消报名 */
    void cancel(Long activityId);

    /** 管理端：给某人签到 */
    void checkin(Long activityId, Long studentId);

    /** 我的报名 */
    List<EcEnrollVO> myEnrolls();

    /** 某活动的报名名单（管理端） */
    List<EcEnrollVO> activityEnrolls(Long activityId);

    /** 学分统计 */
    Map<String, Object> creditStat(Long studentId);
}
