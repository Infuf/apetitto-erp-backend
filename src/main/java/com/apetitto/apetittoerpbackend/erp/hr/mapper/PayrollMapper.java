package com.apetitto.apetittoerpbackend.erp.hr.mapper;

import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollAccrualDto;
import com.apetitto.apetittoerpbackend.erp.hr.model.PayrollAccrual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface PayrollMapper {

    BigDecimal MINUTES_IN_HOUR = BigDecimal.valueOf(60);

    @Mappings({
            @Mapping(target = "employeeId", source = "employee.id"),
            @Mapping(target = "employeeName", expression = "java(getEmployeeName(entity))"),
            @Mapping(target = "departmentName", expression = "java(getDepartmentName(entity))"),

            @Mapping(target = "status", expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)"),

            @Mapping(target = "baseAmount", source = "grossSalaryAmount"),

            @Mapping(target = "bonusAmount", expression = "java(calcBonus(entity))"),

            @Mapping(target = "totalUndertimeHours",
                    expression = "java(minutesToHours(entity.getTotalUndertimeMinutes()))"),

            @Mapping(target = "totalOvertimeHours",
                    expression = "java(minutesToHours(entity.getTotalOvertimeEffectiveMinutes()))")
    })
    PayrollAccrualDto toDto(PayrollAccrual entity);


    default String getEmployeeName(PayrollAccrual entity) {
        return entity.getEmployee() != null
                ? entity.getEmployee().getUser().getFirstName()
                : null;
    }

    default String getDepartmentName(PayrollAccrual entity) {
        return entity.getEmployee() != null
                && entity.getEmployee().getDepartment() != null
                ? entity.getEmployee().getDepartment().getName()
                : null;
    }

    default BigDecimal calcBonus(PayrollAccrual entity) {
        return zero(entity.getOvertimeBonusAmount())
                .add(zero(entity.getManualBonusAmount()));
    }

    default BigDecimal minutesToHours(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(minutes)
                .divide(MINUTES_IN_HOUR, 2, RoundingMode.HALF_UP);
    }

    default BigDecimal zero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
