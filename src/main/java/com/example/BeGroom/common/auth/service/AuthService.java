package com.example.BeGroom.common.auth.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.common.auth.dto.MemberLoginReqDto;

public interface AuthService {
    Member login(MemberLoginReqDto reqDto);
}
