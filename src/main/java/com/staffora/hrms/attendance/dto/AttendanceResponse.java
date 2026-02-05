package com.staffora.hrms.attendance.dto;

import com.staffora.hrms.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long employeeId;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private LocalDateTime breakStartTime;
    private LocalDateTime breakEndTime;
    private LocalDateTime checkOutTime;
    private Long workHours;
    private Long breakMinutes;
    private AttendanceStatus status;
}
