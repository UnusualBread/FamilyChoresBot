package ru.unbread.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.unbread.entity.Group;
import ru.unbread.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberDAO extends JpaRepository<Member, Long> {
    List<Member> findByGroupId(Long groupId);

    Optional<Member> findByTelegramUserIdAndGroupId(Long telegramUserId, Long groupId);

    List<Member> findByGroupIdAndIsActiveTrue(Long groupId);

    List<Member> findByTelegramUserId(Long telegramUserId);

    List<Member> findByGroup(Group group);
}
