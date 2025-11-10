package com.error.monitor.service;

import com.error.monitor.api.ingest.dto.IngestRequest;
import com.error.monitor.domain.error.ErrorOccurrence;
import com.error.monitor.domain.error.ErrorOccurrenceRepository;
import com.error.monitor.domain.replay.SessionReplay;
import com.error.monitor.domain.replay.SessionReplayRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionReplayService {

    private final SessionReplayRepository replayRepository;
    private final ErrorOccurrenceRepository occurrenceRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${app.cloudflare.r2.bucket-name}")
    private String bucketName;

    @Async
    @Transactional
    public void saveReplay(String projectId, String occurrenceId, IngestRequest.SessionReplayData replayData) {
        try {
            log.info("Saving session replay: occurrence={}, sessionId={}",
                occurrenceId, replayData.getSessionId());

            // Convert replay data to JSON
            String jsonData = objectMapper.writeValueAsString(replayData.getEvents());

            // Compress with GZIP
            byte[] compressedData = compressData(jsonData);

            // Upload to Cloudflare R2 (S3 compatible)
            String s3Key = generateS3Key(projectId, replayData.getSessionId());
            String r2Url = uploadToR2(s3Key, compressedData);

            // Save metadata to database
            ErrorOccurrence occurrence = occurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new IllegalArgumentException("Error occurrence not found: " + occurrenceId));

            SessionReplay replay = SessionReplay.builder()
                .project(occurrence.getError().getProject())
                .errorOccurrence(occurrence)
                .sessionId(replayData.getSessionId())
                .replayDataUrl(r2Url)
                .durationMs(replayData.getDurationMs())
                .eventsCount(replayData.getEvents() != null ? replayData.getEvents().size() : 0)
                .fileSizeBytes(compressedData.length)
                .build();

            replay = replayRepository.save(replay);

            // Attach replay to occurrence
            occurrence.attachSessionReplay(replay.getId());
            occurrenceRepository.save(occurrence);

            log.info("Session replay saved: id={}, size={} bytes", replay.getId(), compressedData.length);

        } catch (Exception e) {
            log.error("Failed to save session replay", e);
        }
    }

    private byte[] compressData(String data) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(data.getBytes());
        }
        return byteStream.toByteArray();
    }

    private String generateS3Key(String projectId, String sessionId) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("replays/%s/%s/%s.json.gz", projectId, date, sessionId);
    }

    private String uploadToR2(String key, byte[] data) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/gzip")
                .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));

            // Return the R2 URL (S3 compatible format)
            return String.format("r2://%s/%s", bucketName, key);
        } catch (Exception e) {
            log.error("Failed to upload to Cloudflare R2: key={}", key, e);
            throw new RuntimeException("Cloudflare R2 upload failed", e);
        }
    }

    @Transactional(readOnly = true)
    public SessionReplay getReplayByOccurrence(String occurrenceId) {
        return replayRepository.findByErrorOccurrenceId(occurrenceId)
            .orElseThrow(() -> new IllegalArgumentException("Session replay not found for occurrence: " + occurrenceId));
    }

    @Transactional(readOnly = true)
    public SessionReplay getReplayBySessionId(String sessionId) {
        return replayRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session replay not found for session: " + sessionId));
    }

    /**
     * 세션 리플레이 조회 (에러 ID 기준)
     */
    @Transactional(readOnly = true)
    public com.error.monitor.api.replay.dto.SessionReplayResponse getSessionReplay(String errorId) {
        // 에러의 가장 최근 occurrence에서 리플레이 조회
        ErrorOccurrence occurrence = occurrenceRepository.findFirstByErrorIdOrderByOccurredAtDesc(errorId)
            .orElseThrow(() -> new IllegalArgumentException("Error occurrence not found: " + errorId));

        if (occurrence.getSessionReplayId() == null) {
            throw new IllegalArgumentException("No session replay available for this error");
        }

        SessionReplay replay = replayRepository.findById(occurrence.getSessionReplayId())
            .orElseThrow(() -> new IllegalArgumentException("Session replay not found"));

        return com.error.monitor.api.replay.dto.SessionReplayResponse.builder()
                .errorId(errorId)
                .replayUrl(replay.getReplayDataUrl())
                .size(replay.getFileSizeBytes())
                .recordedAt(replay.getCreatedAt())
                .duration(replay.getDurationMs() / 1000) // convert ms to seconds
                .userInfo(com.error.monitor.api.replay.dto.SessionReplayResponse.UserInfo.builder()
                        .userId(occurrence.getUserId())
                        .ip(occurrence.getUserIp())
                        .userAgent(occurrence.getUserAgent())
                        .browser(occurrence.getBrowser())
                        .os(occurrence.getOs())
                        .build())
                .build();
    }

    /**
     * 세션 리플레이 다운로드 URL 생성 (Pre-signed URL)
     */
    public String generateDownloadUrl(String errorId, int expirationSeconds) {
        ErrorOccurrence occurrence = occurrenceRepository.findFirstByErrorIdOrderByOccurredAtDesc(errorId)
            .orElseThrow(() -> new IllegalArgumentException("Error occurrence not found: " + errorId));

        if (occurrence.getSessionReplayId() == null) {
            throw new IllegalArgumentException("No session replay available for this error");
        }

        SessionReplay replay = replayRepository.findById(occurrence.getSessionReplayId())
            .orElseThrow(() -> new IllegalArgumentException("Session replay not found"));

        // Extract S3 key from R2 URL (format: r2://bucket-name/key)
        String replayUrl = replay.getReplayDataUrl();
        String s3Key = replayUrl.replace("r2://" + bucketName + "/", "");

        try {
            // Generate pre-signed URL for download
            software.amazon.awssdk.services.s3.model.GetObjectRequest getRequest =
                    software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .build();

            software.amazon.awssdk.services.s3.presigner.S3Presigner presigner =
                    software.amazon.awssdk.services.s3.presigner.S3Presigner.create();

            software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presignRequest =
                    software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest.builder()
                            .signatureDuration(java.time.Duration.ofSeconds(expirationSeconds))
                            .getObjectRequest(getRequest)
                            .build();

            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            presigner.close();

            return presignedUrl;
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for key: {}", s3Key, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }
}
