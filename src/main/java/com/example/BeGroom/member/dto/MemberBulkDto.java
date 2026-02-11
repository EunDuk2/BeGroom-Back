package com.example.BeGroom.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberBulkDto {
    private String email;
    private String name;
    private String phoneNumber;
}
