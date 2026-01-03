package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    Optional<ProductOption> findByOptionTypeAndOptionValue(String optionType, String optionValue);
}

