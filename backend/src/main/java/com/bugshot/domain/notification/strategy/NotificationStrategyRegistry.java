package com.bugshot.domain.notification.strategy;

import com.bugshot.domain.notification.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알림 전략 레지스트리 (Strategy Pattern)
 * <p>
 * 모든 NotificationStrategy 구현체를 관리하고,
 * 채널 타입에 맞는 전략을 반환합니다.
 * </p>
 *
 * <pre>
 * Spring이 모든 NotificationStrategy 빈을 자동으로 주입하므로,
 * 새로운 알림 채널을 추가하려면:
 * 1. NotificationStrategy를 구현하는 새 클래스 생성
 * 2. @Component 어노테이션 추가
 * 3. getChannelType()에서 새 채널 타입 반환
 * → 자동으로 레지스트리에 등록됨!
 * </pre>
 */
@Component
@Slf4j
public class NotificationStrategyRegistry {

    private final Map<NotificationChannel.ChannelType, NotificationStrategy> strategies;

    /**
     * Spring이 모든 NotificationStrategy 구현체를 주입
     */
    public NotificationStrategyRegistry(List<NotificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        NotificationStrategy::getChannelType,
                        Function.identity()
                ));

        log.info("NotificationStrategyRegistry initialized with {} strategies: {}",
                strategies.size(),
                strategies.keySet());
    }

    /**
     * 채널 타입에 맞는 전략 반환
     *
     * @param channelType 채널 타입
     * @return 해당 전략 (없으면 Optional.empty())
     */
    public Optional<NotificationStrategy> getStrategy(NotificationChannel.ChannelType channelType) {
        return Optional.ofNullable(strategies.get(channelType));
    }

    /**
     * 채널 타입에 맞는 전략 반환 (없으면 예외)
     *
     * @param channelType 채널 타입
     * @return 해당 전략
     * @throws UnsupportedOperationException 지원하지 않는 채널 타입
     */
    public NotificationStrategy getStrategyOrThrow(NotificationChannel.ChannelType channelType) {
        return getStrategy(channelType)
                .orElseThrow(() -> new UnsupportedOperationException(
                        "Unsupported notification channel type: " + channelType));
    }

    /**
     * 지원하는 모든 채널 타입 반환
     */
    public java.util.Set<NotificationChannel.ChannelType> getSupportedChannelTypes() {
        return strategies.keySet();
    }

    /**
     * 특정 채널 타입 지원 여부 확인
     */
    public boolean supports(NotificationChannel.ChannelType channelType) {
        return strategies.containsKey(channelType);
    }
}
