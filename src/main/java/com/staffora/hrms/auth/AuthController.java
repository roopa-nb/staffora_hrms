package com.staffora.hrms.auth;

import com.staffora.hrms.auth.dto.AuthResponse;
import com.staffora.hrms.auth.dto.ChangePasswordRequest;
import com.staffora.hrms.auth.dto.CompanyRegisterRequest;
import com.staffora.hrms.auth.dto.LoginRequest;
import com.staffora.hrms.security.JwtUtil;
import com.staffora.hrms.user.User;
import com.staffora.hrms.user.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          JwtUtil jwtUtil,
                          UserRepository userRepository) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // ✅ Company signup (creates tenant + first HR)
    @PostMapping("/register-company")
    public AuthResponse register(@RequestBody CompanyRegisterRequest request) {
        String token = authService.registerCompany(request);
        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow();
        return new AuthResponse(token, "HR", request.getCompanyName(), user.isFirstLogin());
    }

    // ✅ Login
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(request);

        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow();

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getCompany().getCompanyName(),
                user.isFirstLogin()
        );
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getCurrentPassword(), request.getNewPassword());
    }
}
