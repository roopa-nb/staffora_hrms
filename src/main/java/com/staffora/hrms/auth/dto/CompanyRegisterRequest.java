package com.staffora.hrms.auth.dto;

import lombok.Data;

@Data
public class CompanyRegisterRequest {
    private String companyName;
    private String domain;
    private String email;
    private String password;
}
