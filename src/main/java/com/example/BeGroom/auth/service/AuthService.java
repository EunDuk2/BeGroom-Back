package com.example.BeGroom.auth.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.auth.dto.MemberLoginReqDto;
import com.example.BeGroom.seller.domain.Seller;

public interface AuthService {
    Member memberLogin(MemberLoginReqDto reqDto);
    Seller sellerLogin(MemberLoginReqDto reqDto);
}
