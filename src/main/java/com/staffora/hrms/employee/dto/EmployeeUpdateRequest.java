package com.staffora.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

    private String employeeCode;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate joinDate;
    private String status;
    private Long departmentId;
}
