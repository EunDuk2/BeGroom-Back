package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    // 부모 카테고리 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    // 자식 카테고리 목록
    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // ProductCategory 연관관계
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCategory> productCategories = new ArrayList<>();

    @Column(name = "external_category_id", nullable = false, length = 20)
    private String externalCategoryId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    @Builder.Default
    private CategoryType categoryType = CategoryType.BASIC;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 10;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;


    public enum CategoryType {
        BASIC, SEASON, EVENT, BEST, DISCOUNT
    }


    // 카테고리 기본 정보 수정
    public void updateCategoryInfo(String categoryName, Integer sortOrder) {
        validateCategoryName(categoryName);
        validateSortOrder(sortOrder);

        this.categoryName = categoryName;
        this.sortOrder = sortOrder;
    }

    // 카테고리 활성화
    public void activate() {
        this.isActive = true;
    }

    // 카테고리 비활성화
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

    // 유효성 검사
    private void validateCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다.");
        }
        if (categoryName.length() > 50) {
            throw new IllegalArgumentException("카테고리명은 50자를 초과할 수 없습니다.");
        }
    }

    private void validateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException("정렬 순서는 0 이상이어야 합니다.");
        }
    }
}