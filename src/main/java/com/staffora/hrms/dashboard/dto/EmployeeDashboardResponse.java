package com.staffora.hrms.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardResponse {

    private String todayAttendanceStatus;
    private long workHoursToday;
    private int leaveBalance;
    private List<String> upcomingHolidays;
    private List<String> recentActivities;
}
