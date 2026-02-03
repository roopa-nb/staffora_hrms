package com.staffora.hrms;

import com.staffora.hrms.company.CompanyRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HrmsApplication {
    @Autowired
    private CompanyRepository companyRepository;

//    @PostConstruct
//    public void checkDb() {
//        System.out.println("🔥 COMPANIES FROM APP DB:");
//        companyRepository.findAll()
//                .forEach(c -> System.out.println(c.getId() + " -> " + c.getCompanyName()));
//    }


    public static void main(String[] args) {
        SpringApplication.run(HrmsApplication.class, args);


    }

}
