package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 高校综合管理系统 启动类
 */
@EnableAsync
@MapperScan("com.campus.mapper")
@SpringBootApplication
public class CampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
        System.out.println("""
                ============================================================
                  高校综合管理系统 启动成功
                  接口文档: http://localhost:8080/api/doc.html
                ============================================================
                """);
    }
}
