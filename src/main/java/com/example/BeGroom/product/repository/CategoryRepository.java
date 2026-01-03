package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByLevelAndIsActiveOrderBySortOrderAsc(Integer level, Boolean isActive);
    List<Category> findByParentIdAndIsActiveOrderBySortOrderAsc(Long parentId, Boolean isActive);
}