package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "brand")
@Entity
public class Brand extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(unique = true, nullable = false)
    private Long brandCode;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @Builder
    private Brand(Seller seller, Long brandCode, String name, String logoUrl, String description) {
        Assert.notNull(seller, "판매자 정보는 필수입니다.");
        Assert.notNull(brandCode, "브랜드 코드는 필수입니다.");
        Assert.hasText(name, "브랜드 이름은 필수입니다.");

        this.brandCode = brandCode;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;

        assignSeller(seller);
    }

    public void assignSeller(Seller seller) {
        if (this.seller != null) {
            this.seller.getBrands().remove(this);
        }

        this.seller = seller;

        if (seller != null && !seller.getBrands().contains(this)) {
            seller.getBrands().add(this);
        }
    }

    public void addProduct(Product product) {
        if (product != null && product.getBrand() != this) {
            product.updateBrand(this);
        }
    }

    public void updateInfo(String name, String logoUrl, String description) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
    }
}