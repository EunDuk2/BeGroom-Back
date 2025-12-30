package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.SellerCreateReqDto;

public interface SellerService {

    Seller create(SellerCreateReqDto sellerCreateReqDto);

}
