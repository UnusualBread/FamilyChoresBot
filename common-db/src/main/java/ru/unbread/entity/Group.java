package ru.unbread.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long chatId;
    @CreationTimestamp
    private LocalDateTime creationDate;
    private String name;
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Member> members;
    private Boolean isActive;
}
