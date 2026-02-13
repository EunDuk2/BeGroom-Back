import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { randomIntBetween, randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import { Rate, Trend, Counter } from 'k6/metrics';

class URLSearchParams {
    constructor() {
        this.params = [];
    }

    append(key, value) {
        this.params.push(`${key}=${encodeURIComponent(value)}`);
    }

    toString() {
        return this.params.join('&');
    }
}

// ============================================================
// 커스텀 메트릭
// ============================================================
const searchErrors = new Rate('search_errors');
const detailErrors = new Rate('detail_errors');
const brandFilterErrors = new Rate('brand_filter_errors');

const searchDuration = new Trend('search_duration');
const detailDuration = new Trend('detail_duration');

const totalRequests = new Counter('total_requests');

// ============================================================
// 테스트 시나리오 설정
// ============================================================
export const options = {
    // scenarios: {
    //     normal_traffic: {
    //         executor: 'constant-arrival-rate',
    //         rate: 20,
    //         timeUnit: '1s',
    //         duration: '30s',
    //         preAllocatedVUs: 50,
    //         maxVUs: 200,
    //     },
    //
    //     peak_traffic: {
    //         executor: 'ramping-arrival-rate',
    //         startRate: 20,
    //         timeUnit: '1s',
    //         stages: [
    //             {duration: '30s', target: 30},
    //             {duration: '1m', target: 60},
    //             {duration: '30s', target: 40},
    //         ],
    //         preAllocatedVUs: 100,
    //         maxVUs: 600,
    //         startTime: '30s',
    //     },
    // },
    scenarios: {
        product_test: {
            executor: 'ramping-arrival-rate',
            startRate: 5,
            timeUnit: '1s',
            stages: [
                { duration: '30s', target: 50 },
                { duration: '1m', target: 50 },
                { duration: '10s', target: 30 },
            ],
            preAllocatedVUs: 100,
            maxVUs: 500,
        },
    },

    thresholds: {
        // 전체 요청 실패율 < 5%
        'http_req_failed': ['rate<0.05'],

        // 95 퍼센트 응답시간 < 1.5초
        'http_req_duration': ['p(95)<1500', 'p(99)<3000'],

        // 검색 API 특정 임계값
        'search_duration': ['p(95)<1500'],
        'search_errors': ['rate<0.05'],

        // 상세 API 특정 임계값
        'detail_duration': ['p(95)<1000'],
        'detail_errors': ['rate<0.05'],
    },
    // scenarios: {
    //     find_breaking_point: {
    //         executor: 'ramping-arrival-rate',
    //         startRate: 10,
    //         timeUnit: '1s',
    //         stages: [
    //             { duration: '30s', target: 20 },
    //             { duration: '30s', target: 40 },
    //             { duration: '30s', target: 50 }, // 경계선
    //             // { duration: '30s', target: 80 },
    //             // { duration: '30s', target: 100 },
    //         ],
    //         preAllocatedVUs: 50,
    //         maxVUs: 500,
    //     },
    // },
    //
    // thresholds: {
    //     'http_req_failed': ['rate<0.01'], // 에러가 1%라도 나면 실패로 간주
    //     'http_req_duration': ['p(95)<1000'], // 응답 시간이 1초를 넘으면 실패
    // },
};

// ============================================================
// 테스트 데이터
// ============================================================
const BASE_URL = 'http://host.docker.internal:8080/api';

const SEARCH_KEYWORDS = [
    // 식품
    '신선한', '유기농', '무농약', '친환경', '국내산', '프리미엄',
    '달콤한', '새콤달콤한', '아삭한', '싱싱한',

    // 간편식
    '간편한', '손쉬운', '바로먹는', '즉석',

    // 음료
    '시원한', '청량한', '상쾌한',

    // 패션/뷰티
    '세련된', '모던한', '트렌디한', '촉촉한', '순한', '다용도', '편안한', '스마트'
];

const LEVEL1_CATEGORY_IDS = Array.from({ length: 26 }, (_, i) => i + 1);
const LEVEL2_CATEGORY_IDS = Array.from({ length: 200 }, (_, i) => i + 27);
const BRAND_IDS = Array.from({ length: 1465 }, (_, i) => i + 1);

const DELIVERY_TYPES = ['DAWN', 'NORMAL_PARCEL'];
const PACKAGING_TYPES = ['AMBIENT_TEMPERATURE', 'COLD', 'FROZEN'];

// ============================================================
// 메인 테스트 함수
// ============================================================
export default function () {
    const scenario = randomIntBetween(1, 10);

    if (scenario <= 4) {
        searchAndBrowse();
    } else if (scenario <= 7) {
        browseByCategoryAndBrand();
    } else if (scenario <= 9) {
        directProductView();
    } else {
        complexFilterSearch();
    }
}

// ============================================================
// 시나리오 1: 검색 → 목록 → 상세 (40%)
// ============================================================
function searchAndBrowse() {
    group('검색 후 상품 탐색', function () {
        // 1. 키워드 검색
        const keyword = getRealisticKeyword();
        // const keyword = randomItem(SEARCH_KEYWORDS);
        const searchRes = searchProducts({ keyword });

        if (!searchRes) return;
        sleep(randomIntBetween(1, 3)); // 검색 결과 시간

        // 2. 페이지네이션
        const pages = randomIntBetween(1, 3);
        for (let i = 0; i < pages; i++) {
            searchProducts({ keyword, page: i });
            sleep(randomIntBetween(2, 4));
        }

        // 3. 상품 상세 조회 (검색 결과에서 1~2개 클릭)
        const viewCount = randomIntBetween(1, 2);
        for (let i = 0; i < viewCount; i++) {
            const productId = getRealisticProductId();
            viewProductDetail(productId);
            sleep(randomIntBetween(3, 7));
        }
    });
}

// ============================================================
// 시나리오 2: 카테고리 + 브랜드 필터 (30%)
// ============================================================
function browseByCategoryAndBrand() {
    group('카테고리 필터링', function () {
        // 1. 카테고리 선택
        const categoryIds = (Math.random() < 0.7) ? [randomItem(LEVEL2_CATEGORY_IDS)] : [randomItem(LEVEL1_CATEGORY_IDS)];

        // 2. 해당 카테고리 상품 조회
        const listRes = searchProducts({ categoryIds });

        if (!listRes) return;
        sleep(randomIntBetween(1, 2));

        // 3. 브랜드 필터 조회
        getBrandFilters({ categoryIds });
        sleep(1);

        // 4. 브랜드 필터 적용
        const brand1 = randomItem(BRAND_IDS);
        let brand2 = randomItem(BRAND_IDS);
        while (brand2 === brand1) {
            brand2 = randomItem(BRAND_IDS);
        }
        const brandIds = [brand1, brand2];
        searchProducts({ categoryIds, brandIds });
        sleep(randomIntBetween(2, 4));

        // 5. 상품 상세 조회
        const productId = getRealisticProductId();
        viewProductDetail(productId);
        sleep(randomIntBetween(3, 6));
    });
}

// ============================================================
// 시나리오 3: 직접 상품 상세 조회 (20%)
// ============================================================
function directProductView() {
    group('직접 상품 조회', function () {
        const productId = getRealisticProductId();
        viewProductDetail(productId);
        sleep(randomIntBetween(5, 10));
    });
}

// ============================================================
// 시나리오 4: 복잡한 필터 검색 (10%)
// ============================================================
function complexFilterSearch() {
    group('복합 필터 검색', function () {
        // 1. 키워드 + 카테고리
        const keyword = getRealisticKeyword();
        // const keyword = randomItem(SEARCH_KEYWORDS);
        const categoryIds = (Math.random() < 0.7) ? [randomItem(LEVEL2_CATEGORY_IDS)] : [randomItem(LEVEL1_CATEGORY_IDS)];

        searchProducts({ keyword, categoryIds });
        sleep(randomIntBetween(1, 2));

        // 2. 브랜드 필터 조회
        getBrandFilters({ keyword, categoryIds });
        sleep(1);

        // 3. 브랜드 추가
        const brandIds = [randomItem(BRAND_IDS)];
        searchProducts({ keyword, categoryIds, brandIds });
        sleep(randomIntBetween(1, 2));

        // 4. 배송/포장 옵션 추가
        const deliveryTypes = [randomItem(DELIVERY_TYPES)];
        const packagingTypes = [randomItem(PACKAGING_TYPES)];

        searchProducts({
            keyword,
            categoryIds,
            brandIds,
            deliveryTypes,
            packagingTypes,
            excludeSoldOut: true
        });

        sleep(randomIntBetween(2, 4));

        // 5. 상품 상세 조회
        const productId = getRealisticProductId();
        viewProductDetail(productId);
        sleep(randomIntBetween(4, 8));
    });
}

// ============================================================
// Helper Functions - 80/20 패턴 (파레토 법칙)
// ============================================================

/**
 * 현실적인 상품 ID 생성 (80/20 법칙)
 * 80%의 트래픽이 상위 1000개 인기 상품에 집중
 */
function getRealisticProductId() {
    if (Math.random() < 0.8) {
        // 80%: 인기 상품 (1~1000)
        return randomIntBetween(1, 1000);
    }
    // 20%: 롱테일 상품 (1~100만)
    return randomIntBetween(1, 1000000);
}

/**
 * 현실적인 검색 키워드 (70/30 법칙)
 * 70%의 검색이 상위 5개 인기 키워드에 집중
 */
function getRealisticKeyword() {
    if (Math.random() < 0.7) {
        // 70%: 인기 키워드
        return randomItem(['신선한', '유기농', '무농약', '친환경', '국내산']);
    }
    // 30%: 나머지 키워드
    return randomItem(SEARCH_KEYWORDS);
}
// ============================================================
// API 호출 함수
// ============================================================
/**
 * 상품 검색
 */
function searchProducts(params = {}) {
    const queryParams = new URLSearchParams();

    // 키워드
    if (params.keyword) {
        queryParams.append('keyword', params.keyword);
    }

    // 카테고리 (다중)
    if (params.categoryIds && params.categoryIds.length > 0) {
        params.categoryIds.forEach(id => queryParams.append('categoryIds', id));
    }

    // 브랜드 (다중)
    if (params.brandIds && params.brandIds.length > 0) {
        params.brandIds.forEach(id => queryParams.append('brandIds', id));
    }

    // 배송 타입 (다중)
    if (params.deliveryTypes && params.deliveryTypes.length > 0) {
        params.deliveryTypes.forEach(type => queryParams.append('deliveryTypes', type));
    }

    // 포장 타입 (다중)
    if (params.packagingTypes && params.packagingTypes.length > 0) {
        params.packagingTypes.forEach(type => queryParams.append('packagingTypes', type));
    }

    // 품절 제외
    if (params.excludeSoldOut) {
        queryParams.append('excludeSoldOut', 'true');
    }

    // 페이지
    const page = params.page || 0;
    queryParams.append('page', page);
    queryParams.append('size', 20);
    queryParams.append('sort', 'id,desc');

    const url = `${BASE_URL}/products/search?${queryParams.toString()}`;

    const res = http.get(url, {
        tags: {name: 'SearchProducts'},
    });

    totalRequests.add(1);
    searchDuration.add(res.timings.duration);

    const success = check(res, {
        'search status 200': (r) => r.status === 200,
        'search response time < 1500ms': (r) => r.timings.duration < 1500,
    });

    searchErrors.add(!success);

    return success ? res : null;
}

/**
 * 상품 상세 조회
 */
function viewProductDetail(productId) {
    const url = `${BASE_URL}/products/${productId}`;

    const res = http.get(url, {
        tags: { name: 'ViewProductDetail' },
    });

    totalRequests.add(1);
    detailDuration.add(res.timings.duration);

    const success = check(res, {
        'detail status 200': (r) => r.status === 200,
        'detail response time < 1000ms': (r) => r.timings.duration < 1000,
    });

    detailErrors.add(!success);

    return success ? res : null;
}

/**
 * 브랜드 필터 조회
 */
function getBrandFilters(params = {}) {
    const queryParams = new URLSearchParams();

    if (params.keyword) {
        queryParams.append('keyword', params.keyword);
    }

    if (params.categoryIds && params.categoryIds.length > 0) {
        params.categoryIds.forEach(id => queryParams.append('categoryIds', id));
    }

    const url = `${BASE_URL}/products/search/brands?${queryParams.toString()}`;

    const res = http.get(url, {
        tags: { name: 'GetBrandFilters' },
    });

    totalRequests.add(1);

    const success = check(res, {
        'brand filter status 200': (r) => r.status === 200,
        'brand filter response time < 500ms': (r) => r.timings.duration < 500,
    });

    brandFilterErrors.add(!success);

    return success ? res : null;
}