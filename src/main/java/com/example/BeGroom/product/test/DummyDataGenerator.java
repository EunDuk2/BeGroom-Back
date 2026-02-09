package com.example.BeGroom.product.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataGenerator {

    private final JdbcTemplate jdbcTemplate;

    // ThreadLocal Faker로 동시성 문제 해결
    private static final ThreadLocal<Faker> FAKER_THREAD_LOCAL =
        ThreadLocal.withInitial(() -> new Faker(new Locale("ko")));

    private PerformanceMonitor monitor;
    public void setPerformanceMonitor(PerformanceMonitor monitor) {
        this.monitor = monitor;
    }

    private record CategoryInfo(Long id, String name, Long parentId) {}
    private record ProductDetailBatch(Long productId, Long detailNo, String detailName) {}
    private record ProductInfo(Long id, String name, Long categoryId, Long brandId, String description) {}

    // 청크별 처리 결과 추적
    private record ChunkResult(int chunkIndex, int productsCreated, int detailsCreated, long durationMs, Throwable error) {
        boolean isSuccess() {
            return error == null;
        }
    }

    private static final Map<String, List<String>> ADJECTIVE_POOL = new HashMap<>();
    private static final List<String> KOREAN_BRAND_PREFIXES = new ArrayList<>();
    private static final List<String> KOREAN_BRAND_SUFFIXES = new ArrayList<>();
    private static final List<String> ENGLISH_BRAND_PREFIXES = new ArrayList<>();
    private static final List<String> ENGLISH_BRAND_SUFFIXES = new ArrayList<>();
    private static final Map<Long, List<String>> CATEGORY_IMAGE_URLS = new HashMap<>();

    private final AtomicLong brandCodeCounter = new AtomicLong(0);
    private final AtomicLong productNoCounter = new AtomicLong(0);
    private final AtomicLong detailNoCounter = new AtomicLong(0);

    static {
        loadCategoryImages();
        initializePools();
    }

    private static void initializePools() {

        KOREAN_BRAND_PREFIXES.addAll(Arrays.asList(
            "가온", "그린", "건강", "고품격", "나눔", "네이처", "다올", "달콤", "동원",
            "라이프", "러블리", "맘스", "모던", "미소", "바른", "베스트", "비타",
            "사랑", "신선", "순수", "아이", "오가닉", "유기농", "자연", "정직",
            "청정", "초이스", "클린", "키즈", "토탈", "퓨어", "프리미엄", "프레시",
            "하늘", "행복", "헬시", "홈"
        ));

        KOREAN_BRAND_SUFFIXES.addAll(Arrays.asList(
            "농장", "푸드", "팜", "마켓", "키친", "쿡", "식탁", "밀", "상회",
            "하우스", "웨어", "라이프", "케어", "플러스", "랜드", "월드", "존",
            "코리아", "몰", "샵", "스토어", "가든", "팩토리", "컴퍼니"
        ));

        ENGLISH_BRAND_PREFIXES.addAll(Arrays.asList(
            "Active", "Best", "Care", "Daily", "Easy", "Fresh", "Green", "Happy",
            "Ideal", "Joy", "Kind", "Life", "Modern", "Nature", "Organic", "Pure",
            "Quality", "Real", "Safe", "True", "Urban", "Vital", "Whole", "Xtra",
            "Young", "Zen"
        ));

        ENGLISH_BRAND_SUFFIXES.addAll(Arrays.asList(
            "Life", "Fresh", "Care", "Food", "Farm", "Home", "Style", "Cook",
            "Choice", "Market", "Kitchen", "Plus", "Lab", "World", "Land", "Box",
            "Pro", "Hub", "Store", "Gear", "Essence", "Nature"
        ));

        ADJECTIVE_POOL.put("FOOD", Arrays.asList(
            "신선한", "유기농", "무농약", "친환경", "국내산", "산지직송",
            "프리미엄", "특선", "엄선된", "GAP인증", "당일수확", "제철",
            "냉장", "냉동", "손질", "세척", "손질된", "바로먹는"
        ));

        ADJECTIVE_POOL.put("FRUIT", Arrays.asList(
            "달콤한", "새콤달콤한", "아삭한", "싱싱한", "제철", "당도선별",
            "프리미엄", "특선", "수입", "국산", "GAP인증", "유기농",
            "냉장", "냉동", "손질", "컷팅", "바로먹는", "간편"
        ));

        ADJECTIVE_POOL.put("CONVENIENCE", Arrays.asList(
            "간편한", "손쉬운", "바로먹는", "즉석", "냉동", "냉장",
            "프리미엄", "맛있는", "든든한", "가성비", "대용량", "1인분",
            "전자레인지용", "에어프라이어용", "간단조리", "3분완성", "5분완성", "10분완성"
        ));

        ADJECTIVE_POOL.put("BEVERAGE", Arrays.asList(
            "시원한", "청량한", "상쾌한", "깔끔한", "부드러운", "달콤한",
            "무설탕", "저칼로리", "제로슈거", "탄산", "무탄산", "프리미엄",
            "수입", "국산", "유기농", "천연", "100%", "무첨가"
        ));

        ADJECTIVE_POOL.put("FASHION", Arrays.asList(
            "우아한", "세련된", "모던한", "클래식", "트렌디한", "심플한",
            "편안한", "고급스러운", "스타일리시", "캐주얼", "포멀", "빈티지",
            "신상", "베스트", "인기", "필수", "데일리", "시즌"
        ));

        ADJECTIVE_POOL.put("BEAUTY", Arrays.asList(
            "촉촉한", "보습", "미백", "주름개선", "탄력", "진정",
            "순한", "저자극", "약산성", "무향", "무알코올", "천연",
            "프리미엄", "럭셔리", "인기", "베스트", "신상", "한정판"
        ));

        ADJECTIVE_POOL.put("LIVING", Arrays.asList(
            "실용적인", "깔끔한", "모던한", "심플한", "고급스러운", "튼튼한",
            "편리한", "다용도", "공간절약", "수납", "정리", "깨끗한",
            "친환경", "무독성", "안전한", "내구성", "프리미엄", "베스트"
        ));

        ADJECTIVE_POOL.put("KIDS_PET", Arrays.asList(
            "안전한", "순한", "저자극", "무첨가", "유기농", "천연",
            "영양만점", "건강한", "프리미엄", "인기", "베스트", "추천",
            "저알러지", "무향", "무색소", "무방부제", "피부자극테스트완료", "소아과추천"
        ));

        ADJECTIVE_POOL.put("SPORTS_HEALTH", Arrays.asList(
            "프로페셔널", "고성능", "내구성", "경량", "튼튼한", "편안한",
            "고급", "프리미엄", "인기", "베스트", "추천", "필수",
            "기능성", "통기성", "속건", "방수", "UV차단", "쿨링"
        ));

        ADJECTIVE_POOL.put("ELECTRONICS", Arrays.asList(
            "스마트", "고효율", "에너지절약", "저소음", "초절전", "최신형",
            "프리미엄", "고급", "인기", "베스트", "추천", "필수",
            "다기능", "IoT", "wifi연결", "음성인식", "터치", "디지털"
        ));
    }

    private void initializeCounters() {
        log.info("DB 현재 상태를 기반으로 카운터를 초기화합니다...");

        Long maxBrandCode = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(brand_code), 0) FROM brand", Long.class);
        brandCodeCounter.set(maxBrandCode + 1);

        Long maxProductNo = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(no), 0) FROM product", Long.class);
        productNoCounter.set(maxProductNo + 1);

        Long maxDetailId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) FROM product_detail", Long.class);
        detailNoCounter.set(maxDetailId + 1);

        log.info("카운터 초기화 완료 - BrandCode: {}, ProductNo: {}, DetailId: {}",
            brandCodeCounter.get(), productNoCounter.get(), detailNoCounter.get());
    }

    private void syncAutoIncrement() {
        log.info("DB AUTO_INCREMENT 값을 동기화합니다...");
        jdbcTemplate.execute("ALTER TABLE product AUTO_INCREMENT = " + productNoCounter.get());
        jdbcTemplate.execute("ALTER TABLE product_detail AUTO_INCREMENT = " + detailNoCounter.get());
    }

    private static final Map<Long, String> CATEGORY_TO_ADJECTIVE_GROUP = Map.ofEntries(
        Map.entry(1L, "FOOD"), Map.entry(2L, "FRUIT"), Map.entry(3L, "FOOD"),
        Map.entry(4L, "FOOD"), Map.entry(5L, "FOOD"), Map.entry(6L, "CONVENIENCE"),
        Map.entry(7L, "FOOD"), Map.entry(8L, "BEVERAGE"), Map.entry(9L, "BEVERAGE"),
        Map.entry(10L, "CONVENIENCE"), Map.entry(11L, "CONVENIENCE"), Map.entry(12L, "FOOD"),
        Map.entry(13L, "SPORTS_HEALTH"), Map.entry(14L, "BEVERAGE"), Map.entry(15L, "BEVERAGE"),
        Map.entry(16L, "FASHION"), Map.entry(17L, "LIVING"), Map.entry(18L, "LIVING"),
        Map.entry(19L, "ELECTRONICS"), Map.entry(20L, "LIVING"), Map.entry(21L, "KIDS_PET"),
        Map.entry(22L, "KIDS_PET"), Map.entry(23L, "SPORTS_HEALTH"), Map.entry(24L, "BEAUTY"),
        Map.entry(25L, "BEAUTY"), Map.entry(26L, "BEAUTY")
    );

    private static final List<String> WEIGHT_OPTIONS_FOOD = Arrays.asList(
        "300g", "500g", "1kg", "2kg", "3kg", "5kg"
    );
    private static final List<String> WEIGHT_OPTIONS_BEVERAGE = Arrays.asList(
        "300ml", "500ml", "900ml", "1L", "1.5L", "2L"
    );
    private static final List<String> WEIGHT_OPTIONS_DEFAULT = Arrays.asList(
        "1개", "2개", "3개", "1세트", "2세트"
    );

    /**
     * 멀티스레드 버전
     */
    public void seedAll(int productCount) {
        log.info("=== 대량 데이터 시딩 시작 (상품 개수: {}) ===", productCount);

        initializeCounters();
        ensureSellerExists();
        seedBrands();
        if (monitor != null) monitor.recordStep("Brand 생성");

        Map<Long, String> brandNameMap = new HashMap<>();
        jdbcTemplate.query(
            "SELECT id, name FROM brand WHERE seller_id = 1",
            rs -> {
                brandNameMap.put(rs.getLong("id"), rs.getString("name"));
            }
        );
        List<Long> brandIds = new ArrayList<>(brandNameMap.keySet());

        List<CategoryInfo> categories = jdbcTemplate.query(
            "SELECT id, category_name, parent_id FROM category WHERE level = 2",
            (rs, rowNum) -> new CategoryInfo(
                rs.getLong("id"),
                rs.getString("category_name"),
                rs.getLong("parent_id")
            )
        );
        if (monitor != null) monitor.recordStep("Category 조회");

        Long baseProductId = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(id), 0) FROM product",
            Long.class
        );
        if (baseProductId == null) baseProductId = 0L;

        int chunkSize = 1000;
        int totalChunks = (int) Math.ceil((double) productCount / chunkSize);
        int threadCount = Math.min(Runtime.getRuntime().availableProcessors(), 8);

        // 고정 크기 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("DataGenerator-" + counter.incrementAndGet());
                return thread;
            }
        });

        try {
            // 청크별 비동기 작업 생성
            List<CompletableFuture<ChunkResult>> futures = new ArrayList<>();

            for (int chunk = 0; chunk < totalChunks; chunk++) {
                int finalChunk = chunk;
                int startIdx = chunk * chunkSize;
                int currentChunkSize = Math.min(chunkSize, productCount - startIdx);
                // 청크별 독립적인 ID 범위 할당 (충돌 방지)
                long chunkStartProductId = baseProductId + startIdx + 1;

                futures.add(CompletableFuture.supplyAsync(() -> {
                    long chunkStart = System.currentTimeMillis();
                    try {
                        int[] counts = processChunkWithTransaction(startIdx, currentChunkSize, chunkStartProductId, brandIds, brandNameMap, categories);
                        return new ChunkResult(finalChunk, counts[0], counts[1], System.currentTimeMillis() - chunkStart, null);
                    } catch (Exception e) {
                        return new ChunkResult(finalChunk, 0, 0, System.currentTimeMillis() - chunkStart, e);
                    }
                }, executorService));
            }
            // 모든 작업 완료까지 블로킹
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 결과 집계
            List<ChunkResult> results = futures.stream()
                .map(CompletableFuture::join).toList();

            long successCount = results.stream().filter(ChunkResult::isSuccess).count();

            int totalProducts = results.stream()
                .mapToInt(ChunkResult::productsCreated)
                .sum();

            int totalDetails = results.stream()
                .mapToInt(ChunkResult::detailsCreated)
                .sum();

            log.info("=== 대량 데이터 시딩 완료 (성공: {}/{}, 상품: {}개, 상세: {}개) ===", successCount, totalChunks, totalProducts, totalDetails);

            syncAutoIncrement();

        } finally {
            // 리소스 정리
            executorService.shutdown();

            // ThreadLocal 정리
            FAKER_THREAD_LOCAL.remove();
        }
    }

    private void ensureSellerExists() {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM seller WHERE id = 1",
            Long.class
        );

        if (count == null || count == 0) {
            log.info("seller_id = 1인 데이터를 생성합니다.");
            jdbcTemplate.update(
                """
                    INSERT INTO seller (id, name, email, password, phone_number, fee_rate, payout_day, created_at, updated_at)
                    VALUES (1, 'BeGroom', 'admin@begroom.com', '\\$2a\\$10\\$dummyHash', '02-1234-5678', 5.00, 25, NOW(), NOW());
                    """
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedBrands() {
        ensureDefaultBrandExists();

        Long existingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM brand WHERE seller_id = 1", Long.class);
        if (existingCount != null && existingCount > 1) return;

        Set<String> allBrands = new HashSet<>();
        for (String prefix : KOREAN_BRAND_PREFIXES) {
            for (String suffix : KOREAN_BRAND_SUFFIXES) {
                allBrands.add(prefix + suffix);
            }
        }
        for (String prefix : ENGLISH_BRAND_PREFIXES) {
            for (String suffix : ENGLISH_BRAND_SUFFIXES) {
                allBrands.add(prefix + suffix);
            }
        }

        List<String> brandList = new ArrayList<>(allBrands);
        Collections.shuffle(brandList);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String sql = "INSERT INTO brand (seller_id, brand_code, name, description, created_at, updated_at) VALUES (1, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String brandName = brandList.get(i);

                ps.setLong(1, brandCodeCounter.getAndIncrement());
                ps.setString(2, brandName);
                ps.setString(3, brandName + " 브랜드");
                ps.setTimestamp(4, now);
                ps.setTimestamp(5, now);
            }

            @Override
            public int getBatchSize() {
                return brandList.size();
            }
        });
    }

    private void ensureDefaultBrandExists() {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM brand WHERE id = 1",
            Long.class
        );

        if (count == null || count == 0) {
            log.info("기본 브랜드 '비구름'을 생성합니다.");
            jdbcTemplate.update(
                """
                    INSERT INTO brand (id, seller_id, brand_code, name, logo_url, description, created_at, updated_at)
                    VALUES (1, 1, 0, '비구름', NULL, '비구름 자체 브랜드', NOW(), NOW())
                    """
            );
        }
    }

    /**
     * 각 청크를 별도 트랜잭션으로 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    public int[] processChunkWithTransaction(int startIdx, int chunkSize, long startProductId,
                                             List<Long> brandIds, Map<Long, String> brandNameMap,
                                             List<CategoryInfo> categories) {
        // 청크 내 병렬 처리용 Executor 생성
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            Faker faker = FAKER_THREAD_LOCAL.get();

            // 카테고리 할당
            List<Long> assignedCategoryIds = new ArrayList<>(chunkSize);
            for (int i = 0; i < chunkSize; i++) {
                assignedCategoryIds.add(
                    categories.get((startIdx + i) % categories.size()).id()
                );
            }

            // Product 생성
            List<ProductInfo> productInfos = seedProductsOptimized(
                startProductId, chunkSize, brandIds, brandNameMap,
                categories, assignedCategoryIds, faker
            );
            List<Long> productIds = productInfos.stream().map(ProductInfo::id).toList();

            // 2. Product 관련 병렬 실행
            CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() ->
                seedProductCategoryMappings(productIds, assignedCategoryIds), executor
            );
            CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() ->
                seedProductImages(productIds, assignedCategoryIds, faker), executor
            );

            // 완료 대기
            CompletableFuture.allOf(categoryFuture, imageFuture).join();

            // 3. Detail 생성
            List<Long> detailIds = seedProductDetailsOptimized(productInfos, faker);

            // 4. Detail 관련 병렬 실행
            CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() ->
                seedPrices(detailIds, faker), executor
            );
            CompletableFuture<Void> stockFuture = CompletableFuture.runAsync(() ->
                seedStocks(detailIds, faker), executor
            );
            CompletableFuture<Void> optionFuture = CompletableFuture.runAsync(() ->
                seedOptionMappings(detailIds, faker), executor
            );

            // 완료 대기
            CompletableFuture.allOf(priceFuture, stockFuture, optionFuture).join();

            return new int[]{productIds.size(), detailIds.size()};
        } catch (Exception e) {
            log.error("청크 처리 중 에러 발생 (startIdx: {}, chunkSize: {})", startIdx, chunkSize, e);
            throw e;
        } finally {
            // Executor 정리
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.warn("Executor 강제 종료 (청크: {})", startIdx);
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Product 생성
     */
    private List<ProductInfo> seedProductsOptimized(long startProductId, int count,
                                                    List<Long> brandIds,
                                                    Map<Long, String> brandNameMap,
                                                    List<CategoryInfo> categories,
                                                    List<Long> assignedIds,
                                                    Faker faker) {
        String sql = "INSERT INTO product (id, brand_id, no, name, short_description, " +
            "product_status, wishlist_count, sales_count, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Map<Long, CategoryInfo> categoryMap = categories.stream().collect(Collectors.toMap(CategoryInfo::id, c -> c));
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        List<ProductInfo> createdProducts = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            long productId = startProductId + i;

            CategoryInfo category = categoryMap.get(assignedIds.get(i));
            String[] keywords = category.name().split("·");
            String keyword = keywords[faker.random().nextInt(keywords.length)].trim();

            Long parentId = category.parentId();
            String adjectiveGroup = CATEGORY_TO_ADJECTIVE_GROUP.getOrDefault(parentId, "FOOD");
            List<String> adjectivePool = ADJECTIVE_POOL.get(adjectiveGroup);
            String adjective = adjectivePool.get(faker.random().nextInt(adjectivePool.size()));

            Long brandId = brandIds.get(faker.random().nextInt(brandIds.size()));
            String brandName = brandNameMap.get(brandId);

            String productName = String.format("[%s] %s %s", brandName, adjective, keyword);
            String description = adjective + " " + keyword + " 상품입니다.";

            createdProducts.add(new ProductInfo(
                productId,
                productName,
                assignedIds.get(i),
                brandId,
                description
            ));
        }

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProductInfo p = createdProducts.get(i);

                ps.setLong(1, p.id());
                ps.setLong(2, p.brandId());
                ps.setLong(3, productNoCounter.getAndIncrement());
                ps.setString(4, p.name());
                ps.setString(5, p.description());
                ps.setString(6, "SALE");
                ps.setInt(7, 0);
                ps.setInt(8, 0);
                ps.setTimestamp(9, now);
                ps.setTimestamp(10, now);
            }

            @Override
            public int getBatchSize() {
                return createdProducts.size();
            }
        });
        return createdProducts;
    }

    /**
     * ProductDetail 생성
     */
    private List<Long> seedProductDetailsOptimized(List<ProductInfo> productInfos, Faker faker) {
        String sql = "INSERT INTO product_detail (id, product_id, no, name, is_available, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        List<ProductDetailBatch> batches = new ArrayList<>();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        for (ProductInfo p : productInfos) {
            double random = faker.random().nextDouble();
            int detailCount = random < 0.3 ? 1 : random < 0.7 ? 2 : 3;

            String adjectiveGroup = CATEGORY_TO_ADJECTIVE_GROUP.getOrDefault(p.categoryId(), "FOOD");

            List<String> options = switch (adjectiveGroup) {
                case "BEVERAGE" -> WEIGHT_OPTIONS_BEVERAGE;
                case "FOOD", "FRUIT" -> WEIGHT_OPTIONS_FOOD;
                default -> WEIGHT_OPTIONS_DEFAULT;
            };

            for (int j = 0; j < detailCount; j++) {
                long detailId = detailNoCounter.getAndIncrement();
                String optionLabel = options.get(faker.random().nextInt(options.size()));
                String detailName = (detailCount == 1) ? p.name() : p.name() + " - " + optionLabel;
                batches.add(new ProductDetailBatch(p.id(), detailId, detailName));
            }
        }

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProductDetailBatch b = batches.get(i);

                ps.setLong(1, b.detailNo());
                ps.setLong(2, b.productId());
                ps.setLong(3, b.detailNo());
                ps.setString(4, b.detailName());
                ps.setBoolean(5, true);
                ps.setTimestamp(6, now);
                ps.setTimestamp(7, now);
            }

            @Override
            public int getBatchSize() {
                return batches.size();
            }
        });
        return batches.stream().map(ProductDetailBatch::detailNo).toList();
    }

    private static void loadCategoryImages() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = DummyDataGenerator.class.getResourceAsStream("/category_images.json");

            if (is == null) return;

            TypeReference<Map<String, List<String>>> typeRef = new TypeReference<>() {};
            Map<String, List<String>> stringKeyMap = mapper.readValue(is, typeRef);

            for (Map.Entry<String, List<String>> entry : stringKeyMap.entrySet()) {
                try {
                    Long categoryId = Long.parseLong(entry.getKey());
                    CATEGORY_IMAGE_URLS.put(categoryId, entry.getValue());
                } catch (NumberFormatException e) {
                    log.error("카테고리 ID 파싱 실패: {}", entry.getKey(), e);
                }
            }
        } catch (IOException e) {
            log.error("카테고리 이미지 로드 실패", e);
        }
    }

    private String getImageUrlForCategory(Long categoryId, Faker faker) {
        List<String> categoryImages = CATEGORY_IMAGE_URLS.get(categoryId);

        if (categoryImages != null && !categoryImages.isEmpty()) {
            return categoryImages.get(faker.random().nextInt(categoryImages.size()));
        } else {
            return "https://picsum.photos/seed/" + UUID.randomUUID() + "/600/600";
        }
    }

    /**
     * Product 이미지 생성
     */
    private void seedProductImages(List<Long> productIds, List<Long> assignedCategoryIds, Faker faker) {
        String sql = "INSERT INTO product_image (product_id, image_url, image_type, sort_order, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, productIds.get(i));
                ps.setString(2, getImageUrlForCategory(assignedCategoryIds.get(i), faker));
                ps.setString(3, "MAIN");
                ps.setInt(4, 1);
                ps.setTimestamp(5, now);
                ps.setTimestamp(6, now);
            }

            @Override
            public int getBatchSize() {
                return productIds.size();
            }
        });
    }

    /**
     * 재고 생성
     */
    private void seedStocks(List<Long> detailIds, Faker faker) {
        String sql = "INSERT INTO stock (product_detail_id, quantity, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, detailIds.get(i));
                ps.setInt(2, faker.number().numberBetween(1, 100));
                ps.setTimestamp(3, now);
                ps.setTimestamp(4, now);
            }

            @Override
            public int getBatchSize() {
                return detailIds.size();
            }
        });
    }

    /**
     * 가격 생성
     */
    private void seedPrices(List<Long> detailIds, Faker faker) {
        String sql = "INSERT INTO product_price (product_detail_id, original_price, discounted_price, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int originalPrice = (faker.number().numberBetween(10, 100)) * 1000;

                Integer discountedPrice = null;
                if (faker.random().nextDouble() < 0.3) {
                    discountedPrice = (int) (originalPrice * 0.8);
                }

                ps.setLong(1, detailIds.get(i));
                ps.setInt(2, originalPrice);
                if (discountedPrice != null) {
                    ps.setInt(3, discountedPrice);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setTimestamp(4, now);
                ps.setTimestamp(5, now);
            }

            @Override
            public int getBatchSize() {
                return detailIds.size();
            }
        });
    }

    /**
     * 옵션 매핑 생성
     */
    private void seedOptionMappings(List<Long> detailIds, Faker faker) {
        String sql = "INSERT INTO product_option_mapping (product_detail_id, option_id, created_at) " +
            "VALUES (?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, detailIds.get(i / 2));
                ps.setLong(2, (i % 2 == 0) ? faker.random().nextInt(1, 3) : faker.random().nextInt(4, 5));
                ps.setTimestamp(3, now);
            }

            @Override
            public int getBatchSize() {
                return detailIds.size() * 2;
            }
        });
    }

    /**
     * 카테고리 매핑 생성
     */
    private void seedProductCategoryMappings(List<Long> productIds, List<Long> assignedCategoryIds) {
        String sql = "INSERT INTO product_category (product_id, category_id, is_primary, created_at) " +
            "VALUES (?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, productIds.get(i));
                ps.setLong(2, assignedCategoryIds.get(i));
                ps.setBoolean(3, true);
                ps.setTimestamp(4, now);
            }

            @Override
            public int getBatchSize() {
                return productIds.size();
            }
        });
    }
}