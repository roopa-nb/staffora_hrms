package com.staffora.hrms.department;

import com.staffora.hrms.company.Company;
import com.staffora.hrms.company.CompanyRepository;
import com.staffora.hrms.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             CompanyRepository companyRepository) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
    }

    public Department createDepartment(String name, String description) {

        Long companyId = TenantContext.getCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Department d = new Department();
        d.setName(name);
        d.setDescription(description);
        d.setCompany(company);

        return departmentRepository.save(d);
    }

    public List<Department> getMyDepartments() {

        Long companyId = TenantContext.getCompanyId();

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return departmentRepository.findByCompany(company);
    }
}
