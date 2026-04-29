"""
Day 6 — AI Developer 2
Prompt Tuning — 10 real inputs per prompt, score accuracy.
Target: average score >= 7/10 for all prompts.

Uses AI Developer 1's GroqClient from services/groq_client.py

Run with: pytest tests/test_prompt_tuning.py -v -s
"""
import json
import os
import sys
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from services.groq_client import GroqClient


# ── Helper: Load Prompt File ───────────────────────────────────────────────
def load_prompt(filename: str) -> str:
    """Load prompt template from prompts/ directory."""
    prompt_path = os.path.join(
        os.path.dirname(__file__), "..", "prompts", filename
    )
    with open(prompt_path, "r", encoding="utf-8") as f:
        return f.read()


# ── 10 Real Policy Inputs ──────────────────────────────────────────────────
POLICY_INPUTS = [
    # 1
    "This health insurance policy covers inpatient and outpatient treatment "
    "for all enrolled employees and their dependents. The policy is effective "
    "from January 1 2026 and expires December 31 2026. Maximum coverage "
    "per person is fifty thousand dollars per annum.",

    # 2
    "Auto insurance policy for commercial vehicles. Covers third-party "
    "liability, collision damage, and theft. Vehicles must not exceed "
    "ten years of age. Premiums are reviewed annually.",

    # 3
    "Life insurance policy providing a death benefit of two hundred thousand "
    "dollars to named beneficiaries. Policy lapses if premiums are unpaid "
    "for more than sixty days. Suicide exclusion applies within first two years.",

    # 4
    "Property insurance covering office premises against fire, flood, "
    "and vandalism. Business interruption cover included up to six months. "
    "Deductible is two thousand five hundred dollars per claim.",

    # 5
    "Cyber liability policy covering data breaches, ransomware attacks, "
    "and regulatory fines. Includes round the clock incident response support. "
    "Coverage limit is one million dollars per incident.",

    # 6
    "Workers compensation policy covering all full-time and part-time "
    "employees for work-related injuries and illnesses. Weekly benefit "
    "is sixty six percent of average weekly wage. No waiting period for "
    "medical benefits.",

    # 7
    "Travel insurance policy for international business travel. Covers "
    "medical emergencies, trip cancellation, and lost luggage. "
    "Maximum trip duration is ninety days. War zones are excluded.",

    # 8
    "Professional indemnity policy for consulting firms. Covers claims "
    "arising from errors, omissions, and negligent advice. Retroactive "
    "date is January 2020. Limit of indemnity is five hundred thousand dollars.",

    # 9
    "Directors and officers liability policy protecting board members "
    "from personal liability arising from their corporate decisions. "
    "Side A, B, and C coverage included. Policy period is twelve months.",

    # 10
    "Product liability policy covering manufacturing defects and personal "
    "injury claims arising from distributed products. Global coverage "
    "excluding certain territories. Annual aggregate limit is two million dollars.",
]


# ══════════════════════════════════════════════════════════════════════════════
# SCORING FUNCTIONS
# ══════════════════════════════════════════════════════════════════════════════

def score_describe_response(result: dict) -> tuple[int, list[str]]:
    """
    Score a /describe response using GroqClient result dict.
    Uses result['parsed'] if available, falls back to result['content'].
    Returns (score, issues).
    """
    issues = []
    score = 10

    # Check for fallback
    if result.get("is_fallback"):
        return 0, [f"Groq API failed: {result.get('fallback_reason')}"]

    # Use parsed JSON if available
    data = result.get("parsed")

    if data is None:
        # Try manual parse of content
        try:
            data = json.loads(result.get("content", ""))
        except (json.JSONDecodeError, TypeError):
            return 1, ["Response is not valid JSON — prompt needs rewriting"]

    if not isinstance(data, dict):
        return 1, ["Response is not a JSON object"]

    # Check required fields
    required_fields = [
        "summary", "key_points", "policy_type",
        "coverage_scope", "complexity_level"
    ]
    for field in required_fields:
        if field not in data or data[field] is None:
            issues.append(f"Missing field: {field}")
            score -= 2

    # Check summary quality
    summary = data.get("summary", "")
    if not isinstance(summary, str) or len(summary) < 20:
        issues.append("Summary too short or missing")
        score -= 2

    # Check key_points
    kp = data.get("key_points", [])
    if not isinstance(kp, list):
        issues.append("key_points must be a list")
        score -= 2
    elif len(kp) != 3:
        issues.append(f"key_points must have 3 items, got {len(kp)}")
        score -= 1

    # Check complexity_level
    if data.get("complexity_level") not in ["Low", "Medium", "High"]:
        issues.append("complexity_level must be Low, Medium, or High")
        score -= 1

    return max(1, score), issues


def score_recommend_response(result: dict) -> tuple[int, list[str]]:
    """
    Score a /recommend response using GroqClient result dict.
    Returns (score, issues).
    """
    issues = []
    score = 10

    # Check for fallback
    if result.get("is_fallback"):
        return 0, [f"Groq API failed: {result.get('fallback_reason')}"]

    # Use parsed JSON if available
    data = result.get("parsed")

    if data is None:
        try:
            data = json.loads(result.get("content", ""))
        except (json.JSONDecodeError, TypeError):
            return 1, ["Response is not valid JSON — prompt needs rewriting"]

    if not isinstance(data, list):
        return 1, ["Response must be a JSON array"]

    # Must have exactly 3 recommendations
    if len(data) != 3:
        issues.append(f"Must have exactly 3 recommendations, got {len(data)}")
        score -= 3

    valid_action_types = {"UPDATE", "REVIEW", "REMOVE", "ADD"}
    valid_priorities   = {"High", "Medium", "Low"}

    for i, rec in enumerate(data[:3]):
        if not isinstance(rec, dict):
            issues.append(f"Recommendation {i+1} is not an object")
            score -= 2
            continue

        if rec.get("action_type") not in valid_action_types:
            issues.append(
                f"Rec {i+1}: invalid action_type '{rec.get('action_type')}'"
            )
            score -= 1

        if rec.get("priority") not in valid_priorities:
            issues.append(
                f"Rec {i+1}: invalid priority '{rec.get('priority')}'"
            )
            score -= 1

        desc = rec.get("description", "")
        if not isinstance(desc, str) or len(desc) < 10:
            issues.append(f"Rec {i+1}: description too short or missing")
            score -= 1

    return max(1, score), issues


# ══════════════════════════════════════════════════════════════════════════════
# TEST: DESCRIBE PROMPT — 10 INPUTS
# ══════════════════════════════════════════════════════════════════════════════

class TestDescribePromptTuning:

    def test_describe_prompt_10_inputs(self):
        """
        Run 10 real policy inputs through describe_prompt.txt.
        Each scored 1-10. Average must be >= 7.0.
        """
        # Load prompt template
        try:
            prompt_template = load_prompt("describe_prompt.txt")
        except FileNotFoundError:
            pytest.fail(
                "prompts/describe_prompt.txt not found. "
                "Create it before running tuning tests."
            )

        # Initialise GroqClient (temperature=0.3 for factual/structured output)
        try:
            client = GroqClient(temperature=0.3, max_tokens=1000)
        except EnvironmentError as e:
            pytest.skip(f"Groq not configured: {e}")

        scores  = []
        results = []

        print("\n" + "═" * 60)
        print("DESCRIBE PROMPT — 10 INPUT SCORING")
        print("═" * 60)

        for i, policy_text in enumerate(POLICY_INPUTS, 1):
            # Inject policy text into prompt template
            prompt = prompt_template.replace("{policy_text}", policy_text)

            # Call Groq using AI Dev 1's GroqClient
            result = client.complete(
                prompt=policy_text,
                system_prompt=prompt
            )

            score, issues = score_describe_response(result)
            scores.append(score)
            results.append({"input": i, "score": score, "issues": issues})

            # Print live result
            status = "✅" if score >= 7 else "❌"
            print(f"\nInput {i:2d}: {status} Score {score}/10")
            if issues:
                for issue in issues:
                    print(f"         ⚠  {issue}")
            if result.get("is_fallback"):
                print(f"         ⚠  FALLBACK: {result.get('fallback_reason')}")

        # Summary
        avg = sum(scores) / len(scores) if scores else 0
        passed = sum(1 for s in scores if s >= 7)

        print(f"\n{'─' * 60}")
        print(f"Scores:        {scores}")
        print(f"Average:       {avg:.1f}/10")
        print(f"Passed (>=7):  {passed}/{len(scores)}")
        print(f"Target:        >= 7.0 average")
        print(f"Status:        {'✅ PASS' if avg >= 7.0 else '❌ FAIL — rewrite prompt'}")
        print("─" * 60)

        assert avg >= 7.0, (
            f"Describe prompt average {avg:.1f}/10 is below 7.0. "
            f"Rewrite describe_prompt.txt. "
            f"Low scoring inputs: "
            f"{[r for r in results if r['score'] < 7]}"
        )


# ══════════════════════════════════════════════════════════════════════════════
# TEST: RECOMMEND PROMPT — 10 INPUTS
# ══════════════════════════════════════════════════════════════════════════════

class TestRecommendPromptTuning:

    def test_recommend_prompt_10_inputs(self):
        """
        Run 10 real policy inputs through recommend_prompt.txt.
        Each scored 1-10. Average must be >= 7.0.
        """
        try:
            prompt_template = load_prompt("recommend_prompt.txt")
        except FileNotFoundError:
            pytest.fail(
                "prompts/recommend_prompt.txt not found. "
                "Create it before running tuning tests."
            )

        try:
            client = GroqClient(temperature=0.3, max_tokens=1000)
        except EnvironmentError as e:
            pytest.skip(f"Groq not configured: {e}")

        scores  = []
        results = []

        print("\n" + "═" * 60)
        print("RECOMMEND PROMPT — 10 INPUT SCORING")
        print("═" * 60)

        for i, policy_text in enumerate(POLICY_INPUTS, 1):
            prompt = prompt_template.replace("{policy_text}", policy_text)

            result = client.complete(
                prompt=policy_text,
                system_prompt=prompt
            )

            score, issues = score_recommend_response(result)
            scores.append(score)
            results.append({"input": i, "score": score, "issues": issues})

            status = "✅" if score >= 7 else "❌"
            print(f"\nInput {i:2d}: {status} Score {score}/10")
            if issues:
                for issue in issues:
                    print(f"         ⚠  {issue}")
            if result.get("is_fallback"):
                print(f"         ⚠  FALLBACK: {result.get('fallback_reason')}")

        avg = sum(scores) / len(scores) if scores else 0
        passed = sum(1 for s in scores if s >= 7)

        print(f"\n{'─' * 60}")
        print(f"Scores:        {scores}")
        print(f"Average:       {avg:.1f}/10")
        print(f"Passed (>=7):  {passed}/{len(scores)}")
        print(f"Target:        >= 7.0 average")
        print(f"Status:        {'✅ PASS' if avg >= 7.0 else '❌ FAIL — rewrite prompt'}")
        print("─" * 60)

        assert avg >= 7.0, (
            f"Recommend prompt average {avg:.1f}/10 is below 7.0. "
            f"Rewrite recommend_prompt.txt. "
            f"Low scoring inputs: "
            f"{[r for r in results if r['score'] < 7]}"
        )