package ru.unbread.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.unbread.entity.AppUser;

import java.util.Optional;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByTelegramUserId(Long id);
}