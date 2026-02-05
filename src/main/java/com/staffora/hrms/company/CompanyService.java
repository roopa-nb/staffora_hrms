package com.staffora.hrms.company;

import com.staffora.hrms.exception.NotFoundException;
import com.staffora.hrms.tenant.TenantContext;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public String updateOfficeIp(String officeIp) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new IllegalStateException("Tenant context is missing.");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found."));

        company.setOfficeIp(officeIp);
        companyRepository.save(company);
        return "Office IP updated successfully";
    }
}
