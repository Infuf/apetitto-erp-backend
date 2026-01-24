package com.apetitto.apetittoerpbackend.erp.hr.service.strategy;

import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import java.util.List;

public interface SalaryCalculationStrategy {
    void calculate(PayrollAccrual accrual, Employee employee, List<AttendanceRecord> records);
    SalaryType getSupportedType();
}