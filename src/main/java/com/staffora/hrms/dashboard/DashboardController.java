package com.staffora.hrms.dashboard;

import com.staffora.hrms.dashboard.dto.EmployeeDashboardResponse;
import com.staffora.hrms.dashboard.dto.HrDashboardResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR')")
    public HrDashboardResponse getHrDashboard() {
        return dashboardService.getHrDashboard();
    }

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public EmployeeDashboardResponse getEmployeeDashboard() {
        return dashboardService.getEmployeeDashboard();
    }
}
