package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    public void updateBrandInfo(String logoUrl, String description) {
        this.logoUrl = logoUrl;
        this.description = description;
    }
}