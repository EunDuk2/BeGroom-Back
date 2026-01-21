package com.example.BeGroom.member.domain;

import com.example.BeGroom.cart.domain.Cart;
import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.wishlist.domain.Wishlist;
import com.fasterxml.classmate.AnnotationOverrides;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.example.BeGroom.member.domain.Role.USER;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wishlist> wishlists = new ArrayList<>();

    private Member(String email, String name, String password, String phoneNumber, Role role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public static Member createMember(String email, String name, String password, String phoneNumber, Role role) {
        return new Member(email, name, password, phoneNumber, role);
    }

    @Builder
    private Member(String email, String name, String password, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = USER;
    }

    // 연관관계 편의 메서드
    public void assignCart(Cart cart) {
        this.cart = cart;
    }
}
