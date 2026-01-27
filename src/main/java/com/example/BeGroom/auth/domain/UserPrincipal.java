package com.example.BeGroom.auth.domain;

import lombok.Builder;

public class UserPrincipal {
    private final Long memberId;
    private final String email;

    public UserPrincipal(Long memberId, String email) {
        this.memberId = memberId;
        this.email = email;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }
}

