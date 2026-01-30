package com.example.BeGroom.product.test;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PerformanceMonitor {

    private final JdbcTemplate jdbcTemplate;
    private final HikariDataSource dataSource;

    private long startTime;
    private long startMemory;
    private int startActiveConnections;
    private final Map<String, Long> stepTimes = new LinkedHashMap<>();
    private final List<ChunkMetric> chunkMetrics = new ArrayList<>();

    private final Runtime runtime = Runtime.getRuntime();

    private long peakMemoryUsed = 0;

    @Data
    @AllArgsConstructor
    public static class ChunkMetric {
        private int index;
        private int size;
        private long durationMs;
    }

    public void start() {
        // GC 실행으로 메모리 상태 정리
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        startTime = System.currentTimeMillis();
        startMemory = runtime.totalMemory() - runtime.freeMemory();
        startActiveConnections = dataSource.getHikariPoolMXBean().getActiveConnections();

        peakMemoryUsed = 0;

        log.info("===== 성능 측정 시작 =====");
    }

    public void recordStep(String stepName) {
        long elapsed = System.currentTimeMillis() - startTime;
        stepTimes.put(stepName, elapsed);
        log.info("{} 완료: {}ms", stepName, elapsed);
    }

    public void recordChunk(int chunkIndex, int size, long durationMs) {
        chunkMetrics.add(new ChunkMetric(chunkIndex, size, durationMs));

        long currentMemory = runtime.totalMemory() - runtime.freeMemory();
        long currentMemoryUsed = currentMemory - startMemory;
        if (currentMemoryUsed > peakMemoryUsed) {
            peakMemoryUsed = currentMemoryUsed;
        }

        if (chunkIndex % 10 == 0) {
            double speed = size / (durationMs / 1000.0);
            log.info("청크 {} 처리: {}개, {}ms, {}/sec", chunkIndex, size, durationMs, String.format("%.2f", speed));
        }
    }

    public void printReport() {
        long totalDurationMs = System.currentTimeMillis() - startTime;
        long totalDurationSec = totalDurationMs / 1000;
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory() - startMemory) / (1024 * 1024);

        log.info("\n" + "=".repeat(100));
        log.info("성능 측정 리포트");
        log.info("=".repeat(100));

        // 전체 소요 시간
        printTotalDuration(totalDurationSec, totalDurationMs);

        // 단계별 시간
        printStepTimes();

        // 메모리 사용량
        printMemoryUsage(memoryUsed);

        // DB 커넥션 상태
        printDatabaseConnections();

        // 테이블 크기
        printTableSize();

        // 청크별 처리 속도
        printChunkMetrics();

        log.info("=".repeat(100) + "\n");
    }

    private void printTotalDuration(long totalDurationSec, long totalDurationMs) {
        log.info("\n전체 소요 시간");
        log.info(" - {}초 {}분 {}초", totalDurationSec, totalDurationSec / 60, totalDurationSec % 60);
        log.info(" - 정확한 시간: {}ms", totalDurationMs);
    }

    private void printStepTimes() {
        if (stepTimes.isEmpty()) return;

        log.info("단계별 시간");
        long previousTime = 0;
        int stepNumber = 1;

        for (Map.Entry<String, Long> entry : stepTimes.entrySet()) {
            long stepDuration = entry.getValue() - previousTime;
            double stepDurationSec = stepDuration / 1000.0;

            log.info(" {}. {}: {}ms ({}초)", stepNumber++, entry.getKey(), stepDuration, String.format("%.2f", stepDurationSec));

            previousTime = entry.getValue();
        }
    }

    private void printMemoryUsage(long memoryUsed) {
        log.info("\n메모리 사용량");
        log.info("  - 현재 메모리: {}MB (GC 후 남은 양)", memoryUsed);
        log.info("  - 최대 메모리: {}MB (실제 사용한 최댓값)", peakMemoryUsed / (1024 * 1024));
        log.info("  - 현재 할당된 메모리: {}MB", runtime.totalMemory() / (1024 * 1024));
        log.info("  - 최대 사용 가능 메모리: {}MB", runtime.maxMemory() / (1024 * 1024));
        log.info("  - 여유 메모리: {}MB", runtime.freeMemory() / (1024 * 1024));
    }

    private void printDatabaseConnections() {
        log.info("\nDB 커넥션 상태");
        log.info("  - 활성 커넥션: {}", dataSource.getHikariPoolMXBean().getActiveConnections());
        log.info("  - 유휴 커넥션: {}", dataSource.getHikariPoolMXBean().getIdleConnections());
        log.info("  - 총 커넥션: {}", dataSource.getHikariPoolMXBean().getTotalConnections());
        log.info("  - 대기 중인 스레드: {}", dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        log.info("  - 최대 풀 크기: {}", dataSource.getMaximumPoolSize());
        log.info("  - 최소 유휴 커넥션: {}", dataSource.getMinimumIdle());
    }

    private void printTableSize() {
        try {
            // Product 테이블 개수
            Long productCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product",
                Long.class
            );

            // Product 테이블 크기
            Long productTableSize = jdbcTemplate.queryForObject(
                """
                SELECT ROUND(((data_length + index_length) / 1024 / 1024), 2)
                FROM information_schema.TABLES
                WHERE table_schema = DATABASE()
                AND table_name = 'product'
                """,
                Long.class
            );

            // ProductDetail 테이블 개수
            Long detailCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_detail",
                Long.class
            );

            // ProductDetail 테이블 크기
            Long detailTableSize = jdbcTemplate.queryForObject(
                """
                SELECT ROUND(((data_length + index_length) / 1024 / 1024), 2)
                FROM information_schema.TABLES
                WHERE table_schema = DATABASE()
                AND table_name = 'product_detail'
                """,
                Long.class
            );

            log.info("\n데이터베이스 테이블 크기");
            log.info("  - Product: {}개, {}MB",
                String.format("%,d", productCount),
                productTableSize);
            log.info("  - ProductDetail: {}개, {}MB",
                String.format("%,d", detailCount),
                detailTableSize);

            // 전체 데이터베이스 크기
            Long totalDbSize = jdbcTemplate.queryForObject(
                """
                SELECT ROUND(SUM((data_length + index_length) / 1024 / 1024), 2)
                FROM information_schema.TABLES
                WHERE table_schema = DATABASE()
                """,
                Long.class
            );

            log.info("  - 전체 DB 크기: {}MB", totalDbSize);

        } catch (Exception e) {
            log.warn("테이블 크기 조회 실패: {}", e.getMessage());
        }
    }

    private void printChunkMetrics() {
        if (chunkMetrics.isEmpty()) {
            return;
        }

        log.info("\n청크별 처리 속도");

        int totalChunks = chunkMetrics.size();

        // 처음 5개 청크
        log.info("[처음 5개 청크]");
        chunkMetrics.stream()
            .limit(5)
            .forEach(m -> {
                double speed = m.size / (m.durationMs / 1000.0);
                log.info("청크 {}: {}개, {}ms, {}/sec",
                    m.index,
                    String.format("%,d", m.size),
                    m.durationMs,
                    String.format("%.2f", speed));
            });

        // 마지막 5개 청크 (전체가 5개 이하면 생략)
        if (totalChunks > 10) {
            log.info("  ...");
        }

        if (totalChunks > 5) {
            log.info("[마지막 5개 청크]");
            chunkMetrics.stream()
                .skip(Math.max(0, totalChunks - 5))
                .forEach(m -> {
                    double speed = m.size / (m.durationMs / 1000.0);
                    log.info("청크 {}: {}개, {}ms, {}/sec",
                        m.index,
                        String.format("%,d", m.size),
                        m.durationMs,
                        String.format("%.2f", speed));
                });
        }

        // 통계 정보
        printChunkStatistics();
    }

    private void printChunkStatistics() {
        if (chunkMetrics.isEmpty()) {
            return;
        }

        // 평균 속도
        double avgSpeed = chunkMetrics.stream()
            .mapToDouble(m -> m.size / (m.durationMs / 1000.0))
            .average()
            .orElse(0);

        // 최고 속도
        double maxSpeed = chunkMetrics.stream()
            .mapToDouble(m -> m.size / (m.durationMs / 1000.0))
            .max()
            .orElse(0);

        // 최저 속도
        double minSpeed = chunkMetrics.stream()
            .mapToDouble(m -> m.size / (m.durationMs / 1000.0))
            .min()
            .orElse(0);

        // 총 처리량
        int totalProcessed = chunkMetrics.stream()
            .mapToInt(ChunkMetric::getSize)
            .sum();

        log.info("[통계]");
        log.info("총 청크 수: {}개", chunkMetrics.size());
        log.info("총 처리량: {}개", String.format("%,d", totalProcessed));
        log.info("평균 속도: {}/sec", String.format("%.2f", avgSpeed));
        log.info("최고 속도: {}/sec", String.format("%.2f", maxSpeed));
        log.info("최저 속도: {}/sec", String.format("%.2f", minSpeed));
    }

    public void reset() {
        stepTimes.clear();
        chunkMetrics.clear();
        startTime = 0;
        startMemory = 0;
        startActiveConnections = 0;
    }

}
