package ru.unbread.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.unbread.entity.Group;
import ru.unbread.entity.Zone;

import java.util.List;
import java.util.Optional;

public interface ZoneDAO extends JpaRepository<Zone, Long> {
    Optional<Zone> findByNameAndGroup(String name, Optional<Group> group);
    List<Zone> findByGroup(Group group);
}