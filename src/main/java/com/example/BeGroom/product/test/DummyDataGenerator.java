package com.example.BeGroom.product.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
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
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker(new Locale("ko"));

    private PerformanceMonitor monitor;
    public void setPerformanceMonitor(PerformanceMonitor monitor) {
        this.monitor = monitor;
    }

    private record CategoryInfo(Long id, String name, Long parentId) {}
    private record ProductDetailBatch(Long productId, Long detailNo, int optionNum) {}

    private static final Map<String, List<String>> ADJECTIVE_POOL = new HashMap<>();
    private static final List<String> KOREAN_BRAND_PREFIXES = new ArrayList<>();
    private static final List<String> KOREAN_BRAND_SUFFIXES = new ArrayList<>();
    private static final List<String> ENGLISH_BRAND_PREFIXES = new ArrayList<>();
    private static final List<String> ENGLISH_BRAND_SUFFIXES = new ArrayList<>();
    private static final Map<Long, List<String>> CATEGORY_IMAGE_URLS = new HashMap<>();

    private final AtomicLong brandCodeCounter = new AtomicLong(1000000L);
    private final AtomicLong productNoCounter = new AtomicLong(30000000L);
    private final AtomicLong detailNoCounter = new AtomicLong(50000000L);

    static {

        loadCategoryImages();

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

    // 중량/용량 옵션
    private static final List<String> WEIGHT_OPTIONS_FOOD = Arrays.asList(
        "300g", "500g", "1kg", "2kg", "3kg", "5kg"
    );
    private static final List<String> WEIGHT_OPTIONS_BEVERAGE = Arrays.asList(
        "300ml", "500ml", "900ml", "1L", "1.5L", "2L"
    );
    private static final List<String> WEIGHT_OPTIONS_DEFAULT = Arrays.asList(
        "1개", "2개", "3개", "1세트", "2세트"
    );

    @Transactional
    public void seedAll(int productCount) {
        log.info("=== 대량 데이터 시딩 시작 (상품 개수: {}) ===", productCount);
        long startTime = System.currentTimeMillis();

        ensureSellerExists();
        if (monitor != null) monitor.recordStep("Seller 확인");

        seedBrands();
        if (monitor != null) monitor.recordStep("Brand 생성");

        List<Long> brandIds = jdbcTemplate.queryForList("SELECT id FROM brand WHERE seller_id = 1", Long.class);

        // 카테고리 데이터 확보 (Level 2)
        List<CategoryInfo> categories = jdbcTemplate.query(
            "SELECT c.id, c.category_name, c.parent_id FROM category c WHERE c.level = 2",
            (rs, rowNum) -> new CategoryInfo(
                rs.getLong("id"),
                rs.getString("category_name"),
                rs.getLong("parent_id")
            )
        );
        if (monitor != null) monitor.recordStep("Category 조회");

        int chunkSize = 1000;
        int totalChunks = (int) Math.ceil((double) productCount / chunkSize);

        log.info("총 {}개 청크로 분할하여 처리 (청크 크기: {})", totalChunks, chunkSize);

        for (int chunk = 0; chunk < totalChunks; chunk++) {
            int startIdx = chunk * chunkSize;
            int endIdx = Math.min(startIdx + chunkSize, productCount);
            int currentChunkSize = endIdx - startIdx;

            log.info("청크 {}/{} 처리 중 ({}~{}번째 상품)",
                chunk + 1, totalChunks, startIdx + 1, endIdx);

            long chunkStart = System.currentTimeMillis();

            processChunkWithTransaction(startIdx, currentChunkSize, brandIds, categories);

            long chunkDuration = System.currentTimeMillis() - chunkStart;
            if (monitor != null) {
                monitor.recordChunk(chunk + 1, currentChunkSize, chunkDuration);
            }
            if (chunk > 0 && chunk % 10 == 0) {
                System.gc();
                log.info("메모리 정리 실행 ({}개 청크 완료)", chunk);
            }
        }
        if (monitor != null) monitor.recordStep("전체 Product 생성 완료");

        long duration = (System.currentTimeMillis() - startTime) / 1000;
        log.info("=== 대량 데이터 시딩 완료 (소요시간: {}초) ===", duration);
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
        } else {
            log.info("seller_id = 1인 데이터가 이미 존재합니다.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedBrands() {

        ensureDefaultBrandExists();

        Long existingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM brand WHERE seller_id = 1", Long.class);

        if (existingCount != null && existingCount > 1) return;

        String sql = "INSERT INTO brand (seller_id, brand_code, name, description, created_at, updated_at) VALUES (1, ?, ?, ?, ?, ?)";

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

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String brandName = brandList.get(i);
                long uniqueBrandCode = brandCodeCounter.getAndIncrement();

                ps.setLong(1, uniqueBrandCode);
                ps.setString(2, brandName);
                ps.setString(3, brandName + "브랜드");
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
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
        } else {
            log.info("기본 브랜드가 이미 존재합니다.");
        }
    }

    /**
     * 각 청크를 별도 트랜잭션으로 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processChunkWithTransaction(int startIdx, int chunkSize,
                                            List<Long> brandIds,
                                            List<CategoryInfo> categories) {
        try {
            processChunk(startIdx, chunkSize, brandIds, categories);
        } catch (Exception e) {
            log.error("청크 처리 실패 (시작: {}, 크기: {}): {}", startIdx, chunkSize, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 실제 데이터 생성 로직
     */
    private void processChunk(int startIdx, int chunkSize,
                              List<Long> brandIds,
                              List<CategoryInfo> categories) {

        // 1. 카테고리 할당
        List<Long> assignedCategoryIds = new ArrayList<>();
        for (int i = 0; i < chunkSize; i++) {
            assignedCategoryIds.add(
                categories.get((startIdx + i) % categories.size()).id()
            );
        }

        // 2. Product 생성
        long startProductId = getNextProductId();
        seedProductsCustomOptimized(startProductId, chunkSize, brandIds, categories, assignedCategoryIds);

        // 3. ID 범위로 조회
        List<Long> productIds = jdbcTemplate.queryForList(
            "SELECT id FROM product WHERE id >= ? AND id < ? ORDER BY id",
            Long.class,
            startProductId,
            startProductId + chunkSize + 100
        );

        if (productIds.isEmpty()) {
            log.warn("생성된 Product가 없습니다. 스킵합니다.");
            return;
        }

        // 4. 연관 데이터 생성
        seedProductCategoryMappings(productIds, assignedCategoryIds);
        seedProductImages(productIds, assignedCategoryIds);
        seedProductDetails(productIds);

        // 5. ProductDetail ID를 범위로 조회
        Long minProductId = productIds.stream().min(Long::compare).orElse(0L);
        Long maxProductId = productIds.stream().max(Long::compare).orElse(0L);

        List<Long> detailIds = jdbcTemplate.queryForList(
            "SELECT id FROM product_detail WHERE product_id BETWEEN ? AND ? ORDER BY id",
            Long.class,
            minProductId,
            maxProductId
        );

        if (detailIds.isEmpty()) {
            log.warn("생성된 ProductDetail이 없습니다.");
            return;
        }

        seedPrices(detailIds);
        seedStocks(detailIds);
        seedOptionMappings(detailIds);

        log.info("청크 처리 완료 ({}개 상품, {}개 상세)", productIds.size(), detailIds.size());
    }

    /**
     * 다음 Product ID 조회
     */
    private long getNextProductId() {
        Long maxId = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(id), 0) FROM product",
            Long.class
        );
        return maxId != null ? maxId + 1 : 1L;
    }

    /**
     * Product 생성 최적화 (500개씩 분할)
     */
    private void seedProductsCustomOptimized(long startProductId, int count,
                                             List<Long> brandIds,
                                             List<CategoryInfo> categories,
                                             List<Long> assignedIds) {
        String sql = "INSERT INTO product (brand_id, no, name, short_description, " +
            "product_status, wishlist_count, sales_count, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Map<Long, CategoryInfo> categoryMap = new HashMap<>();
        for (CategoryInfo ci : categories) {
            categoryMap.put(ci.id(), ci);
        }

        Map<Long, String> brandIdToNameMap = new HashMap<>();
        String brandIdList = brandIds.stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b)
            .orElse("");

        jdbcTemplate.query(
            "SELECT id, name FROM brand WHERE id IN (" + brandIdList + ")",
            rs -> {
                brandIdToNameMap.put(rs.getLong("id"), rs.getString("name"));
            }
        );

        // 500개씩 분할하여 배치 삽입
        int subBatchSize = 500;
        for (int offset = 0; offset < count; offset += subBatchSize) {
            int currentBatchSize = Math.min(subBatchSize, count - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int actualIndex = finalOffset + i;
                    Long categoryId = assignedIds.get(actualIndex);
                    CategoryInfo category = categoryMap.get(categoryId);

                    String fullCategoryName = category.name();
                    String[] keywords = fullCategoryName.split("·");
                    String keyword = keywords[faker.random().nextInt(keywords.length)].trim();

                    Long parentId = category.parentId();
                    String adjectiveGroup = CATEGORY_TO_ADJECTIVE_GROUP.getOrDefault(parentId, "FOOD");
                    List<String> adjectivePool = ADJECTIVE_POOL.get(adjectiveGroup);
                    String adjective = adjectivePool.get(faker.random().nextInt(adjectivePool.size()));

                    Long brandId = brandIds.get(faker.random().nextInt(brandIds.size()));
                    String brandName = brandIdToNameMap.get(brandId);

                    String productName = String.format("[%s] %s %s", brandName, adjective, keyword);
                    long uniqueProductNo = productNoCounter.getAndIncrement();

                    ps.setLong(1, brandId);
                    ps.setLong(2, uniqueProductNo);
                    ps.setString(3, productName);
                    ps.setString(4, adjective + " " + keyword + " 상품입니다.");
                    ps.setString(5, "SALE");
                    ps.setInt(6, 0);
                    ps.setInt(7, 0);
                    ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });
        }
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
                    log.error("카테고리 ID 파싱 실패");
                }
            }
        } catch (IOException e) {
            log.error("카테고리 이미지 로드 실패");
        }
    }

    private String getImageUrlForCategory(Long categoryId) {
        List<String> categoryImages = CATEGORY_IMAGE_URLS.get(categoryId);

        if (categoryImages != null && !categoryImages.isEmpty()) {
            return categoryImages.get(faker.random().nextInt(categoryImages.size()));
        } else {
            return "https://picsum.photos/seed/" + UUID.randomUUID() + "/600/600";
        }
    }

    /**
     * Product 이미지 생성 (500개씩 분할)
     */
    private void seedProductImages(List<Long> productIds, List<Long> assignedCategoryIds) {
        String sql = "INSERT INTO product_image (product_id, image_url, image_type, sort_order, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        int subBatchSize = 500;
        for (int offset = 0; offset < productIds.size(); offset += subBatchSize) {
            int currentSize = Math.min(subBatchSize, productIds.size() - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Long productId = productIds.get(finalOffset + i);
                    Long categoryId = assignedCategoryIds.get(finalOffset + i);
                    String imageUrl = getImageUrlForCategory(categoryId);

                    ps.setLong(1, productId);
                    ps.setString(2, imageUrl);
                    ps.setString(3, "MAIN");
                    ps.setInt(4, 1);
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                }
                @Override
                public int getBatchSize() { return currentSize; }
            });
        }
    }

    /**
     * ProductDetail 생성 (500개씩 분할)
     */
    private void seedProductDetails(List<Long> productIds) {
        String sql = "INSERT INTO product_detail (product_id, no, name, is_available, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        List<ProductDetailBatch> detailBatches = new ArrayList<>();
//        long detailNoCounter = 2000000000L;
        Map<Long, Integer> productDetailCountMap = new HashMap<>();

        for (Long productId : productIds) {
            int detailCount;
            double random = faker.random().nextDouble();
            if (random < 0.3) {
                detailCount = 1;
            } else if (random < 0.7) {
                detailCount = 2;
            } else {
                detailCount = 3;
            }

            productDetailCountMap.put(productId, detailCount);

            for (int j = 0; j < detailCount; j++) {
                detailBatches.add(new ProductDetailBatch(
                    productId,
                    detailNoCounter.getAndIncrement(),
                    j + 1
                ));
            }
        }

        Map<Long, String> productNameMap = new HashMap<>();
        Long minId = productIds.stream().min(Long::compare).orElse(0L);
        Long maxId = productIds.stream().max(Long::compare).orElse(0L);

        jdbcTemplate.query(
            "SELECT id, name FROM product WHERE id BETWEEN ? AND ?",
            new Object[]{minId, maxId},
            rs -> {
                productNameMap.put(rs.getLong("id"), rs.getString("name"));
            }
        );

        // 500개씩 분할하여 배치 삽입
        int subBatchSize = 500;
        for (int offset = 0; offset < detailBatches.size(); offset += subBatchSize) {
            int currentSize = Math.min(subBatchSize, detailBatches.size() - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ProductDetailBatch batch = detailBatches.get(finalOffset + i);
                    int totalDetailCount = productDetailCountMap.get(batch.productId);

                    String detailName;
                    if (totalDetailCount == 1) {
                        detailName = productNameMap.get(batch.productId);
                    } else {
                        List<String> weightOptions;
                        int randomCategory = faker.random().nextInt(10);
                        if (randomCategory < 5) {
                            weightOptions = WEIGHT_OPTIONS_FOOD;
                        } else if (randomCategory < 7) {
                            weightOptions = WEIGHT_OPTIONS_BEVERAGE;
                        } else {
                            weightOptions = WEIGHT_OPTIONS_DEFAULT;
                        }

                        String weight = weightOptions.get(faker.random().nextInt(weightOptions.size()));
                        detailName = String.format("옵션%d - %s", batch.optionNum, weight);
                    }

                    ps.setLong(1, batch.productId);
                    ps.setLong(2, batch.detailNo);
                    ps.setString(3, detailName);
                    ps.setBoolean(4, true);
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                }
                @Override
                public int getBatchSize() { return currentSize; }
            });
        }
    }

    /**
     * 재고 생성 (500개씩 분할)
     */
    private void seedStocks(List<Long> detailIds) {
        String sql = "INSERT INTO stock (product_detail_id, quantity, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?)";

        int subBatchSize = 500;
        for (int offset = 0; offset < detailIds.size(); offset += subBatchSize) {
            int currentSize = Math.min(subBatchSize, detailIds.size() - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, detailIds.get(finalOffset + i));
                    ps.setInt(2, faker.number().numberBetween(0, 500));
                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                }
                @Override
                public int getBatchSize() { return currentSize; }
            });
        }
    }

    /**
     * 가격 생성 (500개씩 분할)
     */
    private void seedPrices(List<Long> detailIds) {
        String sql = "INSERT INTO product_price (product_detail_id, original_price, discounted_price, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?)";

        int subBatchSize = 500;
        for (int offset = 0; offset < detailIds.size(); offset += subBatchSize) {
            int currentSize = Math.min(subBatchSize, detailIds.size() - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int originalPrice = (faker.number().numberBetween(10, 100)) * 1000;

                    Integer discountedPrice = null;
                    if (faker.random().nextDouble() < 0.3) {
                        discountedPrice = (int) (originalPrice * 0.8);
                    }

                    ps.setLong(1, detailIds.get(finalOffset + i));
                    ps.setInt(2, originalPrice);
                    if (discountedPrice != null) {
                        ps.setInt(3, discountedPrice);
                    } else {
                        ps.setNull(3, Types.INTEGER);
                    }
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                }
                @Override
                public int getBatchSize() { return currentSize; }
            });
        }
    }

    /**
     * 옵션 매핑 생성 (500개씩 분할)
     */
    private void seedOptionMappings(List<Long> detailIds) {
        String sql = "INSERT INTO product_option_mapping (product_detail_id, option_id, created_at) " +
            "VALUES (?, ?, ?)";

        int totalMappings = detailIds.size() * 2;
        int subBatchSize = 500;

        for (int offset = 0; offset < totalMappings; offset += subBatchSize) {
            int currentSize = Math.min(subBatchSize, totalMappings - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int actualIndex = finalOffset + i;
                    Long detailId = detailIds.get(actualIndex / 2);
                    ps.setLong(1, detailId);

                    if (actualIndex % 2 == 0) {
                        ps.setLong(2, faker.random().nextInt(1, 3));
                    } else {
                        ps.setLong(2, faker.random().nextInt(4, 5));
                    }

                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                }

                @Override
                public int getBatchSize() {
                    return currentSize;
                }
            });
        }
    }

    /**
     * 카테고리 매핑 생성 (500개씩 분할)
     */
    private void seedProductCategoryMappings(List<Long> productIds, List<Long> assignedCategoryIds) {
        String sql = "INSERT INTO product_category (product_id, category_id, is_primary, created_at) " +
            "VALUES (?, ?, ?, ?)";

        int subBatchSize = 500;
        for (int offset = 0; offset < productIds.size(); offset += subBatchSize) {
            int currentSize = Math.min(subBatchSize, productIds.size() - offset);
            final int finalOffset = offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, productIds.get(finalOffset + i));
                    ps.setLong(2, assignedCategoryIds.get(finalOffset + i));
                    ps.setBoolean(3, true);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                }
                @Override
                public int getBatchSize() { return currentSize; }
            });
        }
    }
}
