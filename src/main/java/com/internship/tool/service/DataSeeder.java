package com.internship.tool.service;

import com.internship.tool.entity.Policy;
import com.internship.tool.entity.User;
import com.internship.tool.repository.PolicyRepository;
import com.internship.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedPolicies();
    }

    // ─── Seed Users ────────────────────────────────────────
    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding users...");

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@tool91.com");
            admin.setPassword(
                passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);

            User user = new User();
            user.setUsername("user1");
            user.setEmail("user1@tool91.com");
            user.setPassword(
                passwordEncoder.encode("user123"));
            user.setRole("USER");
            user.setEnabled(true);
            userRepository.save(user);

            User user2 = new User();
            user2.setUsername("user2");
            user2.setEmail("user2@tool91.com");
            user2.setPassword(
                passwordEncoder.encode("user123"));
            user2.setRole("USER");
            user2.setEnabled(true);
            userRepository.save(user2);

            log.info("Seeded 3 users successfully");
        }
    }

    // ─── Seed Policies ─────────────────────────────────────
    private void seedPolicies() {
        if (policyRepository.count() == 0) {
            log.info("Seeding 30 policies...");

            String[][] policies = {
                {"IT Security Policy",
                    "This policy defines IT security rules for all employees in the organization.",
                    "1.0", "ACTIVE", "admin"},
                {"Data Privacy Policy",
                    "This policy outlines how personal data is collected stored and processed.",
                    "2.0", "ACTIVE", "admin"},
                {"Remote Work Policy",
                    "Guidelines for employees working remotely including security and communication.",
                    "1.5", "ACTIVE", "admin"},
                {"Password Management Policy",
                    "Rules for creating managing and storing passwords securely.",
                    "1.0", "ACTIVE", "admin"},
                {"Acceptable Use Policy",
                    "Defines acceptable use of company IT resources and systems.",
                    "3.0", "ACTIVE", "admin"},
                {"Incident Response Policy",
                    "Procedures for responding to security incidents and breaches.",
                    "1.2", "ACTIVE", "admin"},
                {"Backup and Recovery Policy",
                    "Guidelines for data backup frequency storage and recovery procedures.",
                    "1.0", "DRAFT", "admin"},
                {"Email Communication Policy",
                    "Rules for professional email communication within the organization.",
                    "2.1", "DRAFT", "admin"},
                {"Social Media Policy",
                    "Guidelines for employee use of social media platforms professionally.",
                    "1.0", "DRAFT", "admin"},
                {"Vendor Management Policy",
                    "Procedures for selecting managing and evaluating third party vendors.",
                    "1.3", "DRAFT", "admin"},
                {"Access Control Policy",
                    "Rules for granting revoking and managing access to company systems.",
                    "2.0", "ACTIVE", "admin"},
                {"Change Management Policy",
                    "Process for managing changes to IT systems and infrastructure.",
                    "1.0", "ACTIVE", "admin"},
                {"Business Continuity Policy",
                    "Plans and procedures for maintaining operations during disruptions.",
                    "1.4", "ACTIVE", "admin"},
                {"Employee Code of Conduct",
                    "Standards of professional behavior expected from all employees.",
                    "3.0", "ACTIVE", "admin"},
                {"Leave and Attendance Policy",
                    "Rules for employee leave entitlements and attendance requirements.",
                    "2.2", "ACTIVE", "admin"},
                {"Travel and Expense Policy",
                    "Guidelines for business travel and expense reimbursement procedures.",
                    "1.1", "EXPIRED", "admin"},
                {"Procurement Policy",
                    "Procedures for purchasing goods and services for the organization.",
                    "1.0", "EXPIRED", "admin"},
                {"Asset Management Policy",
                    "Rules for managing company assets including hardware and software.",
                    "2.0", "EXPIRED", "admin"},
                {"Network Security Policy",
                    "Guidelines for securing company network infrastructure and systems.",
                    "1.5", "EXPIRED", "admin"},
                {"Cloud Usage Policy",
                    "Rules for using cloud services and storing data in cloud environments.",
                    "1.0", "EXPIRED", "admin"},
                {"Mobile Device Policy",
                    "Guidelines for using mobile devices to access company resources.",
                    "2.0", "DRAFT", "admin"},
                {"Software Development Policy",
                    "Standards for software development practices and code quality.",
                    "1.0", "DRAFT", "admin"},
                {"Whistleblower Policy",
                    "Procedures for reporting unethical behavior without fear of retaliation.",
                    "1.3", "ACTIVE", "admin"},
                {"Anti Harassment Policy",
                    "Zero tolerance policy for workplace harassment and discrimination.",
                    "2.1", "ACTIVE", "admin"},
                {"Environmental Policy",
                    "Commitment to environmental sustainability in business operations.",
                    "1.0", "ACTIVE", "admin"},
                {"Health and Safety Policy",
                    "Guidelines for maintaining safe and healthy workplace conditions.",
                    "1.2", "ACTIVE", "admin"},
                {"Training and Development Policy",
                    "Framework for employee learning development and skill enhancement.",
                    "1.0", "DRAFT", "admin"},
                {"Performance Management Policy",
                    "Process for setting goals evaluating performance and providing feedback.",
                    "2.0", "DRAFT", "admin"},
                {"Intellectual Property Policy",
                    "Rules for protecting company intellectual property and trade secrets.",
                    "1.1", "ACTIVE", "admin"},
                {"Compliance Policy",
                    "Framework for ensuring compliance with laws regulations and standards.",
                    "1.0", "ACTIVE", "admin"}
            };

            for (String[] policyData : policies) {
                Policy policy = new Policy();
                policy.setTitle(policyData[0]);
                policy.setContent(policyData[1]);
                policy.setVersion(policyData[2]);
                policy.setStatus(policyData[3]);
                policy.setCreatedBy(policyData[4]);
                policy.setDeleted(false);
                policyRepository.save(policy);
            }

            log.info("Seeded 30 policies successfully");
        }
    }
}