# SECURITY.md — Tool-91 Policy Version Diff Viewer

## 1. Overview

This document covers the security threat model,
tests conducted, findings fixed and residual risks
for Tool-91.

---

## 2. Threat Model

| Threat               | Description                | Mitigation                    |
| -------------------- | -------------------------- | ----------------------------- |
| SQL Injection        | Malicious SQL in input     | JPA parameterized queries     |
| XSS                  | Script injection in fields | Input sanitisation            |
| JWT Tampering        | Forged tokens              | HMAC-SHA256 signing           |
| Brute Force          | Password guessing          | BCrypt + rate limiting        |
| CSRF                 | Cross site request forgery | CSRF disabled, JWT stateless  |
| Unauthorized Access  | No token access            | JWT required on all endpoints |
| Privilege Escalation | USER acting as ADMIN       | RBAC with @PreAuthorize       |
| Prompt Injection     | AI input manipulation      | Input sanitisation middleware |

---

## 3. Security Tests Conducted

| Test                         | Method  | Result     |
| ---------------------------- | ------- | ---------- |
| No token — 401 returned      | Postman | ✅ Pass    |
| USER deletes — 403 returned  | Postman | ✅ Pass    |
| ADMIN deletes — 200 returned | Postman | ✅ Pass    |
| Empty input — 400 returned   | Postman | ✅ Pass    |
| Invalid JWT — 401 returned   | Postman | ✅ Pass    |
| SQL injection attempt        | Postman | ✅ Blocked |
| XSS in title field           | Postman | ✅ Blocked |

---

## 4. Findings Fixed

| Finding            | Severity | Fix Applied              |
| ------------------ | -------- | ------------------------ |
| No rate limiting   | Medium   | Added flask-limiter      |
| Weak JWT secret    | High     | Enforced 32+ char secret |
| Missing RBAC       | High     | Added @PreAuthorize      |
| H2 console exposed | Low      | Restricted to dev only   |

---

## 5. Residual Risks

| Risk                          | Severity | Reason                  |
| ----------------------------- | -------- | ----------------------- |
| Email not configured          | Low      | SMTP credentials needed |
| H2 used instead of PostgreSQL | Medium   | Docker setup pending    |

---

## 6. Team Sign-Off

| Member   | Role              | Signed |
| -------- | ----------------- | ------ |
| Member 1 | Java Developer 1  | ✅     |
| Member 2 | Java Developer 2  | ✅     |
| Member 3 | AI Developer 1    | ✅     |
| Member 4 | AI Developer 2    | ✅     |
| Member 5 | Security Reviewer | ✅     |

---

_Tool-91 Security Review — May 2026_
