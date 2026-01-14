package com.apetitto.apetittoerpbackend.erp.hr.repository;

import com.apetitto.apetittoerpbackend.erp.hr.model.HrDeviceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HrDeviceLogRepository extends JpaRepository<HrDeviceLog, Long> {
    List<HrDeviceLog> findByIsProcessedFalse();
}