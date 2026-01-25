package com.apetitto.apetittoerpbackend.erp.hr.service.strategy;

import com.apetitto.apetittoerpbackend.erp.hr.model.AttendanceRecord;
import com.apetitto.apetittoerpbackend.erp.hr.model.Employee;
import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.AttendanceStatus;
import com.apetitto.apetittoerpbackend.erp.hr.model.enums.SalaryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DailyShiftStrategy implements SalaryCalculationStrategy {

    @Override
    public SalaryType getSupportedType() {
        return SalaryType.DAILY_SHIFT;
    }

    @Override
    public void calculate(PayrollAccrual accrual, Employee employee, List<AttendanceRecord> records) {
        BigDecimal shiftRate = employee.getSalaryBase();

        accrual.setCalculatedDayRate(shiftRate);
        accrual.setCalculatedHourRate(BigDecimal.ZERO);

        int daysWorked = 0;
        for (AttendanceRecord rec : records) {
            if (rec.getStatus() == AttendanceStatus.PRESENT) {
                daysWorked++;
            }
        }

        BigDecimal grossAmount = shiftRate.multiply(BigDecimal.valueOf(daysWorked));

        accrual.setDaysWorked(daysWorked);
        accrual.setGrossSalaryAmount(grossAmount);
        accrual.setPenaltyAmount(BigDecimal.ZERO);
        accrual.setOvertimeBonusAmount(BigDecimal.ZERO);
        accrual.setFinalAmount(grossAmount);
    }
}