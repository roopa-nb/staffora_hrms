package com.staffora.hrms.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.staffora.hrms.company.Company;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

    List<Employee> findByCompany(Company company);
}
