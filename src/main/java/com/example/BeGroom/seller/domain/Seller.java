package com.example.BeGroom.seller.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.settlement.domain.Settlement;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate = BigDecimal.valueOf(10.00);

    @Column(nullable = false)
    private Integer payoutDay = 10;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.SELLER;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Brand> brands = new ArrayList<>();

    private Seller(String email, String name, String password, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public static Seller createSeller(String email, String name, String password, String phoneNumber) {
        return new Seller(email, name, password, phoneNumber);
    }

    public void addBrand(Brand brand) {
        this.brands.add(brand);
        brand.setSeller(this);
    }

}
