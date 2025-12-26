package com.example.BeGroom.auth.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.auth.dto.MemberLoginReqDto;

public interface AuthService {
    Member login(MemberLoginReqDto reqDto);
}
