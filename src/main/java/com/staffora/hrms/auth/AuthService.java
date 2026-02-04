package com.staffora.hrms.auth;

import com.staffora.hrms.auth.dto.CompanyRegisterRequest;
import com.staffora.hrms.auth.dto.LoginRequest;
import com.staffora.hrms.company.Company;
import com.staffora.hrms.company.CompanyRepository;
import com.staffora.hrms.exception.BadRequestException;
import com.staffora.hrms.security.JwtUtil;
import com.staffora.hrms.tenant.TenantContext;
import com.staffora.hrms.user.Role;
import com.staffora.hrms.user.User;
import com.staffora.hrms.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(CompanyRepository companyRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Register company (tenant) + first HR
    public String registerCompany(CompanyRegisterRequest request) {

        // 🔒 Prevent duplicate tenant
//        if (companyRepository.findByCompanyName(request.getCompanyName())
//                .isPresent()) {
//            throw new BadRequestException("Company with this domain already exists");
//        }

        // 1️⃣ Create company (tenant)
        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .plan("FREE")
                .build();

        companyRepository.save(company);


        companyRepository.save(company);

        // 2️⃣ Create first HR user
        User user = User.builder()
                .fullName("HR Admin")
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.HR)
                .company(company)
                .build();

        userRepository.save(user);

        // 3️⃣ Generate JWT
        return jwtUtil.generateToken(
                user.getId(),
                company.getId(),
                user.getRole().name()
        );
    }
    public void someMethod() {
        System.out.println("TENANT: " + TenantContext.getCompanyId());
        // your logic...
    }

    // ✅ Login
    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }


        return jwtUtil.generateToken(
                user.getId(),
                user.getCompany().getId(),
                user.getRole().name()
        );
    }

    public void changePassword(String currentPassword, String newPassword) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}
