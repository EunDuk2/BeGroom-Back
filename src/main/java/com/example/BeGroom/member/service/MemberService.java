package com.example.BeGroom.member.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;

public interface MemberService {
    Member create(MemberCreateReqDto reqDto);
}
