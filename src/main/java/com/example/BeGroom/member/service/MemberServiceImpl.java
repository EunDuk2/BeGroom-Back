package com.example.BeGroom.member.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.MemberCreateReqDto;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.wallet.service.WalletService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Override
    public Member create(MemberCreateReqDto reqDto) {
        // 존재 유무 검증
        if(memberRepository.findByEmail(reqDto.getEmail()).isPresent()) {
            throw new EntityExistsException("이미 존재하는 회원입니다.");
        }
        // 생성
        Member member = Member.createMember(
                reqDto.getEmail(),
                reqDto.getName(),
                passwordEncoder.encode(reqDto.getPassword()),
                reqDto.getPhoneNumber(),
                reqDto.getRole()
        );
        // 저장
        memberRepository.save(member);
        // 지갑 생성
        walletService.create(member);

        return member;
    }
}