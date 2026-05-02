import logging

from services.groq_client import GroqClient

logger = logging.getLogger(__name__)
client = None


def _get_client():
    global client
    if client is None:
        try:
            client = GroqClient()
        except EnvironmentError as exc:
            logger.error("Groq client unavailable: %s", exc)
            return None
    return client


def call_ai(prompt):
    client = _get_client()
    if client is None:
        return None

    result = client.complete(prompt)

    if result["is_fallback"] or result["parsed"] is None:
        return None

    return result["parsed"]