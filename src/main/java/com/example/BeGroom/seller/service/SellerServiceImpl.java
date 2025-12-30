package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.SellerCreateReqDto;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService{

    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Seller create(SellerCreateReqDto sellerCreateReqDto) {
        if(sellerRepository.findByEmail(sellerCreateReqDto.getEmail()).isPresent()) {
            throw new EntityExistsException("이미 존재하는 판매자입니다.");
        }

        Seller seller = Seller.createSeller(
                sellerCreateReqDto.getEmail(),
                sellerCreateReqDto.getName(),
                passwordEncoder.encode(sellerCreateReqDto.getPassword()),
                sellerCreateReqDto.getPhoneNumber()
        );

        sellerRepository.save(seller);

        return seller;
    }

}
