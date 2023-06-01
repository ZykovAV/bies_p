package ylab.bies.ideaservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ylab.bies.ideaservice.dto.notification.NotificationDto;


@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;
    private final String topicName;

    public KafkaProducerService(KafkaTemplate<String, NotificationDto> kafkaTemplate,
                                @Value("${kafka.topic-name}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void sendNotification(NotificationDto notificationDto) {
        try {
            kafkaTemplate.send(topicName, notificationDto);
            log.info("Notification sent successfully: {}", notificationDto);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
            throw new KafkaException("Failed to send notification", e);
        }
    }
}
