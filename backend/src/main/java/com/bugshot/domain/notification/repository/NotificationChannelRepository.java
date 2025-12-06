package com.bugshot.domain.notification.repository;

import com.bugshot.domain.notification.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, String> {

    List<NotificationChannel> findByProjectId(String projectId);

    List<NotificationChannel> findByProjectIdAndEnabled(String projectId, Boolean enabled);

    List<NotificationChannel> findByProjectIdAndChannelType(String projectId,
                                                              NotificationChannel.ChannelType channelType);
}
