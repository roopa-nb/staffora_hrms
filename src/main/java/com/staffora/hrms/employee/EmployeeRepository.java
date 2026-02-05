package com.staffora.hrms.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import com.staffora.hrms.company.Company;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByIdAndCompanyId(Long id, Long companyId);

    List<Employee> findByCompany(Company company);

    List<Employee> findAllByCompanyId(Long companyId);

    long countByCompanyId(Long companyId);
}
