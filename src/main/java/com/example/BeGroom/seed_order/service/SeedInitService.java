package com.example.BeGroom.seed_order.service;

import com.example.BeGroom.common.security.JwtTokenProvider;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.repository.BrandRepository;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seed_order.dto.SeedInitRequest;
import com.example.BeGroom.seed_order.dto.SeedInitResponse;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.wallet.domain.Wallet;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SeedInitService {

    private final EntityManager em;
    private final JwtTokenProvider jwtTokenProvider;

    private final SellerRepository sellerRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    // --- base (재사용용) ---
    private final String baseSellerEmail = "seed-seller@test.com";
    private final String baseSellerName = "seedSeller";
    private final String baseSellerPassword = "1234";
    private final String baseSellerPhone = "01012341234";

    private final long baseBrandCode = 999L;
    private final String baseBrandName = "seed-brand";

    // -------------------------
    // Public APIs
    // -------------------------

    /**
     * JSON 응답으로 usersCsv / productDetailsCsv 를 분리해서 반환
     */
    @Transactional
    public SeedInitResponse init(SeedInitRequest req) {
        String runId = normalizeRunId(req.runId());

        int memberCount = Math.max(0, req.memberCount());
        long walletBalance = req.walletBalance();

        int detailTarget = Math.max(0, req.productDetailCount());
        int detailsPerProduct = req.detailsPerProduct() <= 0 ? 10 : req.detailsPerProduct();

        int initialStock = Math.max(0, req.initialStock());
        int priceMin = Math.max(0, req.priceMin());
        int priceMax = Math.max(priceMin, req.priceMax());

        // 1) seller/brand 준비(재사용 권장)
        Catalog catalog = ensureCatalog(req.reuseSellerBrand());

        // 2) 멤버+지갑 생성 + users.csv 만들기
        String usersCsv = createMembersAndWalletsCsv(runId, memberCount, walletBalance);

        // 3) 상품/옵션 생성 + product-details.csv 만들기
        String pdCsv = createProductsAndDetailsCsv(
                runId,
                catalog,
                detailTarget,
                detailsPerProduct,
                initialStock,
                priceMin,
                priceMax
        );

        return new SeedInitResponse(
                runId,
                memberCount,
                detailTarget,
                usersCsv,
                pdCsv
        );
    }

    /**
     * ✅ k6에서 그대로 orders.csv 로 붙여넣을 수 있는 단일 CSV 문자열 반환
     *
     * 포맷:
     * memberId,token
     * 1,xxx
     * 2,yyy
     *
     * productDetailId
     * 10
     * 11
     * ...
     */
    @Transactional
    public String initAsOrdersCsv(SeedInitRequest req) {
        SeedInitResponse res = init(req);
        return toOrdersCsv(res.usersCsv(), res.productDetailsCsv());
    }

    // -------------------------
    // CSV builders
    // -------------------------

    /**
     * usersCsv + (blank line) + productDetailsCsv 로 결합
     * - k6 파서에서 "memberId,token" / "productDetailId" 헤더를 찾아 섹션을 나누는 방식과 호환
     */
    private String toOrdersCsv(String usersCsv, String productDetailsCsv) {
        String u = (usersCsv == null) ? "" : usersCsv.strip();
        String p = (productDetailsCsv == null) ? "" : productDetailsCsv.strip();

        // usersCsv 또는 productDetailsCsv 가 비어도 헤더는 존재하도록 보장(안전장치)
        if (u.isEmpty()) u = "memberId,token";
        if (p.isEmpty()) p = "productDetailId";

        StringBuilder sb = new StringBuilder(u.length() + p.length() + 16);
        sb.append(u).append("\n\n").append(p).append("\n");
        return sb.toString();
    }

    // -------------------------
    // members + wallets
    // -------------------------
    private String createMembersAndWalletsCsv(String runId, int memberCount, long walletBalance) {
        StringBuilder sb = new StringBuilder();
        sb.append("memberId,token\n");

        if (memberCount == 0) return sb.toString();

        final int CHUNK = 500;

        for (int base = 0; base < memberCount; base += CHUNK) {
            int end = Math.min(memberCount, base + CHUNK);

            List<Member> members = new ArrayList<>(end - base);

            for (int i = base; i < end; i++) {
                // ✅ 의도: "테스트 유저 집합"을 누적 생성/재사용할 수 있게 runId를 email에 섞지 않음
                // 필요하면 runId를 email에 섞어서 run 격리도 가능
                String email = "seed-user-" + String.format("%06d", i) + "@test.com";
                String name = "seed-user-" + i;

                Member m = Member.createMember(email, name, "1234", "010", Role.USER);
                em.persist(m);
                members.add(m);
            }

            em.flush(); // member id 확보

            for (Member m : members) {
                Wallet w = Wallet.create(m, walletBalance);
                em.persist(w);

                // token은 DB 저장 없이 CSV에만 포함
                String token = jwtTokenProvider.createToken(m.getId(), m.getEmail(), "USER");
                sb.append(m.getId()).append(",").append(token).append("\n");
            }

            em.flush();
            em.clear();
        }

        return sb.toString();
    }

    // -------------------------
    // products + details
    // -------------------------
    private String createProductsAndDetailsCsv(
            String runId,
            Catalog catalog,
            int detailTarget,
            int detailsPerProduct,
            int initialStock,
            int priceMin,
            int priceMax
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("productDetailId\n");

        if (detailTarget == 0) return sb.toString();

        int productCount = (int) Math.ceil(detailTarget / (double) detailsPerProduct);

        final int PRODUCT_CHUNK = 200;
        Random rnd = new Random(Objects.hash(runId, detailTarget, detailsPerProduct));

        for (int base = 0; base < productCount; base += PRODUCT_CHUNK) {
            int end = Math.min(productCount, base + PRODUCT_CHUNK);

            // 1) Product 생성
            List<Product> products = new ArrayList<>(end - base);
            for (int i = base; i < end; i++) {
                long productNo = 10_000_000L + i; // 유니크(Seed 1회 생성 전제)
                Product p = Product.builder()
                        .brand(catalog.brand())
                        .no(productNo)
                        .name("seed-product-" + i)
                        .productStatus(ProductStatus.SALE)
                        .build();
                em.persist(p);
                products.add(p);
            }
            em.flush(); // product id 확보

            // 2) ProductDetail 생성 (이번 chunk에서 만든 것만 모아 id 기록)
            List<ProductDetail> createdDetails = new ArrayList<>(products.size() * detailsPerProduct);

            for (int pi = 0; pi < products.size(); pi++) {
                Product p = products.get(pi);

                for (int dj = 0; dj < detailsPerProduct; dj++) {
                    int globalIndex = (base + pi) * detailsPerProduct + dj;
                    if (globalIndex >= detailTarget) break;

                    long detailNo = (p.getNo() * 100L) + dj; // 유니크
                    int price = priceMin + rnd.nextInt(Math.max(1, priceMax - priceMin + 1));

                    ProductDetail pd = ProductDetail.builder()
                            .product(p)
                            .no(detailNo)
                            .name("seed-detail-" + p.getNo() + "-" + dj)
                            .initialQuantity(initialStock)
                            .build();

                    // 가격 엔티티/VO 추가 로직 (프로젝트 구현에 맞게)
                    pd.addPrice(price, price);

                    em.persist(pd);
                    createdDetails.add(pd);
                }
            }

            em.flush(); // productDetail id 확보

            for (ProductDetail pd : createdDetails) {
                sb.append(pd.getId()).append("\n");
            }

            em.clear();
        }

        return sb.toString();
    }

    // -------------------------
    // seller/brand
    // -------------------------
    private Catalog ensureCatalog(boolean reuse) {
        if (reuse) {
            Seller seller = sellerRepository.findByEmail(baseSellerEmail)
                    .orElseGet(() -> sellerRepository.save(
                            Seller.createSeller(baseSellerEmail, baseSellerName, baseSellerPassword, baseSellerPhone)
                    ));

            Brand brand = brandRepository.findBySellerAndBrandCode(seller, baseBrandCode)
                    .orElseGet(() -> brandRepository.save(
                            Brand.builder()
                                    .seller(seller)
                                    .brandCode(baseBrandCode)
                                    .name(baseBrandName)
                                    .build()
                    ));

            return new Catalog(seller, brand);
        }

        // 재사용 안 하면 매번 새로(간단히 timestamp 섞기)
        String suffix = String.valueOf(System.currentTimeMillis());

        Seller seller = sellerRepository.save(
                Seller.createSeller(
                        "seed-seller-" + suffix + "@test.com",
                        "seedSeller-" + suffix,
                        "1234",
                        "01012341234"
                )
        );

        long brandCode = Long.parseLong(suffix.substring(Math.max(0, suffix.length() - 6)));
        Brand brand = brandRepository.save(
                Brand.builder()
                        .seller(seller)
                        .brandCode(brandCode)
                        .name("seed-brand-" + suffix)
                        .build()
        );

        return new Catalog(seller, brand);
    }

    // -------------------------
    // helpers
    // -------------------------
    private String normalizeRunId(String runId) {
        if (runId == null || runId.isBlank()) return UUID.randomUUID().toString();
        return runId;
    }

    private record Catalog(Seller seller, Brand brand) {}
}
