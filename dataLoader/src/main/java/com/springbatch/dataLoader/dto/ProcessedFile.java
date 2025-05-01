package com.springbatch.dataLoader.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity

@Getter
@Setter
@AllArgsConstructor@NoArgsConstructor
public class ProcessedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String fileName;

    private  String processingStatus;

    public ProcessedFile(String fileName, String processingStatus) {
        this.fileName = fileName;
        this.processingStatus = processingStatus;
    }
}
