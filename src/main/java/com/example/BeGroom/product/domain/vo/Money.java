package com.example.BeGroom.product.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {

    @Column(nullable = false)
    private Integer amount;

    private Money(Integer amount) {
        Assert.notNull(amount, "금액은 필수입니다.");
        Assert.isTrue(amount >= 0, "금액은 0원 이상이어야 합니다.");

        this.amount = amount;
    }

    public static Money of(Integer amount) {
        return new Money(amount);
    }

    public static Money zero() {
        return new Money(0);
    }

    public int calculateDiscountRate(Money discountedPrice) {
        if (discountedPrice == null || this.amount == 0) {
            return 0;
        }

        return (int) (((double) (this.amount - discountedPrice.amount) / this.amount) * 100);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }
}
