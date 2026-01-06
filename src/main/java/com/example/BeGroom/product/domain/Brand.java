package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "brand")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "brand_code", unique = true)
    private Long brandCode;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>();


    // 브랜드 기본 정보 수정
    public void updateBrandInfo(String logoUrl, String description) {
        this.logoUrl = logoUrl;
        this.description = description;
    }

    // 브랜드 이름 변경
    public void changeName(String name) {
        validateName(name);
        this.name = name;
    }

    // 이름 유효성 검사
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("브랜드 이름은 필수이며 공백일 수 없습니다.");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("브랜드 이름은 100자를 초과할 수 없습니다.");
        }
    }

    // 연관관계 편의 메서드
    public void addProduct(Product product) {
        this.products.add(product);
        product.setBrand(this);
    }
}