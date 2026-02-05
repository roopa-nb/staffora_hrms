package com.staffora.hrms.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrDashboardResponse {

    private long totalEmployees;
    private long presentToday;
    private long absentToday;
    private long onLeave;
    private long totalDepartments;
}
