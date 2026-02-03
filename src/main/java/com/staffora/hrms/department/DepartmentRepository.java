package com.staffora.hrms.department;

import com.staffora.hrms.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompany(Company company);

    Optional<Department> findByIdAndCompanyId(Long id, Long companyId);
}
