package com.staffora.hrms.attendance;

import com.staffora.hrms.attendance.dto.AttendanceResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping(value = "/mark", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public AttendanceResponse markAttendance(@RequestParam("photo") MultipartFile photo,
                                             HttpServletRequest request) {
        return attendanceService.markAttendance(photo, request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public AttendanceResponse getMyAttendance() {
        return attendanceService.getMyAttendance();
    }

    @GetMapping("/company")
    @PreAuthorize("hasRole('HR')")
    public List<AttendanceResponse> getCompanyAttendance() {
        return attendanceService.getCompanyAttendance();
    }
}
