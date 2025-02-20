package com.ruoyi.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /videos/** 到实际存储的视频目录
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:D:/WHLProject/创业园项目最新/新建文件夹/chuangyeyuan/RuoYi-Vue-master/uploads/videos/");
    }
}

