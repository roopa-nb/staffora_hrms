package com.staffora.hrms.attendance;

import com.staffora.hrms.attendance.dto.AttendanceResponse;
import com.staffora.hrms.company.Company;
import com.staffora.hrms.company.CompanyRepository;
import com.staffora.hrms.employee.Employee;
import com.staffora.hrms.exception.BadRequestException;
import com.staffora.hrms.exception.NotFoundException;
import com.staffora.hrms.tenant.TenantContext;
import com.staffora.hrms.user.User;
import com.staffora.hrms.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    private static final List<String> ALLOWED_IPS = List.of("127.0.0.1");
    private static final String UPLOAD_DIR = "uploads/attendance";

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             UserRepository userRepository,
                             CompanyRepository companyRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    public AttendanceResponse markAttendance(MultipartFile photo, HttpServletRequest request) {
        Long companyId = requireCompanyId();
        User user = getAuthenticatedUser(companyId);
        Employee employee = requireEmployee(user);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found."));

        String clientIp = request.getRemoteAddr();
        String allowedIp = company.getOfficeIp();
        System.out.println("Attendance IP check requestIp=" + clientIp + ", allowedIp=" + allowedIp);
        if (allowedIp != null && !allowedIp.equals(clientIp)) {

        String clientIp = request.getRemoteAddr();
        if (!ALLOWED_IPS.contains(clientIp)) {
            throw new RuntimeException("Attendance allowed only from office network");
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), today)
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .company(user.getCompany())
                        .date(today)
                        .status(AttendanceStatus.PRESENT)
                        .build());

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("Attendance already checked out.");
        }

        LocalDateTime now = LocalDateTime.now();
        String photoUrl = storePhoto(employee.getId(), photo, now);

        if (attendance.getCheckInTime() == null) {
            attendance.setCheckInTime(now);
            attendance.setPhotoCheckInUrl(photoUrl);
            attendance.setIpCheckIn(clientIp);
        } else if (attendance.getBreakStartTime() == null) {
            attendance.setBreakStartTime(now);
            attendance.setPhotoBreakStartUrl(photoUrl);
            attendance.setIpBreakStart(clientIp);
        } else if (attendance.getBreakEndTime() == null) {
            attendance.setBreakEndTime(now);
            attendance.setPhotoBreakEndUrl(photoUrl);
            attendance.setIpBreakEnd(clientIp);
        } else if (attendance.getCheckOutTime() == null) {
            attendance.setCheckOutTime(now);
            attendance.setPhotoCheckOutUrl(photoUrl);
            attendance.setIpCheckOut(clientIp);
            calculateTotals(attendance);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return toResponse(saved);
    }

    public AttendanceResponse getMyAttendance() {
        Long companyId = requireCompanyId();
        User user = getAuthenticatedUser(companyId);
        Employee employee = requireEmployee(user);

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .orElseThrow(() -> new NotFoundException("Attendance not found."));

        return toResponse(attendance);
    }

    public List<AttendanceResponse> getCompanyAttendance() {
        Long companyId = requireCompanyId();
        return attendanceRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void calculateTotals(Attendance attendance) {
        if (attendance.getCheckInTime() == null || attendance.getCheckOutTime() == null) {
            return;
        }
        long breakMinutes = 0L;
        if (attendance.getBreakStartTime() != null && attendance.getBreakEndTime() != null) {
            breakMinutes = Duration.between(attendance.getBreakStartTime(), attendance.getBreakEndTime()).toMinutes();
        }
        long totalMinutes = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
        attendance.setBreakMinutes(breakMinutes);
        attendance.setWorkHours(Math.max(0L, totalMinutes - breakMinutes));
    }

    private String storePhoto(Long employeeId, MultipartFile photo, LocalDateTime timestamp) {
        if (photo == null || photo.isEmpty()) {
            throw new BadRequestException("Photo is required.");
        }
        String filename = employeeId + "_" + timestamp.toInstant(java.time.ZoneOffset.UTC).toEpochMilli() + ".jpg";
        Path uploadPath = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(filename);
            photo.transferTo(targetPath);
            return "/uploads/attendance/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store attendance photo.");
        }
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .employeeId(attendance.getEmployee().getId())
                .date(attendance.getDate())
                .checkInTime(attendance.getCheckInTime())
                .breakStartTime(attendance.getBreakStartTime())
                .breakEndTime(attendance.getBreakEndTime())
                .checkOutTime(attendance.getCheckOutTime())
                .workHours(attendance.getWorkHours())
                .breakMinutes(attendance.getBreakMinutes())
                .status(attendance.getStatus())
                .build();
    }

    private User getAuthenticatedUser(Long companyId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private Employee requireEmployee(User user) {
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new BadRequestException("Employee profile not linked.");
        }
        return employee;
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Tenant context is missing.");
        }
        return companyId;
    }
}
