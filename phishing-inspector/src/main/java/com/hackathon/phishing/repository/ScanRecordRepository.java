package com.hackathon.phishing.repository;

import com.hackathon.phishing.model.ScanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanRecordRepository extends JpaRepository<ScanRecord, Long> {
}
