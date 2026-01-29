package com.example.BeGroom.product.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryImageJsonGeneratorTest {

    @Autowired
    private CategoryImageJsonGenerator generator;

    @DisplayName("크롤링 데이터로 카테고리별 이미지 JSON 생성")
    @Test
    void generateCategoryImagesJson() throws IOException {
        // when
        generator.generateJson();

        // then
        System.out.println("JSON 파일 생성 완료");

    }

}