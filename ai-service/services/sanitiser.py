import re
import html
import logging

logger = logging.getLogger(__name__)

# ── Prompt injection patterns ──────────────────────────────────────────────
INJECTION_PATTERNS = [
    r"ignore\s+(all\s+)?(previous|prior|above)\s+(instructions?|prompts?|context)",
    r"you\s+are\s+now",
    r"new\s+instructions?",
    r"system\s*:",
    r"<\s*system\s*>",
    r"forget\s+(everything|all|your|previous)",
    r"do\s+not\s+follow",
    r"override\s+(your\s+)?(instructions?|rules?|guidelines?)",
    r"disregard\s+(all\s+)?(previous|prior)",
    r"act\s+as\s+(if\s+you\s+are|a)",
    r"pretend\s+(you\s+are|to\s+be)",
    r"jailbreak",
    r"dan\s+mode",
    r"developer\s+mode",
    r"prompt\s*injection",
    r"\[\s*INST\s*\]",
    r"<\s*\|?\s*(im_start|im_end|endoftext)\s*\|?\s*>",
    r"###\s*(instruction|system|human|assistant)",
]

COMPILED_PATTERNS = [re.compile(p, re.IGNORECASE) for p in INJECTION_PATTERNS]


def strip_html(text: str) -> str:
    """Remove all HTML tags and unescape HTML entities."""
    text = html.unescape(text)
    text = re.sub(r"<[^>]+>", "", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def detect_prompt_injection(text: str) -> tuple[bool, str]:
    """
    Returns (is_injection, matched_pattern_description).
    True means the input is suspicious and should be rejected.
    """
    for pattern in COMPILED_PATTERNS:
        match = pattern.search(text)
        if match:
            logger.warning(
                "Prompt injection detected. Pattern: '%s' | Match: '%s'",
                pattern.pattern,
                match.group(0),
            )
            return True, match.group(0)
    return False, ""


def sanitise(text: str) -> tuple[str, bool, str]:
    """
    Full sanitisation pipeline.
    Returns: cleaned_text, is_injection, matched_fragment
    """
    if not isinstance(text, str):
        text = str(text)

    cleaned = strip_html(text)
    is_injection, matched = detect_prompt_injection(cleaned)
    return cleaned, is_injection, matched