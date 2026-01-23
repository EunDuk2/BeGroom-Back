package com.example.BeGroom.product.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option")
@Entity
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String optionType;

    @Column(nullable = false, length = 100)
    private String optionValue;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder
    private ProductOption(String optionType, String optionValue, String displayName, Boolean isActive) {
        Assert.hasText(optionType, "옵션 타입은 필수입니다.");
        Assert.hasText(optionValue, "옵션 값은 필수입니다.");
        validateDisplayName(displayName);

        this.optionType = optionType;
        this.optionValue = optionValue;
        this.displayName = displayName;
        this.isActive = (isActive != null) ? isActive : true;
    }

    public void updateDisplayName(String displayName) {
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
        Assert.hasText(displayName, "옵션 표시명은 필수입니다.");
        if (displayName.length() > 100) {
            throw new IllegalArgumentException("옵션 표시명은 100자를 초과할 수 없습니다.");
        }
    }

}