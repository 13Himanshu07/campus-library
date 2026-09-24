package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="notification_preferences")
@Getter @Setter @NoArgsConstructor
public class NotificationPreference {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="user_id", nullable=false, unique=true) private User user;
    private boolean emailNotifications=true;
    private boolean dueDateNotifications=true;
    private boolean newBookNotifications=true;
    private boolean overdueNotifications=true;
}
