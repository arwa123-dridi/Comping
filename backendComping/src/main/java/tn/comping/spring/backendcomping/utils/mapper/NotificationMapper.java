package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.NotificationResponse;
import tn.comping.spring.backendcomping.entities.Notification;

public class NotificationMapper {

    public static NotificationResponse toDto(Notification entity) {
        if (entity == null) return null;
        return NotificationResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .titre(entity.getTitre())
                .message(entity.getMessage())
                .lu(entity.isLu())
                .dateCreation(entity.getDateCreation())
                .refType(entity.getRefType())
                .refId(entity.getRefId())
                .lien(entity.getLien())
                .build();
    }
}
