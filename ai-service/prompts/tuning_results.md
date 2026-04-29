# Prompt Tuning Results — Day 6
**Date:** Monday, 21 April 2026
**Tester:** AI Developer 2
**Tool:** Tool-91 — Policy Version Diff Viewer

---

## Describe Prompt — `prompts/describe_prompt.txt`

### Test Run Results

| Input # | Policy Type | Score | Issues |
|---------|-------------|-------|--------|
| 1 | Health Insurance | 10/10 | None ✅ |
| 2 | Auto Insurance | 10/10 | None ✅ |
| 3 | Life Insurance | 10/10 | None ✅ |
| 4 | Property Insurance | 10/10 | None ✅ |
| 5 | Cyber Liability | 10/10 | None ✅ |
| 6 | Workers Compensation | 10/10 | None ✅ |
| 7 | Travel Insurance | 10/10 | None ✅ |
| 8 | Professional Indemnity | 10/10 | None ✅ |
| 9 | Directors and Officers | 10/10 | None ✅ |
| 10 | Product Liability | 10/10 | None ✅ |

### Summary

| Metric | Value |
|--------|-------|
| Scores | [10, 10, 10, 10, 10, 10, 10, 10, 10, 10] |
| Average Score | **10.0 / 10** |
| Passed (>= 7) | **10 / 10** |
| Target | >= 7.0 average |
| Status | ✅ **PASS** |

### Prompt Rewrite Required?
**No** — Average score 10.0/10 exceeds the 7.0 threshold. No rewrite needed.

---

## Recommend Prompt — `prompts/recommend_prompt.txt`

### Test Run Results

| Input # | Policy Type | Score | Issues |
|---------|-------------|-------|--------|
| 1 | Health Insurance | 10/10 | None ✅ |
| 2 | Auto Insurance | 10/10 | None ✅ |
| 3 | Life Insurance | 10/10 | None ✅ |
| 4 | Property Insurance | 10/10 | None ✅ |
| 5 | Cyber Liability | 10/10 | None ✅ |
| 6 | Workers Compensation | 10/10 | None ✅ |
| 7 | Travel Insurance | 10/10 | None ✅ |
| 8 | Professional Indemnity | 10/10 | None ✅ |
| 9 | Directors and Officers | 10/10 | None ✅ |
| 10 | Product Liability | 10/10 | None ✅ |

### Summary

| Metric | Value |
|--------|-------|
| Scores | [10, 10, 10, 10, 10, 10, 10, 10, 10, 10] |
| Average Score | **10.0 / 10** |
| Passed (>= 7) | **10 / 10** |
| Target | >= 7.0 average |
| Status | ✅ **PASS** |

### Prompt Rewrite Required?
**No** — Average score 10.0/10 exceeds the 7.0 threshold. No rewrite needed.

---

## Overall Results

| Prompt | Average Score | Target | Status |
|--------|--------------|--------|--------|
| describe_prompt.txt | 10.0 / 10 | >= 7.0 | ✅ PASS |
| recommend_prompt.txt | 10.0 / 10 | >= 7.0 | ✅ PASS |

---

## Prompts Rewritten

| Prompt | Reason | Changes Made |
|--------|--------|--------------|
| None | Both prompts scored 10.0/10 | No changes required |

---

## Scoring Criteria Used

### Describe Prompt Scoring (out of 10)
| Criterion | Points Deducted if Failed |
|-----------|--------------------------|
| Valid JSON response | -9 (score = 1 if invalid) |
| `summary` field present and >= 20 chars | -2 |
| `key_points` is a list of exactly 3 items | -1 |
| `policy_type` field present | -2 |
| `coverage_scope` field present | -2 |
| `complexity_level` is Low/Medium/High | -1 |

### Recommend Prompt Scoring (out of 10)
| Criterion | Points Deducted if Failed |
|-----------|--------------------------|
| Valid JSON array | -9 (score = 1 if invalid) |
| Exactly 3 recommendations | -3 |
| Valid `action_type` per rec (UPDATE/REVIEW/REMOVE/ADD) | -1 each |
| Valid `priority` per rec (High/Medium/Low) | -1 each |
| `description` >= 10 chars per rec | -1 each |

---

## Test Execution Details

| Item | Value |
|------|-------|
| Test file | `test/test_prompt_tuning.py` |
| Model used | `llama-3.3-70b-versatile` |
| Temperature | 0.3 (factual/structured output) |
| Max tokens | 1000 |
| Total inputs tested | 20 (10 per prompt) |
| Total time | 23.55s |
| pytest result | **2 passed** |
| Run date | 21 April 2026 |

---

## Sign-off

| Member | Role | Signature | Date |
|--------|------|-----------|------|
| | AI Developer 2 | ✅ Approved | 21 Apr 2026 |
| | AI Developer 1 | | |
| | Java Developer 1 | | |
| | Java Developer 2 | | |