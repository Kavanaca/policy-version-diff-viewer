package com.internship.tool.service;

import com.internship.tool.entity.Policy;
import com.internship.tool.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final EmailService emailService;
    private final PolicyRepository policyRepository;

    // ─── Daily Reminder — 9AM Mon–Fri ─────────────────────
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void sendDailyReminder() {
        log.info("Running daily reminder scheduler...");
        try {
            List<Policy> draftPolicies =
                policyRepository.findByStatus("DRAFT");
            log.info("Found {} DRAFT policies",
                draftPolicies.size());
            for (Policy policy : draftPolicies) {
                emailService.sendReminderEmail(
                    "admin@tool91.com",
                    "Admin",
                    policy.getTitle(),
                    policy.getStatus(),
                    policy.getVersion()
                );
            }
            log.info("Daily reminders sent for {} policies",
                draftPolicies.size());
        } catch (Exception e) {
            log.error("Error in daily reminder: {}",
                e.getMessage());
        }
    }

    // ─── Deadline Alert — 8AM Every Day ───────────────────
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDeadlineAlert() {
        log.info("Running deadline alert scheduler...");
        try {
            List<Policy> expiredPolicies =
                policyRepository.findByStatus("EXPIRED");
            log.info("Found {} EXPIRED policies",
                expiredPolicies.size());
            for (Policy policy : expiredPolicies) {
                emailService.sendDeadlineAlert(
                    "admin@tool91.com",
                    "Admin",
                    policy.getTitle(),
                    policy.getStatus(),
                    policy.getVersion()
                );
            }
            log.info("Deadline alerts sent for {} policies",
                expiredPolicies.size());
        } catch (Exception e) {
            log.error("Error in deadline alert: {}",
                e.getMessage());
        }
    }
}