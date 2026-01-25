package com.apetitto.apetittoerpbackend.erp.hr.service.strategy;

import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class HourlySalaryStrategy implements SalaryCalculationStrategy {

    @Override
    public SalaryType getSupportedType() {
        return SalaryType.HOURLY;
    }

    @Override
    public void calculate(PayrollAccrual accrual, Employee employee, List<AttendanceRecord> records) {
        BigDecimal hourRate = employee.getSalaryBase();
        accrual.setCalculatedDayRate(BigDecimal.ZERO);
        accrual.setCalculatedHourRate(hourRate);

        int totalMinutesWorked = 0;
        int daysWorked = 0;

        for (AttendanceRecord rec : records) {
            if (rec.getStatus() == AttendanceStatus.PRESENT) {
                daysWorked++;
                totalMinutesWorked += rec.getDurationMinutes();
            }
        }

        accrual.setDaysWorked(daysWorked);

        BigDecimal totalHours = BigDecimal.valueOf(totalMinutesWorked).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        accrual.setTotalWorkedHours(totalHours);

        BigDecimal gross = totalHours.multiply(hourRate);
        accrual.setGrossSalaryAmount(gross);
        accrual.setFinalAmount(gross);
    }
}