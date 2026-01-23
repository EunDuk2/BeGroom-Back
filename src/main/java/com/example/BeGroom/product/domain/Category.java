package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
@Entity
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String externalCategoryId;

    // 부모 카테고리 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // 자식 카테고리 목록
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Column(nullable = false, length = 50)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType categoryType;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean isActive;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder
    private Category(String externalCategoryId, Category parent, String categoryName, CategoryType categoryType, Integer sortOrder, Boolean isActive, LocalDateTime startDate, LocalDateTime endDate) {
        validateCategoryName(categoryName);
        Assert.hasText(externalCategoryId, "외부 카테고리 ID가 필요합니다.");

        this.externalCategoryId = externalCategoryId;
        this.categoryName = categoryName;
        this.categoryType = (categoryType != null) ? categoryType : CategoryType.BASIC;
        this.sortOrder = (sortOrder != null) ? sortOrder : 10;
        this.isActive = (isActive != null) ? isActive : true;
        this.startDate = startDate;
        this.endDate = endDate;

        setParent(parent);
    }

    // 카테고리 기본 정보 수정
    public void updateCategoryInfo(String categoryName, Integer sortOrder) {
        validateCategoryName(categoryName);
        if (sortOrder != null && sortOrder < 0) throw new IllegalArgumentException("정렬 순서는 0 이상이어야 합니다.");

        this.categoryName = categoryName;
        this.sortOrder = sortOrder;
    }

    public void activate() {
        this.isActive = true;
    }
    public void deactivate() {
        this.isActive = false;
    }

    // 노출 기간 설정
    public void updatePeriod(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
        }
        this.startDate = start;
        this.endDate = end;
    }

    public void setParent(Category parent) {
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }

        this.parent = parent;
        if (parent != null) {
            if (!parent.getChildren().contains(this)) {
                parent.getChildren().add(this);
            }
            this.level = parent.getLevel() + 1;
        } else {
            this.level = 1;
        }
    }

    // 유효성 검사
    private void validateCategoryName(String name) {
        Assert.hasText(name, "카테고리명은 필수입니다.");
        if (name.length() > 50) {
            throw new IllegalArgumentException("카테고리명은 50자를 초과할 수 없습니다.");
        }
    }
}