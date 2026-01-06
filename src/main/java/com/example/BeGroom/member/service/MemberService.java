package com.example.BeGroom.member.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    Member create(MemberCreateReqDto reqDto);
    MemberGetProfileResDto getMyProfile(String email);
    GetMemberOrdersResDto getMyOrders(Long memberId);
//  GetMemberWishesResDto getMyWishes(Long memberId);
    public GetMemberWalletResDto getWalletTransactions(Long memberId, Pageable pageable);
}