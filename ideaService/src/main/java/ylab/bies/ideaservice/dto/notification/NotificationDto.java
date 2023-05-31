package ylab.bies.ideaservice.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class NotificationDto {

    private LocalDateTime localDateTime;
    private String action;
    private UUID actionUserId;
    private Long ideaId;
    private UUID ideaCreatorUserId;


}
