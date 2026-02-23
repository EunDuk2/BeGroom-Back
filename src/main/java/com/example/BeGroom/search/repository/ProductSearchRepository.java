//package com.example.BeGroom.search.repository;
//
//import com.example.BeGroom.search.domain.ProductDocument;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
//
//    Page<ProductDocument> findByName(String name, Pageable pageable);
//    Page<ProductDocument> findByBrandId(Long brandId, Pageable pageable);
//    Page<ProductDocument> findByCategoryIdsContaining(Long categoryId, Pageable pageable);
//    Page<ProductDocument> findByIsSoldOut(Boolean isSoldOut, Pageable pageable);
//    Page<ProductDocument> findByDeliveryTypesContaining(String deliveryType, Pageable pageable);
//    Page<ProductDocument> findByPackagingTypesContaining(String packagingType, Pageable pageable);
//}
