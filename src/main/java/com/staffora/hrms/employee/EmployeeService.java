package com.staffora.hrms.employee;

import com.staffora.hrms.company.Company;
import com.staffora.hrms.company.CompanyRepository;
import com.staffora.hrms.department.Department;
import com.staffora.hrms.department.DepartmentRepository;
import com.staffora.hrms.employee.dto.EmployeeRequest;
import com.staffora.hrms.employee.dto.EmployeeResponse;
import com.staffora.hrms.employee.dto.EmployeeUpdateRequest;
import com.staffora.hrms.exception.NotFoundException;
import com.staffora.hrms.tenant.TenantContext;
import com.staffora.hrms.user.Role;
import com.staffora.hrms.user.User;
import com.staffora.hrms.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           CompanyRepository companyRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Long companyId = requireCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found."));

        Department department = resolveDepartment(request.getDepartmentId(), companyId);

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .joinDate(request.getJoinDate())
                .status(request.getStatus())
                .company(company)
                .department(department)
                .build();

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        Long companyId = requireCompanyId();

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new IllegalStateException("Employee not found."));

        if (request.getEmployeeCode() != null) {
            employee.setEmployeeCode(request.getEmployeeCode());
        }

        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }

        if (request.getEmail() != null) {
            updateEmployeeEmail(employee, request.getEmail(), companyId);
        }

        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }

        if (request.getJoinDate() != null) {
            employee.setJoinDate(request.getJoinDate());
        }

        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        if (request.getDepartmentId() != null) {
            Department department = resolveDepartment(request.getDepartmentId(), companyId);
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    public EmployeeResponse getEmployeeProfile(Long employeeId, Authentication authentication) {
        Long companyId = requireCompanyId();

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new IllegalStateException("Employee not found."));

        if (hasAnyRole(authentication, Role.HR, Role.COMPANY_ADMIN, Role.SUPER_ADMIN)) {
            return toResponse(employee);
        }

        if (hasAnyRole(authentication, Role.EMPLOYEE)) {
            Long requesterId = (Long) authentication.getPrincipal();
            User requester = userRepository.findById(requesterId)
                    .orElseThrow(() -> new IllegalStateException("User not found."));
            if (!employee.getEmail().equalsIgnoreCase(requester.getEmail())) {
                throw new IllegalStateException("Employees can only view their own profile.");
            }
            return toResponse(employee);
        }

        throw new IllegalStateException("Access denied.");
    }

    public EmployeeResponse getMyProfile(Authentication authentication) {
        Long companyId = requireCompanyId();
        if (!hasAnyRole(authentication, Role.EMPLOYEE)) {
            throw new AccessDeniedException("Access denied.");
        }
        User user = userRepository.findByIdAndCompanyId((Long) authentication.getPrincipal(), companyId)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new NotFoundException("Employee profile not found.");
        }
        return toResponse(employee);
    }

    public EmployeeResponse updateMyProfile(EmployeeUpdateRequest request, Authentication authentication) {
        Long companyId = requireCompanyId();
        User user = userRepository.findByIdAndCompanyId((Long) authentication.getPrincipal(), companyId)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new IllegalStateException("Employee profile not found.");
        }

        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName());
        }

        if (request.getEmail() != null) {
            updateEmployeeEmail(employee, request.getEmail(), companyId);
        }

        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    public List<Employee> getMyEmployees() {
        Long companyId = requireCompanyId();
        return employeeRepository.findAllByCompanyId(companyId);
    }

    public void deleteEmployee(Long employeeId, Authentication authentication) {
        Long companyId = requireCompanyId();
        if (hasAnyRole(authentication, Role.EMPLOYEE)) {
            throw new IllegalStateException("Employees cannot delete employees.");
        }

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new IllegalStateException("Employee not found."));
        employeeRepository.delete(employee);
    }

    public String enableLogin(Long employeeId) {
        Long companyId = requireCompanyId();

        Employee employee = employeeRepository.findByIdAndCompanyId(employeeId, companyId)
                .orElseThrow(() -> new IllegalStateException("Employee not found."));

        if (employee.getUser() != null) {
            throw new IllegalStateException("Employee login already enabled.");
        }

        if (userRepository.findByEmailAndCompanyId(employee.getEmail(), companyId).isPresent()) {
            throw new IllegalStateException("Email is already in use.");
        }

        String temporaryPassword = generateTemporaryPassword();

        User user = User.builder()
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .password(passwordEncoder.encode(temporaryPassword))
                .role(Role.EMPLOYEE)
                .company(employee.getCompany())
                .employee(employee)
                .build();

        userRepository.save(user);
        employee.setUser(user);
        employeeRepository.save(employee);

        return temporaryPassword;
    }

    private Department resolveDepartment(Long departmentId, Long companyId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findByIdAndCompanyId(departmentId, companyId)
                .orElseThrow(() -> new IllegalStateException("Department not found."));
    }

    private boolean hasAnyRole(Authentication authentication, Role... roles) {
        for (Role role : roles) {
            String expected = "ROLE_" + role.name();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (expected.equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .joinDate(employee.getJoinDate())
                .status(employee.getStatus())
                .companyId(employee.getCompany().getId())
                .departmentId(employee.getDepartment() == null ? null : employee.getDepartment().getId())
                .departmentName(employee.getDepartment() == null ? null : employee.getDepartment().getName())
                .build();
    }

    private void updateEmployeeEmail(Employee employee, String email, Long companyId) {
        User existing = userRepository.findByEmailAndCompanyId(email, companyId).orElse(null);
        if (existing != null && (employee.getUser() == null || !existing.getId().equals(employee.getUser().getId()))) {
            throw new IllegalStateException("Email is already in use.");
        }
        employee.setEmail(email);
        if (employee.getUser() != null) {
            employee.getUser().setEmail(email);
            userRepository.save(employee.getUser());
        }
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private Long requireCompanyId() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Tenant context is missing.");
        }
        return companyId;
    }
}
