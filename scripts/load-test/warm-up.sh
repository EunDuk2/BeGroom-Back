#!/bin/bash

echo "=== 캐시 워밍업 시작 ==="

BASE_URL="http://localhost:8080/api"

# 인기 검색어 워밍업
echo "검색 키워드 워밍업 중..."
for keyword in "신선한" "유기농" "무농약" "친환경" "국내산"; do
  for page in 0 1 2; do
    curl -s "${BASE_URL}/products/search?keyword=${keyword}&page=${page}&size=20" > /dev/null
    echo "✓ ${keyword} - 페이지 ${page}"
    sleep 0.5
  done
done

# 인기 상품 워밍업
echo "인기 상품 워밍업 중..."
for id in {1..1000}; do
  curl -s "${BASE_URL}/products/${id}" > /dev/null
  if [ $((id % 100)) -eq 0 ]; then
    echo "✓ 상품 ${id}/1000"
  fi
done

echo "=== 워밍업 완료 ==="