package com.example.BeGroom.product.dto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public record ProductSearchCacheKey(
    String keyword,
    List<Long> categoryIds,
    List<Long> brandIds,
    Boolean excludeSoldOut,
    List<String> deliveryTypes,
    List<String> packagingTypes
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static ProductSearchCacheKey from(ProductSearchCondition condition) {
        return new ProductSearchCacheKey(
            condition.keyword(),
            sortedList(condition.categoryIds()),
            sortedList(condition.brandIds()),
            condition.excludeSoldOut(),
            sortedList(condition.deliveryTypes()),
            sortedList(condition.packagingTypes())
        );
    }

    public String toCacheKey() {
        StringBuilder sb = new StringBuilder();

        if (keyword != null && !keyword.isBlank()) {
            sb.append("kw:").append(keyword).append(";");
        }

        appendListIfNotEmpty(sb, "cat", categoryIds);
        appendListIfNotEmpty(sb, "brand", brandIds);

        if (Boolean.TRUE.equals(excludeSoldOut)) {
            sb.append("soldOut:false;");
        }

        appendListIfNotEmpty(sb, "delivery", deliveryTypes);
        appendListIfNotEmpty(sb, "packaging", packagingTypes);

        return sb.toString();
    }

    private static <T extends Comparable<T>> List<T> sortedList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream().sorted().distinct().toList();
    }

    private void appendListIfNotEmpty(StringBuilder sb, String prefix, List<?> list) {
        if (list != null && !list.isEmpty()) {
            sb.append(prefix).append(":")
                .append(list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")))
                .append(";");
        }
    }

    @Override
    public String toString() {
        return toCacheKey();
    }
}

