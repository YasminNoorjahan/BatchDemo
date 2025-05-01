package com.springbatch.dataLoader.repository;

import com.springbatch.dataLoader.dto.ProcessedFile;
import jakarta.annotation.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedFileRepository extends JpaRepository<ProcessedFile,Integer> {
}
