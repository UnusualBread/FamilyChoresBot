package ru.unbread.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.unbread.entity.Group;

import java.util.List;
import java.util.Optional;

public interface GroupDAO extends JpaRepository<Group, Long> {
    Optional<Group> findByChatId(Long chatId);

    List<Group> findByIsActiveTrue();
}