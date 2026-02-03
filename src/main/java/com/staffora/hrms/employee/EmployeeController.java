package com.staffora.hrms.employee;

import com.staffora.hrms.employee.dto.EmployeeRequest;
import com.staffora.hrms.employee.dto.EmployeeResponse;
import com.staffora.hrms.employee.dto.EmployeeUpdateRequest;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR','COMPANY_ADMIN','SUPER_ADMIN')")
    public EmployeeResponse createEmployee(@RequestBody EmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR','COMPANY_ADMIN','SUPER_ADMIN')")
    public List<Employee> getEmployees() {
        return employeeService.getMyEmployees();
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('HR','COMPANY_ADMIN','SUPER_ADMIN')")
    public EmployeeResponse updateEmployee(@PathVariable Long employeeId,
                                           @RequestBody EmployeeUpdateRequest request) {
        return employeeService.updateEmployee(employeeId, request);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public void deleteEmployee(@PathVariable Long employeeId,
                               Authentication authentication) {
        employeeService.deleteEmployee(employeeId, authentication);
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('HR','COMPANY_ADMIN','SUPER_ADMIN','EMPLOYEE')")
    public EmployeeResponse getEmployee(@PathVariable Long employeeId,
                                        Authentication authentication) {
        return employeeService.getEmployeeProfile(employeeId, authentication);
    }
}
