package com.example.BeGroom.product.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DummyDataGeneratorTest {

    @Autowired
    private DummyDataGenerator dummyDataGenerator;
    
    @DisplayName("사용자는 테스트를 위해 100개의 상품 더미 데이터를 생성할 수 있다.")
    @Test
    @Commit
    void seedData() {
        // given
        int productCount = 50;
        
        // when
        dummyDataGenerator.seedAll(productCount);
        
        // then
        System.out.println("✅ " + productCount + "개의 데이터 시딩 완료!");
        
    }

}