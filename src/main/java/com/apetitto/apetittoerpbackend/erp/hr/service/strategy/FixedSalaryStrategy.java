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
public class FixedSalaryStrategy implements SalaryCalculationStrategy {

    private static final int STANDARD_MONTH_DAYS = 30;

    @Override
    public SalaryType getSupportedType() {
        return SalaryType.FIXED;
    }

    @Override
    public void calculate(PayrollAccrual accrual, Employee emp, List<AttendanceRecord> records) {
        BigDecimal baseSalary = emp.getSalaryBase();
        BigDecimal workHoursPerDay = emp.getWorkHoursPerDay();
        int daysOff = emp.getDaysOffPerMonth() != null ? emp.getDaysOffPerMonth() : 0;

        BigDecimal normDays = BigDecimal.valueOf(STANDARD_MONTH_DAYS - daysOff);
        BigDecimal dayRate = baseSalary.divide(normDays, 2, RoundingMode.HALF_UP);
        BigDecimal hourRate = dayRate.divide(workHoursPerDay, 2, RoundingMode.HALF_UP);

        accrual.setCalculatedDayRate(dayRate);
        accrual.setCalculatedHourRate(hourRate);

        int daysWorked = 0;
        int lateMins = 0;
        int earlyLeaveMins = 0;
        int overtimeMins = 0;
        int earlyComeMins = 0;
        int lateOutMins = 0;

        for (AttendanceRecord rec : records) {
            if (rec.getStatus() == AttendanceStatus.PRESENT) {
                daysWorked++;
                lateMins += rec.getLateMinutes();
                earlyLeaveMins += rec.getEarlyLeaveMinutes();
                overtimeMins += rec.getOvertimeMinutes();
                earlyComeMins += rec.getEarlyComeMinutes();
                lateOutMins += rec.getLateOutMinutes();
            }
        }

        accrual.setDaysWorked(daysWorked);
        accrual.setLateMinutes(lateMins);
        accrual.setEarlyLeaveMinutes(earlyLeaveMins);
        accrual.setTotalUndertimeMinutes(lateMins + earlyLeaveMins);

        accrual.setEarlyComeMinutes(earlyComeMins);
        accrual.setLateOutMinutes(lateOutMins);
        accrual.setTotalOvertimeMinutes(overtimeMins);

        int effectiveOvertimeMins = Math.max(0, overtimeMins - earlyComeMins);
        accrual.setTotalOvertimeEffectiveMinutes(effectiveOvertimeMins);


        BigDecimal grossAmount = dayRate.multiply(BigDecimal.valueOf(daysWorked));
        accrual.setGrossSalaryAmount(grossAmount);

        BigDecimal undertimeHours = BigDecimal.valueOf(accrual.getTotalUndertimeMinutes()).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal penaltyAmount = undertimeHours.multiply(hourRate);
        accrual.setPenaltyAmount(penaltyAmount);

        BigDecimal overtimeHours = BigDecimal.valueOf(effectiveOvertimeMins).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal bonusAmount = overtimeHours.multiply(hourRate);
        accrual.setOvertimeBonusAmount(bonusAmount);

        accrual.setFinalAmount(grossAmount.subtract(penaltyAmount).add(bonusAmount));
    }
}