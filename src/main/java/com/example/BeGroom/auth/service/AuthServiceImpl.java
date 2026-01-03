package com.example.BeGroom.auth.service;

import com.example.BeGroom.auth.dto.MemberLoginReqDto;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member memberLogin(MemberLoginReqDto reqDto) {
        Member member = memberRepository.findByEmail(reqDto.getEmail()).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        if(!passwordEncoder.matches(reqDto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("password가 일치하지 않습니다.");
        }
        return member;
    }

    // 판매자 로그인
    @Override
    public Seller sellerLogin(MemberLoginReqDto reqDto){
        Seller seller = sellerRepository.findByEmail(reqDto.getEmail()).orElseThrow(() -> new EntityNotFoundException("없는 판매자입니다."));

        if(!passwordEncoder.matches(reqDto.getPassword(), seller.getPassword())){
            throw new IllegalArgumentException("password가 일치하지 않습니다.");
        }
        return seller;
    }
}
