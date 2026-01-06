package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @Column(name = "option_type", nullable = false, length = 50)
    private String optionType;

    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    // 옵션 정보 수정
    public void updateOptionInfo(String displayName) {
        validateDisplayName(displayName);
        this.displayName = displayName;
    }

    // 옵션 활성화
    public void activate() {
        this.isActive = true;
    }

    // 옵션 비활성화
    public void deactivate() {
        this.isActive = false;
    }

    // 유효성 검사
    private void validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("옵션 표시명은 필수입니다.");
        }
        if (displayName.length() > 100) {
            throw new IllegalArgumentException("옵션 표시명은 100자를 초과할 수 없습니다.");
        }
    }

}