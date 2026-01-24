package com.example.BeGroom.product.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyDataGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker(new Locale("ko"));
    private record CategoryInfo(Long id, String name, Long parentId) {}

    private static final Map<String, List<String>> ADJECTIVE_POOL = new HashMap<>();

    private static final List<String> KOREAN_BRAND_PREFIXES = new ArrayList<>();
    private static final List<String> KOREAN_BRAND_SUFFIXES = new ArrayList<>();
    private static final List<String> ENGLISH_BRAND_PREFIXES = new ArrayList<>();
    private static final List<String> ENGLISH_BRAND_SUFFIXES = new ArrayList<>();

    static {
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

        // 브랜드 생성
        seedBrands();

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

        List<Long> assignedCategoryIds = new ArrayList<>();
        for (int i = 0; i < productCount; i++) {
            assignedCategoryIds.add(categories.get(i % categories.size()).id());
        }

        // Product & Image 삽입
        seedProductsCustom(productCount, brandIds, categories, assignedCategoryIds);
        List<Long> productIds = jdbcTemplate.queryForList(
            "SELECT id FROM product ORDER BY id DESC LIMIT " + productCount, Long.class);
        Collections.reverse(productIds);

        // ProductDetail, Price, Stock, OptionMapping (상품당 2개씩 옵션 생성)
        seedProductImages(productIds);
        seedProductDetails(productIds);

        List<Long> detailIds = jdbcTemplate.queryForList(
            "SELECT id FROM product_detail WHERE product_id IN (" +
                productIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0") +
                ") ORDER BY id",
            Long.class
        );
        Collections.reverse(detailIds);

        seedPrices(detailIds);
        seedStocks(detailIds);
        seedOptionMappings(detailIds);

        // 8. ProductCategory (카테고리 매핑) 벌크 삽입
        seedProductCategoryMappings(productIds, assignedCategoryIds);

        log.info("=== 대량 데이터 시딩 완료 ===");
    }

    private void seedBrands() {
        Long existingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM brand WHERE seller_id = 1", Long.class);

        if (existingCount != null && existingCount > 0) return;

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
                ps.setLong(1, Long.parseLong(faker.number().digits(8)) + i);
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

    private void seedProductsCustom(int count, List<Long> brandIds, List<CategoryInfo> categories, List<Long> assignedIds) {
        String sql = "INSERT INTO product (brand_id, no, name, short_description, product_status, wishlist_count, sales_count, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Map<Long, CategoryInfo> categoryMap = new HashMap<>();
        for (CategoryInfo ci : categories) categoryMap.put(ci.id(), ci);

        Map<Long, String> brandIdToNameMap = new HashMap<>();
        String brandIdList = brandIds.stream()
                .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                        .orElse("");

        jdbcTemplate.query("SELECT id, name FROM brand WHERE id IN (" + brandIdList + ")", rs -> {
            brandIdToNameMap.put(rs.getLong("id"), rs.getString("name"));
        });

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long categoryId = assignedIds.get(i);
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

                ps.setLong(1, brandId);
                ps.setLong(2, 1000000000L + i);
                ps.setString(3, productName);
                ps.setString(4, adjective + " " + keyword + " 상품입니다.");
                ps.setString(5, "SALE");
                ps.setInt(6, 0); // wishlistCount
                ps.setInt(7, 0); // salesCount
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            }
            @Override
            public int getBatchSize() { return count; }
        });
    }

    private void seedProductImages(List<Long> productIds) {
        String sql = "INSERT INTO product_image (product_id, image_url, image_type, sort_order, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, productIds.get(i));
                ps.setString(2, "https://picsum.photos/seed/" + UUID.randomUUID() + "/600/600");
                ps.setString(3, "MAIN");
                ps.setInt(4, 1);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            }
            @Override
            public int getBatchSize() { return productIds.size(); }
        });
    }

    private void seedProductDetails(List<Long> productIds) {
        String sql = "INSERT INTO product_detail (product_id, no, name, is_available, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        List<ProductDetailBatch> detailBatches = new ArrayList<>();
        long detailNoCounter = 2000000000L;

        Map<Long, Integer> productDetailCountMap = new HashMap<>();

        for (Long productId : productIds) {
            // 30% 확률로 상세 상품 없음, 70% 확률로 1~3개
            int detailCount;
            double random = faker.random().nextDouble();
            if (random < 0.3) {
                detailCount = 1; // 30% 확률로 단일 옵션
            } else if (random < 0.7) {
                detailCount = 2; // 옵션 2개
            } else {
                detailCount = 3; // 옵션 3개
            }

            productDetailCountMap.put(productId, detailCount);

            for (int j = 0; j < detailCount; j++) {
                detailBatches.add(new ProductDetailBatch(
                    productId,
                    detailNoCounter++,
                    j + 1
                ));
            }
        }

        Map<Long, String> productNameMap = new HashMap<>();
        String productIdList = productIds.stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b)
            .orElse("0");

        jdbcTemplate.query("SELECT id, name FROM product WHERE id IN (" + productIdList + ")", rs -> {
            productNameMap.put(rs.getLong("id"), rs.getString("name"));
        });

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ProductDetailBatch batch = detailBatches.get(i);
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
            public int getBatchSize() { return detailBatches.size(); }
        });
    }

    private record ProductDetailBatch(Long productId, Long detailNo, int optionNum) {}

    private void seedStocks(List<Long> detailIds) {
        String sql = "INSERT INTO stock (product_detail_id, quantity, created_at, updated_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, detailIds.get(i));
                ps.setInt(2, faker.number().numberBetween(0, 500));
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            }
            @Override
            public int getBatchSize() { return detailIds.size(); }
        });
    }

    private void seedPrices(List<Long> detailIds) {
        String sql = "INSERT INTO product_price (product_detail_id, original_price, discounted_price, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?)";

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
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }
            @Override
            public int getBatchSize() { return detailIds.size(); }
        });
    }

    private void seedOptionMappings(List<Long> detailIds) {
        String sql = "INSERT INTO product_option_mapping (product_detail_id, option_id, created_at) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long detailId = detailIds.get(i / 2);
                ps.setLong(1, detailId);

                if (i % 2 == 0) {
                    ps.setLong(2, faker.random().nextInt(1, 3));
                } else {
                    ps.setLong(2, faker.random().nextInt(4, 5));
                }

                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return detailIds.size() * 2;
            }
        });
    }

    private void seedProductCategoryMappings(List<Long> productIds, List<Long> assignedCategoryIds) {
        String sql = "INSERT INTO product_category (product_id, category_id, is_primary, created_at) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, productIds.get(i));
                ps.setLong(2, assignedCategoryIds.get(i));
                ps.setBoolean(3, true);
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            }
            @Override
            public int getBatchSize() { return productIds.size(); }
        });
    }
}
