package com.example.BeGroom.member.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.member.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member create(MemberCreateReqDto reqDto) {
        if(memberRepository.findByEmail(reqDto.getEmail()).isPresent()) {
            throw new EntityExistsException("이미 존재하는 회원입니다.");
        }

        Member member = Member.createMember(
                reqDto.getEmail(),
                reqDto.getName(),
                passwordEncoder.encode(reqDto.getPassword()),
                reqDto.getPhoneNumber(),
                reqDto.getRole()
        );

        memberRepository.save(member);

        return member;
    }
}