package com.example.BeGroom.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "crawling")
@Data
public class CrawlingConfig {
    private List<Category> categories;

    @Data
    public static class Category {
        private Long id;
        private String name;
    }
}
