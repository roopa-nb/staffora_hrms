package com.staffora.hrms.company;

import com.staffora.hrms.company.dto.OfficeIpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PutMapping("/office-ip")
    @PreAuthorize("hasRole('HR')")
    public Map<String, String> updateOfficeIp(@RequestBody OfficeIpRequest request) {
        String message = companyService.updateOfficeIp(request.getOfficeIp());
        return Map.of("message", message);
    }
}
