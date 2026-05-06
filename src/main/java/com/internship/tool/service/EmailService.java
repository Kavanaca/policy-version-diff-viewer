package com.internship.tool.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    // ─── Reminder Email ────────────────────────────────────
    @Async
    public void sendReminderEmail(String to, String name,
            String policyTitle, String status, String version) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping reminder");
            return;
        }
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("policyTitle", policyTitle);
            context.setVariable("status", status);
            context.setVariable("version", version);

            String html = templateEngine
                .process("email-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Policy Reminder: " + policyTitle);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Reminder email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send reminder to {}: {}",
                to, e.getMessage());
        }
    }

    // ─── Deadline Alert Email ──────────────────────────────
    @Async
    public void sendDeadlineAlert(String to, String name,
            String policyTitle, String status, String version) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping alert");
            return;
        }
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("policyTitle", policyTitle);
            context.setVariable("status", status);
            context.setVariable("version", version);

            String html = templateEngine
                .process("email-deadline", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("DEADLINE ALERT: " + policyTitle);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Deadline alert sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send deadline alert to {}: {}",
                to, e.getMessage());
        }
    }
}