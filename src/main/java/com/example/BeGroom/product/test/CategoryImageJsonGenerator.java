package com.example.BeGroom.product.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryImageJsonGenerator {

    private final JdbcTemplate jdbcTemplate;

    public void generateJson() throws IOException {
        log.info("=== 카테고리 이미지 JSON 생성 시작 ===");

        String sql = """
            SELECT
                c.id AS category_id,
                c.category_name,
                JSON_ARRAYAGG(pi.image_url) AS image_urls
            FROM category c
            JOIN product_category pc ON c.id = pc.category_id
            JOIN product p ON pc.product_id = p.id
            JOIN product_image pi ON p.id = pi.product_id
            WHERE p.id BETWEEN 1000001 AND 1003313
                AND c.level = 2
                AND pi.image_url IS NOT NULL
            GROUP BY c.id, c.category_name
            ORDER BY c.id;
            """;

        Map<String, List<String>> categoryImageMap = new LinkedHashMap<>();

        jdbcTemplate.query(sql, rs -> {
            String categoryId = String.valueOf(rs.getLong("category_id"));
            String imageUrlsJson = rs.getString("image_urls");

            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> imageUrls = mapper.readValue(
                    imageUrlsJson,
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                categoryImageMap.put(categoryId, imageUrls);
            } catch (Exception e) {
                log.error("카테고리 {} 파싱 실패: {}", categoryId, e.getMessage());
            }
        });

        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File("src/main/resources/category_images.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, categoryImageMap);

        log.info("JSON 파일 생성 완료");
        log.info("총 카테고리 수: {}", categoryImageMap.size());

        categoryImageMap.forEach((categoryId, urls) -> {
            log.info("  카테고리 {}: {}개 이미지", categoryId, urls.size());
        });
    }
}
