package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Brand;
import com.example.BeGroom.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByBrandCode(Long brandCode);

    Optional<Brand> findBySellerAndBrandCode(Seller seller, Long brandCode);
}