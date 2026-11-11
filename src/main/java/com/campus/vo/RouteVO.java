package com.campus.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端动态路由
 */
@Data
public class RouteVO {

    private Long id;
    private String path;
    private String name;
    private String component;
    private String redirect;
    private Meta meta;
    private List<RouteVO> children = new ArrayList<>();

    @Data
    public static class Meta {
        private String title;
        private String icon;
        private Integer sort;
        private Boolean hidden;
        private Boolean keepAlive;

        public Meta(String title, String icon, Integer sort, Boolean hidden, Boolean keepAlive) {
            this.title = title;
            this.icon = icon;
            this.sort = sort;
            this.hidden = hidden;
            this.keepAlive = keepAlive;
        }
    }
}
