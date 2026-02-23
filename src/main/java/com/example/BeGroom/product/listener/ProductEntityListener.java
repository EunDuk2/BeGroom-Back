//package com.example.BeGroom.product.listener;
//
//import com.example.BeGroom.product.domain.Product;
//import com.example.BeGroom.search.service.ProductSyncService;
//import jakarta.persistence.PostPersist;
//import jakarta.persistence.PostRemove;
//import jakarta.persistence.PostUpdate;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class ProductEntityListener {
//
//    private static ProductSyncService syncService;
//
//    /**
//     * Spring Bean 주입 (static 별도)
//     * EntityListener는 JPA가 직접 생성하므로 static으로 주입 필요
//     */
//    @Autowired
//    public void init(ProductSyncService syncService) {
//        ProductEntityListener.syncService = syncService;
//    }
//
//    /**
//     * 상품 생성 후 실행
//     */
//    @PostPersist
//    public void onPostPersist(Product product) {
//        log.info("Product created: {}", product.getId());
//        if (syncService != null) {
//            try {
//                syncService.indexProduct(product.getId());
//            } catch (Exception e) {
//                log.error("Failed to index product: {}", product.getId(), e);
//            }
//        }
//    }
//
//    /**
//     * 상품 수정 후 실행
//     */
//    @PostUpdate
//    public void onPostUpdate(Product product) {
//        log.info("Product updated: {}", product.getId());
//        if (syncService != null) {
//            try {
//                syncService.indexProduct(product.getId());
//            } catch (Exception e) {
//                log.error("Failed to update product index: {}", product.getId(), e);
//            }
//        }
//    }
//
//    /**
//     * 상품 삭제 후 실행
//     */
//    @PostRemove
//    public void onPostRemove(Product product) {
//        log.info("Product removed: {}", product.getId());
//        if (syncService != null) {
//            try {
//                syncService.deleteProduct(product.getId());
//            } catch (Exception e) {
//                log.error("Failed to delete product from index: {}", product.getId(), e);
//            }
//        }
//    }
//}
