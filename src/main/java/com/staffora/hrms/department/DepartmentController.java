package com.staffora.hrms.department;

import com.staffora.hrms.department.dto.DepartmentRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public Department create(@RequestBody DepartmentRequest request) {
        return departmentService.createDepartment(
                request.getName(),
                request.getDescription()
        );
    }

    @GetMapping
    public List<Department> getAll() {
        return departmentService.getMyDepartments();
    }
}
