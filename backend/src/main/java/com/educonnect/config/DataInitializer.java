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
    private final com.educonnect.repository.ResourceRepository resourceRepository;
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

    public DataInitializer(
            UserRepository userRepository,
            com.educonnect.repository.ResourceRepository resourceRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
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
        seedSampleResources();
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

    private void seedSampleResources() {
        if (resourceRepository.count() > 0) {
            logger.debug("Educational resources already exist in database.");
            return;
        }

        User uploader = userRepository.findByEmail(studentEmail.trim().toLowerCase())
                .orElse(null);

        com.educonnect.entity.Resource r1 = new com.educonnect.entity.Resource();
        r1.setTitle("Data Structures & Algorithms Comprehensive Notes");
        r1.setDescription("Complete handwritten and digital notes covering Trees, Graphs, Dynamic Programming, and Sorting algorithms with Big-O complexity analysis.");
        r1.setSubject("Data Structures");
        r1.setDepartment("Computer Science & Engineering");
        r1.setSemester("Semester 3");
        r1.setCategory(com.educonnect.enums.ResourceCategory.NOTES);
        r1.setFileType("PDF");
        r1.setFileSize(4718592L); // 4.5 MB
        r1.setFileName("DSA_Comprehensive_Notes_2026.pdf");
        r1.setStatus(com.educonnect.enums.ResourceStatus.APPROVED);
        r1.setDownloadCount(142);
        r1.setTags("algorithms, trees, graphs, dynamic-programming");
        r1.setUploadedBy(uploader);

        com.educonnect.entity.Resource r2 = new com.educonnect.entity.Resource();
        r2.setTitle("Operating Systems End-Semester Examination 2024");
        r2.setDescription("Previous year university question paper with marking scheme and model answers for Process Scheduling, Memory Management, and Deadlocks.");
        r2.setSubject("Operating Systems");
        r2.setDepartment("Computer Science & Engineering");
        r2.setSemester("Semester 4");
        r2.setCategory(com.educonnect.enums.ResourceCategory.QP);
        r2.setFileType("PDF");
        r2.setFileSize(2306867L); // 2.2 MB
        r2.setFileName("OS_Exam_Paper_2024.pdf");
        r2.setStatus(com.educonnect.enums.ResourceStatus.APPROVED);
        r2.setDownloadCount(98);
        r2.setTags("os, question-paper, past-exam, sem4");
        r2.setUploadedBy(uploader);

        com.educonnect.entity.Resource r3 = new com.educonnect.entity.Resource();
        r3.setTitle("Database Management Systems Lab Manual & SQL Exercises");
        r3.setDescription("Laboratory guide featuring 12 practical experiments, ER diagrams, normalization steps, SQL query syntax, and PL/SQL procedures.");
        r3.setSubject("Database Management Systems");
        r3.setDepartment("Information Technology");
        r3.setSemester("Semester 4");
        r3.setCategory(com.educonnect.enums.ResourceCategory.LAB);
        r3.setFileType("PDF");
        r3.setFileSize(3670016L); // 3.5 MB
        r3.setFileName("DBMS_Lab_Manual_Full.pdf");
        r3.setStatus(com.educonnect.enums.ResourceStatus.APPROVED);
        r3.setDownloadCount(175);
        r3.setTags("dbms, sql, lab, er-diagram, queries");
        r3.setUploadedBy(uploader);

        com.educonnect.entity.Resource r4 = new com.educonnect.entity.Resource();
        r4.setTitle("Computer Networks Protocol Summary & WireShark Lab Guide");
        r4.setDescription("Layer-by-layer reference sheet covering TCP/IP, OSI model, packet flow, subnetting formulas, and hands-on packet inspection tutorial.");
        r4.setSubject("Computer Networks");
        r4.setDepartment("Electronics & Communication");
        r4.setSemester("Semester 5");
        r4.setCategory(com.educonnect.enums.ResourceCategory.TUTORIALS);
        r4.setFileType("PDF");
        r4.setFileSize(2831155L); // 2.7 MB
        r4.setFileName("CN_Protocols_Wireshark_Guide.pdf");
        r4.setStatus(com.educonnect.enums.ResourceStatus.APPROVED);
        r4.setDownloadCount(83);
        r4.setTags("networking, tcp-ip, wireshark, routing");
        r4.setUploadedBy(uploader);

        com.educonnect.entity.Resource r5 = new com.educonnect.entity.Resource();
        r5.setTitle("Python & Machine Learning Starter Lab Exercises");
        r5.setDescription("Code snippets and Jupyter notebooks covering NumPy, Pandas, Scikit-learn, Linear Regression, and Decision Tree classifications.");
        r5.setSubject("Machine Learning");
        r5.setDepartment("Artificial Intelligence & Data Science");
        r5.setSemester("Semester 6");
        r5.setCategory(com.educonnect.enums.ResourceCategory.PROG);
        r5.setFileType("ZIP");
        r5.setFileSize(5452595L); // 5.2 MB
        r5.setFileName("ML_Python_Exercises.zip");
        r5.setStatus(com.educonnect.enums.ResourceStatus.APPROVED);
        r5.setDownloadCount(210);
        r5.setTags("python, machine-learning, jupyter, pandas");
        r5.setUploadedBy(uploader);

        // Pending submission to test moderator approval queue
        com.educonnect.entity.Resource r6 = new com.educonnect.entity.Resource();
        r6.setTitle("Design and Analysis of Algorithms Mid-Term Review Sheet");
        r6.setDescription("Student-contributed formula sheet for divide-and-conquer recurrences, Master Theorem, and Greedy Huffman coding.");
        r6.setSubject("Algorithms");
        r6.setDepartment("Computer Science & Engineering");
        r6.setSemester("Semester 5");
        r6.setCategory(com.educonnect.enums.ResourceCategory.NOTES);
        r6.setFileType("PDF");
        r6.setFileSize(1572864L); // 1.5 MB
        r6.setFileName("DAA_Midterm_Review.pdf");
        r6.setStatus(com.educonnect.enums.ResourceStatus.PENDING);
        r6.setDownloadCount(0);
        r6.setTags("algorithms, daa, review, formulas");
        r6.setUploadedBy(uploader);

        resourceRepository.saveAll(java.util.List.of(r1, r2, r3, r4, r5, r6));
        logger.info("Sample educational resources provisioned for initial exploration and moderation testing.");
    }
}
