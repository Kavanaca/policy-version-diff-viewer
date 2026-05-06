import json
import logging
from flask import request, jsonify
from services.sanitiser import sanitise

logger = logging.getLogger(__name__)

# Fields to inspect in the JSON request body
FIELDS_TO_CHECK = ["text", "content", "query", "input", "description", "policy_text"]


def register_sanitise_middleware(app):
    """
    Register a before_request hook on the Flask app.
    - Strips HTML from all inspected string fields.
    - Rejects the request with 400 if prompt injection is detected.
    """

    @app.before_request
    def sanitise_request():
        # Only inspect JSON bodies
        if not request.is_json:
            return  # pass through non-JSON requests unchanged

        try:
            body = request.get_json(force=True, silent=True)
        except Exception:
            return  # malformed body — let the route handler deal with it

        if not body or not isinstance(body, dict):
            return

        for field in FIELDS_TO_CHECK:
            value = body.get(field)
            if not isinstance(value, str):
                continue

            cleaned, is_injection, matched = sanitise(value)

            if is_injection:
                logger.warning(
                    "Blocked request — prompt injection in field '%s': %s",
                    field,
                    matched,
                )
                return (
                    jsonify(
                        {
                            "error": "Invalid input detected.",
                            "detail": f"Field '{field}' contains disallowed content.",
                            "blocked_fragment": matched,
                        }
                    ),
                    400,
                )

            # Mutate the parsed body so downstream route handlers
            # receive clean text.  We re-inject it via environ so
            # request.get_json() inside the route still works.
            body[field] = cleaned

        # Push the cleaned body back into the WSGI environ
        cleaned_bytes = json.dumps(body).encode("utf-8")
        request.environ["wsgi.input"] = _BytesIO(cleaned_bytes)
        request.environ["CONTENT_LENGTH"] = str(len(cleaned_bytes))
        # Clear Flask's cached parsed JSON so it re-reads the cleaned bytes
        request._cached_json = (None, None)  # (on_true, on_false) cache slots


# ── Minimal BytesIO shim ───────────────────────────────────────────────────
import io

_BytesIO = io.BytesIO