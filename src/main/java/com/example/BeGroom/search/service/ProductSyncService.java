//package com.example.BeGroom.search.service;
//
//import com.example.BeGroom.product.domain.Product;
//import com.example.BeGroom.product.domain.ProductStatus;
//import com.example.BeGroom.product.repository.ProductRepository;
//import com.example.BeGroom.search.domain.ProductDocument;
//import com.example.BeGroom.search.repository.ProductSearchRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class ProductSyncService {
//
//    private final ProductRepository productRepository;
//    private final ProductSearchRepository searchRepository;
//
//    /**
//     * 전체 상품 초기 색
//     */
//    @Transactional(readOnly = true)
//    public void indexAllProducts() {
//        log.info("Starting full product indexing...");
//
//        int pageSize = 1000;
//        int pageNumber = 0;
//        long totalIndexed = 0;
//
//        while(true) {
//            Pageable pageable = PageRequest.of(pageNumber, pageSize);
//            Page<Product> productPage = productRepository.findAll(pageable);
//
//            if (productPage.isEmpty()) break;
//
//            // Product -> ProductDocument
//            List<ProductDocument> documents = productPage.getContent().stream()
//                .filter(product -> product.getDeletedAt() == null)
//                .filter(product -> product.getProductStatus() != ProductStatus.STOP)
//                .map(ProductDocument::from)
//                .toList();
//
//            searchRepository.saveAll(documents);
//            totalIndexed += documents.size();
//
//            log.info("Indexed page {}: {} products", pageNumber, documents.size());
//            pageNumber++;
//        }
//
//        log.info("Full indexing completed. Total indexed: {} products", totalIndexed);
//    }
//
//    /**
//     * 단일 상품 색인 - 상품 생성/수정 시 호출
//     */
//    public void indexProduct(Long productId) {
//        Product product = productRepository.findById(productId)
//            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
//
//        // 삭제/판매중지 상품은 ElasticSearch에서 삭제
//        if (product.getDeletedAt() != null || product.getProductStatus() == ProductStatus.STOP) {
//            deleteProduct(productId);
//            return;
//        }
//
//        ProductDocument document = ProductDocument.from(product);
//        searchRepository.save(document);
//        log.info("Indexed product: {}", productId);
//    }
//
//    /**
//     * 단일 상품 삭제 - 상품 삭제 시 호출
//     */
//    public void deleteProduct(Long productId) {
//        searchRepository.deleteById(String.valueOf(productId));
//        log.info("Deleted product from index: {}", productId);
//    }
//
//    /**
//     * 전체 인덱스 삭제 - 재색인 전 기존 데이터 정리
//     */
//    public void deleteAllProducts() {
//        searchRepository.deleteAll();
//        log.info("Deleted all products from index");
//    }
//
//    /**
//     * 전체 재색인 - 기존 인덱스 삭제 후 전체 색인
//     */
//    @Transactional(readOnly = true)
//    public void reindexAllProducts() {
//        log.info("Starting full reindexing...");
//        deleteAllProducts();
//        indexAllProducts();
//        log.info("Full reindexing completed");
//    }
//}
