package com.example.BeGroom.common.auth.service;

import com.example.BeGroom.common.auth.dto.MemberLoginReqDto;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member login(MemberLoginReqDto reqDto) {
        Member member = memberRepository.findByEmail(reqDto.getEmail()).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        if(!passwordEncoder.matches(reqDto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("password가 일치하지 않습니다.");
        }
        return member;
    }
}
