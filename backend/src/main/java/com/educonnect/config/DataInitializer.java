package com.educonnect.config;

import com.educonnect.entity.User;
import com.educonnect.enums.Role;
import com.educonnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default administrator and student evaluation accounts upon startup if database is empty.
 * Configured securely via environment variables (ADMIN_EMAIL, ADMIN_PASSWORD).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.default-accounts:true}")
    private boolean initDefaultAccounts;

    @Value("${app.init.admin.email:admin@college.edu}")
    private String adminEmail;

    @Value("${app.init.admin.password:AdminPassword123!}")
    private String adminPassword;

    @Value("${app.init.admin.name:Prof. Lakshmi Rao (CSP Coordinator)}")
    private String adminName;

    @Value("${app.init.admin.department:Computer Science & Engineering}")
    private String adminDepartment;

    @Value("${app.init.admin.semester:Faculty / Admin}")
    private String adminSemester;

    @Value("${app.init.student.email:student@college.edu}")
    private String studentEmail;

    @Value("${app.init.student.password:Password123!}")
    private String studentPassword;

    @Value("${app.init.student.name:Aarav Sharma}")
    private String studentName;

    @Value("${app.init.student.department:Computer Science & Engineering}")
    private String studentDepartment;

    @Value("${app.init.student.semester:Semester 6}")
    private String studentSemester;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!initDefaultAccounts) {
            logger.info("Default account initialization is disabled.");
            return;
        }

        seedAdminAccount();
        seedStudentAccount();
    }

    private void seedAdminAccount() {
        String normalizedEmail = adminEmail.trim().toLowerCase();
        if (!userRepository.existsByEmail(normalizedEmail)) {
            User admin = new User(
                    adminName,
                    normalizedEmail,
                    passwordEncoder.encode(adminPassword),
                    adminDepartment,
                    adminSemester,
                    Role.ADMIN
            );
            userRepository.save(admin);
            logger.info("Initial Administrator account provisioned: {}", normalizedEmail);
        } else {
            logger.debug("Administrator account {} already exists.", normalizedEmail);
        }
    }

    private void seedStudentAccount() {
        String normalizedEmail = studentEmail.trim().toLowerCase();
        if (!userRepository.existsByEmail(normalizedEmail)) {
            User student = new User(
                    studentName,
                    normalizedEmail,
                    passwordEncoder.encode(studentPassword),
                    studentDepartment,
                    studentSemester,
                    Role.STUDENT
            );
            userRepository.save(student);
            logger.info("Evaluation Student account provisioned: {}", normalizedEmail);
        } else {
            logger.debug("Evaluation student account {} already exists.", normalizedEmail);
        }
    }
}
