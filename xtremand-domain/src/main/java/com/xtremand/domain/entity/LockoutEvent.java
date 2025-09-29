package com.xtremand.domain.entity;

import com.xtremand.domain.enums.LockoutEventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xt_lockout_events", indexes = {
        @Index(name = "idx_xt_lockout_events_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LockoutEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private LockoutEventType eventType;
}
