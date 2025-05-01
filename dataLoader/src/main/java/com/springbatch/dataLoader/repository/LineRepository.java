package com.springbatch.dataLoader.repository;

import com.springbatch.dataLoader.dto.LineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineRepository extends JpaRepository<LineEntity, Integer> {
}
