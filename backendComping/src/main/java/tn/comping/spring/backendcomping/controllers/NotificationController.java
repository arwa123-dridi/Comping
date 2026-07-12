package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.NotificationResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.NotificationService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_URL_NOTIFICATION)
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(Constants.GET_MES_NOTIFICATIONS)
    public ResponseEntity<List<NotificationResponse>> getMesNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getForCurrentUser(authentication.getName()));
    }

    @GetMapping(Constants.GET_UNREAD_COUNT_NOTIFICATIONS)
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCountForCurrentUser(authentication.getName())));
    }

    @PatchMapping(Constants.MARK_READ_NOTIFICATION)
    public ResponseEntity<Void> markRead(@PathVariable String id, Authentication authentication) {
        notificationService.markRead(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(Constants.MARK_ALL_READ_NOTIFICATIONS)
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationService.markAllReadForCurrentUser(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
