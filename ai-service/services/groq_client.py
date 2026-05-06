import json
import logging
import os
import re
import time

import requests
from dotenv import load_dotenv

load_dotenv()

# ── Logging (Requirement 4) ───────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger("groq_client")

# ── Constants ─────────────────────────────────────────────────────────────────
GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
GROQ_MODEL   = os.getenv("GROQ_MODEL", "llama-3.3-70b-versatile")

MAX_RETRIES      = 3   # Requirement 3
BASE_BACKOFF_SEC = 1   # 1s -> 2s -> 4s
REQUEST_TIMEOUT  = 30  # seconds


class GroqClient:

    def __init__(self, temperature: float = 0.3, max_tokens: int = 1024):
        if not GROQ_API_KEY:
            raise EnvironmentError("GROQ_API_KEY not set in .env")

        self.model       = GROQ_MODEL
        self.temperature = temperature
        self.max_tokens  = max_tokens
        self.headers     = {
            "Authorization": f"Bearer {GROQ_API_KEY}",
            "Content-Type":  "application/json",
        }

    # ── Requirement 1: API Call ───────────────────────────────────────────────
    def complete(self, prompt: str, system_prompt: str = None) -> dict:
        """
        Send prompt to Groq. Returns a result dict with:
          - content     : raw model text
          - parsed      : Python dict/list (or None)
          - is_fallback : True if all retries failed
        """
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": prompt})

        payload = {
            "model":       self.model,
            "messages":    messages,
            "temperature": self.temperature,
            "max_tokens":  self.max_tokens,
        }

        last_error = None

        # ── Requirement 3: 3-retry with exponential backoff ───────────────────
        for attempt in range(1, MAX_RETRIES + 1):
            try:
                logger.info("Groq API call — attempt %d/%d", attempt, MAX_RETRIES)

                response = requests.post(
                    GROQ_API_URL,
                    headers=self.headers,
                    json=payload,
                    timeout=REQUEST_TIMEOUT,
                )

                # Handle rate limit separately
                if response.status_code == 429:
                    wait = float(response.headers.get("Retry-After", BASE_BACKOFF_SEC * attempt))
                    logger.warning("Rate limited. Waiting %.0fs.", wait)
                    time.sleep(wait)
                    continue

                response.raise_for_status()

                # ── Requirement 2: JSON Parsing ───────────────────────────────
                raw     = response.json()["choices"][0]["message"]["content"]
                parsed  = self._parse_json(raw)

                logger.info("Groq call succeeded on attempt %d.", attempt)
                return {
                    "content":     raw,
                    "parsed":      parsed,
                    "is_fallback": False,
                    "attempt":     attempt,
                }

            # ── Requirement 4: Error Logging ──────────────────────────────────
            except requests.exceptions.Timeout:
                last_error = f"Attempt {attempt}: timed out after {REQUEST_TIMEOUT}s"
                logger.error(last_error)

            except requests.exceptions.ConnectionError as e:
                last_error = f"Attempt {attempt}: connection error — {e}"
                logger.error(last_error)

            except requests.exceptions.HTTPError as e:
                last_error = f"Attempt {attempt}: HTTP {e.response.status_code} — {e}"
                logger.error(last_error)
                if 400 <= e.response.status_code < 500:
                    break  # client error, no point retrying

            except (KeyError, IndexError, TypeError) as e:
                last_error = f"Attempt {attempt}: unexpected response shape — {e}"
                logger.error(last_error)

            except Exception as e:
                last_error = f"Attempt {attempt}: {type(e).__name__} — {e}"
                logger.error(last_error)

            # Backoff before next attempt (Requirement 3)
            if attempt < MAX_RETRIES:
                wait = BASE_BACKOFF_SEC * (2 ** (attempt - 1))  # 1s, 2s, 4s
                logger.info("Waiting %.0fs before attempt %d.", wait, attempt + 1)
                time.sleep(wait)

        # All retries exhausted
        logger.error("All %d attempts failed. Last error: %s", MAX_RETRIES, last_error)
        return {
            "content":         None,
            "parsed":          None,
            "is_fallback":     True,
            "fallback_reason": last_error or "Unknown error",
        }

    # ── Requirement 2: JSON Parsing ───────────────────────────────────────────
    def _parse_json(self, raw: str):
        """
        Extract JSON from model text.
        Handles markdown fences the model often wraps around JSON:
            ```json
            { "key": "value" }
            ```
        Returns parsed dict/list, or None if no valid JSON found.
        """
        if not raw:
            return None

        # Strip ```json ... ``` fences
        cleaned = re.sub(r"```(?:json)?\s*", "", raw, flags=re.IGNORECASE)
        cleaned = cleaned.replace("```", "").strip()

        # Try direct parse
        try:
            return json.loads(cleaned)
        except json.JSONDecodeError:
            pass

        # Try extracting first { } or [ ] block
        for pattern in (r"(\{.*\})", r"(\[.*\])"):
            match = re.search(pattern, cleaned, re.DOTALL)
            if match:
                try:
                    return json.loads(match.group(1))
                except json.JSONDecodeError:
                    continue

        logger.warning("Could not parse JSON. Raw text in result['content'].")
        return None