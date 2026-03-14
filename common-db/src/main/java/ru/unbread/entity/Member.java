package ru.unbread.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"telegram_user_id", "group_id"})
)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "telegram_user_id")
    private Long telegramUserId;
    @CreationTimestamp
    private LocalDateTime dateAdded;
    private String firstName;
    private String lastName;
    private String username;
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
    private Boolean isActive;
}
