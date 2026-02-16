package com.bugshot.domain.replay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 로컬 파일 시스템에 세션 리플레이 데이터를 저장하는 서비스
 * AWS S3 대신 맥미니의 로컬 디스크를 사용
 */
@Service
@Slf4j
public class LocalFileStorageService {

    @Value("${app.file-storage.base-path:./data/replays}")
    private String basePath;

    private Path baseDirectory;

    @PostConstruct
    public void init() {
        try {
            baseDirectory = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(baseDirectory);
            log.info("Local file storage initialized at: {}", baseDirectory);
        } catch (IOException e) {
            log.error("Failed to create base directory: {}", basePath, e);
            throw new RuntimeException("Failed to initialize local file storage", e);
        }
    }

    /**
     * 세션 리플레이 데이터를 로컬 파일 시스템에 저장
     *
     * @param projectId 프로젝트 ID
     * @param sessionId 세션 ID
     * @param data 압축된 리플레이 데이터
     * @return 파일 경로 (file:// URI 형식)
     */
    public String saveReplayData(String projectId, String sessionId, byte[] data) {
        try {
            // 날짜별 디렉토리 생성 (예: replays/project-123/2025/01/09/session-abc.json.gz)
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path projectDir = baseDirectory.resolve(projectId).resolve(date);
            Files.createDirectories(projectDir);

            // 파일명: sessionId.json.gz
            String fileName = sessionId + ".json.gz";
            Path filePath = projectDir.resolve(fileName);

            // 파일 저장
            Files.write(filePath, data);

            log.info("Session replay saved: path={}, size={} bytes", filePath, data.length);

            // file:// URI 반환
            return filePath.toUri().toString();

        } catch (IOException e) {
            log.error("Failed to save replay data: projectId={}, sessionId={}", projectId, sessionId, e);
            throw new RuntimeException("Failed to save replay data to local storage", e);
        }
    }

    /**
     * 세션 리플레이 데이터를 로컬 파일 시스템에서 읽기
     *
     * @param fileUri file:// URI
     * @return 압축된 리플레이 데이터
     */
    public byte[] getReplayData(String fileUri) {
        try {
            Path filePath = Paths.get(fileUri.replace("file://", "")).toAbsolutePath().normalize();

            // 보안: baseDirectory 외부 접근 방지
            if (!filePath.startsWith(baseDirectory)) {
                throw new SecurityException("Access denied: file path outside base directory");
            }

            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("File not found: " + fileUri);
            }

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("Failed to read replay data: fileUri={}", fileUri, e);
            throw new RuntimeException("Failed to read replay data from local storage", e);
        }
    }

    /**
     * 오래된 리플레이 데이터 삭제 (보관 기간 지난 파일)
     *
     * @param retentionDays 보관 기간 (일)
     */
    public void cleanupOldReplays(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            log.info("Cleaning up replays older than: {}", cutoffDate);

            // TODO: 날짜별 디렉토리를 순회하며 오래된 파일 삭제
            // 구현은 스케줄러에서 호출하도록 추후 추가

        } catch (Exception e) {
            log.error("Failed to cleanup old replays", e);
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean exists(String fileUri) {
        try {
            Path filePath = Paths.get(fileUri.replace("file://", "")).toAbsolutePath().normalize();
            return Files.exists(filePath) && filePath.startsWith(baseDirectory);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteReplayData(String fileUri) {
        try {
            Path filePath = Paths.get(fileUri.replace("file://", "")).toAbsolutePath().normalize();

            // 보안: baseDirectory 외부 접근 방지
            if (!filePath.startsWith(baseDirectory)) {
                throw new SecurityException("Access denied: file path outside base directory");
            }

            Files.deleteIfExists(filePath);
            log.info("Replay data deleted: {}", filePath);

        } catch (IOException e) {
            log.error("Failed to delete replay data: fileUri={}", fileUri, e);
            throw new RuntimeException("Failed to delete replay data from local storage", e);
        }
    }
}
