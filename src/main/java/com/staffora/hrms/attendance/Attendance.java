package com.staffora.hrms.attendance;

import com.staffora.hrms.company.Company;
import com.staffora.hrms.employee.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private LocalDate date;

    private LocalDateTime checkInTime;
    private LocalDateTime breakStartTime;
    private LocalDateTime breakEndTime;
    private LocalDateTime checkOutTime;

    private String photoCheckInUrl;
    private String photoBreakStartUrl;
    private String photoBreakEndUrl;
    private String photoCheckOutUrl;

    private String ipCheckIn;
    private String ipBreakStart;
    private String ipBreakEnd;
    private String ipCheckOut;

    private Long workHours;
    private Long breakMinutes;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
