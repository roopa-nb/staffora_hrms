package com.staffora.hrms.dashboard;

import com.staffora.hrms.attendance.Attendance;
import com.staffora.hrms.attendance.AttendanceRepository;
import com.staffora.hrms.dashboard.dto.EmployeeDashboardResponse;
import com.staffora.hrms.dashboard.dto.HrDashboardResponse;
import com.staffora.hrms.employee.Employee;
import com.staffora.hrms.employee.EmployeeRepository;
import com.staffora.hrms.exception.NotFoundException;
import com.staffora.hrms.tenant.TenantContext;
import com.staffora.hrms.user.User;
import com.staffora.hrms.user.UserRepository;
import com.staffora.hrms.department.DepartmentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                            AttendanceRepository attendanceRepository,
                            DepartmentRepository departmentRepository,
                            UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    public HrDashboardResponse getHrDashboard() {
        Long companyId = requireCompanyId();
        LocalDate today = LocalDate.now();

        long totalEmployees = employeeRepository.countByCompanyId(companyId);
        long presentToday = attendanceRepository.countByCompanyIdAndDate(companyId, today);
        long absentToday = Math.max(0L, totalEmployees - presentToday);
        long totalDepartments = departmentRepository.countByCompanyId(companyId);

        return HrDashboardResponse.builder()
                .totalEmployees(totalEmployees)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .onLeave(0L)
                .totalDepartments(totalDepartments)
                .build();
    }

    public EmployeeDashboardResponse getEmployeeDashboard() {
        Long companyId = requireCompanyId();
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findByIdAndCompanyId(userId, companyId).orElse(null);
        if (user == null) {
            User existingUser = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
            if (!existingUser.getCompany().getId().equals(companyId)) {
                throw new AccessDeniedException("User does not belong to tenant company.");
            }
            user = existingUser;
        }

        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new NotFoundException("Employee profile not linked.");
        }

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .orElse(null);

        String todayStatus = attendance == null ? "NOT_MARKED" : resolveStatus(attendance);
        long workHoursToday = computeWorkMinutes(attendance);

        return EmployeeDashboardResponse.builder()
                .todayAttendanceStatus(todayStatus)
                .workHoursToday(workHoursToday)
                .leaveBalance(12)
                .upcomingHolidays(List.of("Sunday", "Independence Day"))
                .recentActivities(List.of("Checked profile", "Viewed attendance"))
                .build();
    }

    private String resolveStatus(Attendance attendance) {
        if (attendance.getCheckOutTime() != null) {
            return "CHECKED_OUT";
        }
        if (attendance.getBreakEndTime() != null) {
            return "WORKING";
        }
        if (attendance.getBreakStartTime() != null) {
            return "ON_BREAK";
        }
        if (attendance.getCheckInTime() != null) {
            return "CHECKED_IN";
        }
        return "NOT_MARKED";
    }

    private long computeWorkMinutes(Attendance attendance) {
        if (attendance == null || attendance.getCheckInTime() == null) {
            return 0L;
        }

        if (attendance.getWorkHours() != null) {
            return attendance.getWorkHours();
        }

        LocalDateTime endTime = attendance.getCheckOutTime() != null
                ? attendance.getCheckOutTime()
                : LocalDateTime.now();

        long breakMinutes = attendance.getBreakMinutes() != null ? attendance.getBreakMinutes() : 0L;
        return Math.max(0L, Duration.between(attendance.getCheckInTime(), endTime).toMinutes() - breakMinutes);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        System.out.println("DASHBOARD TENANT = " + companyId);
        if (companyId == null) {
            throw new IllegalStateException("Tenant context is missing.");
        }
        return companyId;
    }
}
