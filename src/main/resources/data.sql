INSERT INTO product_option (option_type, option_value, display_name, is_active) VALUES
    ('packaging', 'AMBIENT_TEMPERATURE', '상온', TRUE),
    ('packaging', 'COLD', '냉장', TRUE),
    ('packaging', 'FROZEN', '냉동', TRUE),
    ('delivery', 'DAWN', '구름배송', TRUE),
    ('delivery', 'NORMAL_PARCEL', '택배배송 · 무료배송', TRUE);


-- ==========================================
-- 1. 대분류 데이터 삽입 (Level 1)
-- ==========================================
INSERT INTO category (external_category_id, category_name, level, sort_order, is_active) VALUES
     ('907', '채소', 1, 1, true),
     ('908', '과일·견과·쌀', 1, 2, true),
     ('909', '수산·해산·건어물', 1, 3, true),
     ('910', '정육·가공육·달걀', 1, 4, true),
     ('911', '국·반찬·메인요리', 1, 5, true),
     ('912', '간편식·밀키트·샐러드', 1, 6, true),
     ('913', '면·양념·오일', 1, 7, true),
     ('914', '생수·음료', 1, 8, true),
     ('383', '커피·차', 1, 9, true),
     ('249', '간식·과자·떡', 1, 10, true),
     ('915', '베이커리', 1, 11, true),
     ('018', '유제품', 1, 12, true),
     ('032', '건강식품', 1, 13, true),
     ('722', '와인·위스키·데낄라', 1, 14, true),
     ('251', '전통주', 1, 15, true),
     ('188', '패션', 1, 16, true),
     ('916', '주방용품', 1, 17, true),
     ('918', '생활용품·리빙', 1, 18, true),
     ('085', '가전제품', 1, 19, true),
     ('019', '가구·인테리어', 1, 20, true),
     ('919', '유아동', 1, 21, true),
     ('991', '반려동물', 1, 22, true),
     ('020', '스포츠·레저·캠핑', 1, 23, true),
     ('233', '스킨케어·메이크업', 1, 24, true),
     ('012', '헤어·바디·구강', 1, 25, true),
     ('365', '럭셔리뷰티', 1, 26, true);

-- ==========================================
-- 2. 중분류 데이터 삽입 (Level 2)
-- ==========================================

-- 1. 채소 (907)
SET @cat_907 = (SELECT id FROM category WHERE external_category_id = '907');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('907001', @cat_907, '고구마·감자·당근', 2, 2, true),
    ('907002', @cat_907, '시금치·쌈채소·나물', 2, 3, true), ('907003', @cat_907, '브로콜리·파프리카·양배추', 2, 4, true),
    ('907005', @cat_907, '양파·대파·마늘·배추', 2, 5, true), ('907004', @cat_907, '오이·호박·고추', 2, 6, true),
    ('907007', @cat_907, '냉동·이색·간편채소', 2, 7, true), ('907006', @cat_907, '콩나물·버섯', 2, 8, true), ('907008', @cat_907, '친환경', 2, 1, true);

-- 2. 과일·견과·쌀 (908)
SET @cat_908 = (SELECT id FROM category WHERE external_category_id = '908');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('908006', @cat_908, '제철과일', 2, 2, true),
    ('908001', @cat_908, '국산과일', 2, 3, true), ('908002', @cat_908, '수입과일', 2, 4, true),
    ('908007', @cat_908, '간편과일', 2, 5, true), ('908003', @cat_908, '냉동·건과일', 2, 6, true),
    ('908004', @cat_908, '견과류', 2, 7, true), ('908005', @cat_908, '쌀·잡곡', 2, 8, true), ('908008', @cat_908, '친환경', 2, 1, true);

-- 3. 수산·해산·건어물 (909)
SET @cat_909 = (SELECT id FROM category WHERE external_category_id = '909');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('909001', @cat_909, '생선류', 2, 2, true),
    ('909009', @cat_909, '굴비·반건류', 2, 3, true), ('909011', @cat_909, '연어·참치', 2, 4, true),
    ('909012', @cat_909, '회·탕류', 2, 5, true), ('909002', @cat_909, '오징어·낙지·문어', 2, 6, true),
    ('909004', @cat_909, '해산물·전복·조개류', 2, 7, true), ('909003', @cat_909, '새우·게·랍스터', 2, 8, true),
    ('909007', @cat_909, '수산가공품', 2, 9, true), ('909013', @cat_909, '명란', 2, 10, true),
    ('909014', @cat_909, '젓갈·장류', 2, 11, true), ('909015', @cat_909, '간편구이', 2, 12, true),
    ('909005', @cat_909, '김·미역·해조류', 2, 13, true), ('909006', @cat_909, '멸치·황태·다시팩', 2, 14, true),
    ('909016', @cat_909, '조미오징어·어포·쥐포', 2, 15, true), ('909010', @cat_909, '제철수산', 2, 1, true);

-- 4. 정육·가공육·달걀 (910)
SET @cat_910 = (SELECT id FROM category WHERE external_category_id = '910');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('910001', @cat_910, '국내산 소고기', 2, 1, true), ('910007', @cat_910, '수입산 소고기', 2, 2, true),
    ('910002', @cat_910, '국내산 돼지고기', 2, 3, true), ('910009', @cat_910, '수입산 돼지고기·양고기', 2, 4, true),
    ('910004', @cat_910, '닭·오리고기', 2, 5, true), ('910010', @cat_910, '식단관리용 가공육', 2, 6, true),
    ('910003', @cat_910, '양념육', 2, 7, true), ('910011', @cat_910, '돈까스·떡갈비·함박', 2, 8, true),
    ('910012', @cat_910, '소시지·베이컨·하몽', 2, 9, true), ('910005', @cat_910, '달걀·가공란', 2, 10, true);

-- 5. 국·반찬·메인요리 (911)
SET @cat_911 = (SELECT id FROM category WHERE external_category_id = '911');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('911001', @cat_911, '국·탕·찌개', 2, 1, true), ('911002', @cat_911, '밑반찬', 2, 2, true),
    ('911003', @cat_911, '김치·젓갈·장류', 2, 3, true), ('911005', @cat_911, '두부·어묵·부침개', 2, 4, true),
    ('911006', @cat_911, '메인요리', 2, 5, true);

-- 6. 간편식·밀키트·샐러드 (912)
SET @cat_912 = (SELECT id FROM category WHERE external_category_id = '912');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('912011', @cat_912, '밀키트', 2, 1, true), ('912001', @cat_912, '샐러드·샌드위치', 2, 2, true),
    ('912002', @cat_912, '선식·시리얼', 2, 3, true), ('912003', @cat_912, '도시락·밥류', 2, 4, true),
    ('912004', @cat_912, '짜장·짬뽕·파스타·면류', 2, 5, true), ('912005', @cat_912, '떡볶이·튀김·순대', 2, 6, true),
    ('912008', @cat_912, '치킨·피자·핫도그·만두', 2, 7, true);

-- 7. 면·양념·오일 (913)
SET @cat_913 = (SELECT id FROM category WHERE external_category_id = '913');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('913009', @cat_913, '라면', 2, 1, true), ('913001', @cat_913, '파스타·면류·조리용 떡', 2, 2, true),
    ('913002', @cat_913, '밀가루·가루·믹스', 2, 3, true), ('913010', @cat_913, '햄·통조림·병조림', 2, 4, true),
    ('913011', @cat_913, '죽·스프·카레', 2, 5, true), ('913003', @cat_913, '양념·액젓·장류', 2, 6, true),
    ('913006', @cat_913, '식용유·참기름·오일', 2, 7, true), ('913007', @cat_913, '식초·소스·드레싱', 2, 8, true),
    ('913008', @cat_913, '소금·설탕·향신료', 2, 9, true), ('913013', @cat_913, '조미료', 2, 10, true),
    ('913005', @cat_913, '면·양념·오일 선물세트', 2, 11, true);

-- 8. 생수·음료 (914)
SET @cat_914 = (SELECT id FROM category WHERE external_category_id = '914');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('914001', @cat_914, '생수/얼음', 2, 1, true), ('914002', @cat_914, '탄산수', 2, 2, true),
    ('914003', @cat_914, '탄산·스포츠음료', 2, 3, true), ('914004', @cat_914, '과일·야채음료', 2, 4, true),
    ('914005', @cat_914, '차음료', 2, 5, true), ('914006', @cat_914, '어린이음료·선물세트', 2, 6, true);

-- 9. 커피·차 (383)
SET @cat_383 = (SELECT id FROM category WHERE external_category_id = '383');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('383001', @cat_383, '원두커피', 2, 1, true), ('383002', @cat_383, '드립백/커피백', 2, 2, true),
    ('383003', @cat_383, '캡슐커피', 2, 3, true), ('383004', @cat_383, '콜드브루', 2, 4, true),
    ('383005', @cat_383, '커피음료', 2, 5, true), ('383006', @cat_383, '인스턴트 커피', 2, 6, true),
    ('383007', @cat_383, '곡물차/전통차', 2, 7, true), ('383008', @cat_383, '홍차/녹차/보이차', 2, 8, true),
    ('383009', @cat_383, '허브차/꽃차/과일차', 2, 9, true), ('383010', @cat_383, '코코아/밀크티/기타 차', 2, 10, true),
    ('383011', @cat_383, '액상차/청', 2, 11, true), ('383012', @cat_383, '커피/차 선물세트', 2, 12, true);

-- 10. 간식·과자·떡 (249)
SET @cat_249 = (SELECT id FROM category WHERE external_category_id = '249');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('249005', @cat_249, '과자·간식', 2, 1, true), ('249006', @cat_249, '쿠키·비스킷·크래커', 2, 2, true),
    ('249002', @cat_249, '초콜릿·젤리·캔디', 2, 3, true), ('249008', @cat_249, '유아 과자·간식', 2, 4, true),
    ('249009', @cat_249, '대용량 과자·간식', 2, 5, true), ('249003', @cat_249, '떡·한과', 2, 6, true);

-- 11. 베이커리 (915)
SET @cat_915 = (SELECT id FROM category WHERE external_category_id = '915');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('915011', @cat_915, '식빵·모닝빵·베이글', 2, 2, true),
    ('915007', @cat_915, '간식빵', 2, 3, true), ('915008', @cat_915, '타르트·파이', 2, 4, true),
    ('915009', @cat_915, '디저트', 2, 5, true), ('915012', @cat_915, '케이크', 2, 6, true),
    ('915010', @cat_915, '잼·스프레드', 2, 7, true), ('915014', @cat_915, '이 주의 추천 베이커리', 2, 1, true);

-- 12. 유제품 (018)
SET @cat_018 = (SELECT id FROM category WHERE external_category_id = '018');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('018001', @cat_018, '우유·두유', 2, 1, true), ('018002', @cat_018, '요거트·생크림', 2, 2, true),
    ('018003', @cat_018, '자연치즈', 2, 3, true), ('018004', @cat_018, '가공치즈', 2, 4, true),
    ('018005', @cat_018, '버터', 2, 5, true), ('018006', @cat_018, '아이스크림', 2, 6, true);

-- 13. 건강식품 (032)
SET @cat_032 = (SELECT id FROM category WHERE external_category_id = '032');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('032009', @cat_032, '비타민·미네랄', 2, 1, true), ('032001', @cat_032, '건강즙·건강음료', 2, 2, true),
    ('032002', @cat_032, '홍삼·인삼', 2, 3, true), ('032010', @cat_032, '꿀·과일청', 2, 4, true),
    ('032003', @cat_032, '영양제', 2, 5, true), ('032004', @cat_032, '유산균', 2, 6, true),
    ('032005', @cat_032, '건강분말·건강환', 2, 7, true), ('032011', @cat_032, '체중관리', 2, 8, true),
    ('032012', @cat_032, '프로틴', 2, 9, true), ('032014', @cat_032, '이너뷰티', 2, 10, true),
    ('032007', @cat_032, '유아동·키즈', 2, 11, true);

-- 14. 와인·위스키·데낄라 (722)
SET @cat_722 = (SELECT id FROM category WHERE external_category_id = '722');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('722021', @cat_722, 'CU BAR 픽업', 2, 1, true), ('722027', @cat_722, '라빈리커 픽업', 2, 2, true),
    ('722025', @cat_722, '와인앤모어 픽업', 2, 3, true), ('722023', @cat_722, '레드텅 와인샵 픽업', 2, 4, true),
    ('722014', @cat_722, '레드와인', 2, 5, true), ('722012', @cat_722, '화이트·로제와인', 2, 6, true),
    ('722013', @cat_722, '샴페인·스파클링', 2, 7, true), ('722016', @cat_722, '위스키·리큐르', 2, 8, true),
    ('722031', @cat_722, '사케', 2, 9, true), ('722017', @cat_722, '맥주·하이볼', 2, 10, true),
    ('722015', @cat_722, '스위트와인', 2, 11, true), ('722022', @cat_722, '고량주', 2, 12, true),
    ('722029', @cat_722, '데낄라', 2, 13, true), ('722019', @cat_722, '논알콜·무알콜', 2, 14, true),
    ('722020', @cat_722, '와인·위스키용품', 2, 15, true);

-- 15. 전통주 (251)
SET @cat_251 = (SELECT id FROM category WHERE external_category_id = '251');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('251001', @cat_251, '막걸리·탁주', 2, 1, true), ('251002', @cat_251, '증류주·약주·청주', 2, 2, true),
    ('251003', @cat_251, '과실주·리큐르', 2, 3, true), ('251005', @cat_251, '라빈리커 픽업', 2, 4, true),
    ('251004', @cat_251, '전통주 선물세트', 2, 5, true);

-- 16. 패션 (188)
SET @cat_188 = (SELECT id FROM category WHERE external_category_id = '188');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('188014', @cat_188, '여성의류', 2, 1, true), ('188015', @cat_188, '언더웨어·라운지웨어', 2, 2, true),
    ('188003', @cat_188, '슈즈', 2, 3, true), ('188016', @cat_188, '가방·지갑', 2, 4, true),
    ('188005', @cat_188, '주얼리·액세서리', 2, 5, true), ('188018', @cat_188, '애슬레저·스윔웨어·스포츠', 2, 6, true),
    ('188017', @cat_188, '스카프·아이웨어·양말·소품', 2, 7, true), ('188019', @cat_188, '키즈패션', 2, 8, true),
    ('188020', @cat_188, '남성의류', 2, 9, true), ('188023', @cat_188, '명품', 2, 10, true),
    ('188021', @cat_188, 'Groom Only', 2, 11, true);

-- 17. 주방용품 (916)
SET @cat_916 = (SELECT id FROM category WHERE external_category_id = '916');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('916006', @cat_916, '일회용품·주방잡화', 2, 1, true), ('916007', @cat_916, '주방·조리도구', 2, 2, true),
    ('916008', @cat_916, '냄비·팬·솥', 2, 3, true), ('916009', @cat_916, '식기·그릇', 2, 4, true),
    ('916010', @cat_916, '컵·잔·물병', 2, 5, true), ('916012', @cat_916, '밀폐·보관·저장용기', 2, 6, true),
    ('916014', @cat_916, '커트러리', 2, 7, true), ('916013', @cat_916, '주방수납정리', 2, 8, true),
    ('916011', @cat_916, '보관용기·텀블러', 2, 9, true);

-- 18. 생활용품·리빙 (918)
SET @cat_918 = (SELECT id FROM category WHERE external_category_id = '918');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('918007', @cat_918, '화장지·물티슈', 2, 1, true), ('918018', @cat_918, '세제', 2, 2, true),
    ('918019', @cat_918, '탈취·제습·방충제', 2, 3, true), ('918020', @cat_918, '청소용품', 2, 4, true),
    ('918021', @cat_918, '욕실용품', 2, 5, true), ('918022', @cat_918, '세탁용품', 2, 6, true),
    ('918023', @cat_918, '수납정리용품', 2, 7, true), ('918010', @cat_918, '의약외품·마스크', 2, 8, true),
    ('918016', @cat_918, '여성·위생용품', 2, 9, true), ('918011', @cat_918, '취미·문구·오피스', 2, 10, true);

-- 19. 가전제품 (085)
SET @cat_085 = (SELECT id FROM category WHERE external_category_id = '085');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('085001', @cat_085, '생활가전', 2, 1, true), ('085002', @cat_085, '주방가전', 2, 2, true),
    ('085003', @cat_085, '계절가전', 2, 3, true), ('085004', @cat_085, '디지털·PC', 2, 4, true),
    ('085005', @cat_085, '대형·설치가전', 2, 5, true);

-- 20. 가구·인테리어 (019)
SET @cat_019 = (SELECT id FROM category WHERE external_category_id = '019');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('019021', @cat_019, '이불·베개·패드', 2, 1, true), ('019017', @cat_019, '침대·매트리스·토퍼', 2, 2, true),
    ('019019', @cat_019, '홈데코·조명·거울', 2, 3, true), ('019020', @cat_019, '홈패브릭', 2, 4, true),
    ('019016', @cat_019, '행거·옷장', 2, 5, true), ('019013', @cat_019, '테이블·식탁·책상', 2, 6, true),
    ('019014', @cat_019, '소파·의자', 2, 7, true), ('019015', @cat_019, '서랍·선반·진열장', 2, 8, true),
    ('019018', @cat_019, '화장대·콘솔', 2, 9, true);

-- 21. 유아동 (919)
SET @cat_919 = (SELECT id FROM category WHERE external_category_id = '919');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('919008', @cat_919, '이유식 재료', 2, 1, true), ('919011', @cat_919, '분유·간편 이유식', 2, 2, true),
    ('919013', @cat_919, '간식·음식', 2, 3, true), ('919012', @cat_919, '건강식품', 2, 4, true),
    ('919009', @cat_919, '이유·수유용품', 2, 5, true), ('919010', @cat_919, '세제·위생용품', 2, 6, true),
    ('919015', @cat_919, '스킨·구강케어', 2, 7, true), ('919016', @cat_919, '완구·잡화류', 2, 8, true),
    ('919017', @cat_919, '유아동패션', 2, 9, true), ('919018', @cat_919, '기저귀·물티슈', 2, 10, true);

-- 22. 반려동물 (991)
SET @cat_991 = (SELECT id FROM category WHERE external_category_id = '991');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('991002', @cat_991, '강아지 주식', 2, 1, true), ('991001', @cat_991, '강아지 간식·영양제', 2, 2, true),
    ('991004', @cat_991, '고양이 주식', 2, 3, true), ('991003', @cat_991, '고양이 간식·영양제', 2, 4, true),
    ('991011', @cat_991, '배변용품', 2, 5, true), ('991012', @cat_991, '미용·목욕용품', 2, 6, true),
    ('991013', @cat_991, '펫 의류·외출용품', 2, 7, true), ('991014', @cat_991, '급식기·급수기', 2, 8, true),
    ('991015', @cat_991, '하우스용품', 2, 9, true), ('991009', @cat_991, '장난감', 2, 10, true);

-- 23. 스포츠·레저·캠핑 (020)
SET @cat_020 = (SELECT id FROM category WHERE external_category_id = '020');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('020001', @cat_020, '골프', 2, 1, true), ('020006', @cat_020, '휘트니스', 2, 2, true),
    ('020007', @cat_020, '캠핑', 2, 3, true);

-- 24. 스킨케어·메이크업 (233)
SET @cat_233 = (SELECT id FROM category WHERE external_category_id = '233');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('233001', @cat_233, '스킨·미스트·패드', 2, 1, true), ('233002', @cat_233, '에센스·앰플·로션', 2, 2, true),
    ('233003', @cat_233, '크림·오일', 2, 3, true), ('233004', @cat_233, '클렌징', 2, 4, true),
    ('233005', @cat_233, '마스크팩', 2, 5, true), ('233006', @cat_233, '선케어', 2, 6, true),
    ('233007', @cat_233, '베이스메이크업', 2, 7, true), ('233010', @cat_233, '립메이크업', 2, 8, true),
    ('233011', @cat_233, '아이메이크업', 2, 9, true), ('233008', @cat_233, '맨즈케어', 2, 10, true),
    ('233009', @cat_233, '뷰티소품·기기', 2, 11, true), ('233013', @cat_233, '스킨케어세트', 2, 12, true);

-- 25. 헤어·바디·구강 (012)
SET @cat_012 = (SELECT id FROM category WHERE external_category_id = '012');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('012010', @cat_012, '구강·면도', 2, 1, true), ('012009', @cat_012, '샴푸·컨디셔너', 2, 2, true),
    ('012014', @cat_012, '트리트먼트·팩', 2, 3, true), ('012015', @cat_012, '헤어에센스·염모', 2, 4, true),
    ('012008', @cat_012, '바디워시·스크럽', 2, 5, true), ('012016', @cat_012, '바디로션·크림', 2, 6, true),
    ('012017', @cat_012, '핸드·립·데오', 2, 7, true), ('012018', @cat_012, '향수·디퓨저', 2, 8, true),
    ('012012', @cat_012, '헤어·바디소품', 2, 9, true), ('012001', @cat_012, '헤어·바디·구강케어세트', 2, 10, true);

-- 26. 럭셔리뷰티 (365)
SET @cat_365 = (SELECT id FROM category WHERE external_category_id = '365');
INSERT INTO category (external_category_id, parent_id, category_name, level, sort_order, is_active) VALUES
    ('365001', @cat_365, '스킨케어', 2, 1, true), ('365002', @cat_365, '메이크업', 2, 2, true),
    ('365003', @cat_365, '바디케어', 2, 3, true), ('365004', @cat_365, '헤어케어', 2, 4, true),
    ('365005', @cat_365, '프레그랑스', 2, 5, true), ('365009', @cat_365, '클렌징', 2, 6, true),
    ('365010', @cat_365, '선케어', 2, 7, true), ('365013', @cat_365, '남성케어', 2, 8, true);