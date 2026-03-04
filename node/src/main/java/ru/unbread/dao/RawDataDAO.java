package ru.unbread.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.unbread.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
