from flask import Blueprint, request, jsonify
from datetime import datetime

from services.ai_client import call_ai

describe_bp = Blueprint("describe", __name__)


# Load prompt
def load_prompt():
    try:
        with open("prompts/describe_prompt.txt", "r", encoding="utf-8") as file:
            return file.read()
    except Exception as e:
        raise Exception(f"Failed to load prompt: {str(e)}")


@describe_bp.route("/describe", methods=["POST"])
def describe():
    try:
        # Validate request
        if not request.is_json:
            return jsonify({"error": "Request must be JSON"}), 400

        data = request.get_json()
        policy_text = data.get("policy_text")

        if not policy_text or not policy_text.strip():
            return jsonify({"error": "policy_text is required"}), 400

        # Load prompt
        prompt_template = load_prompt()
        final_prompt = prompt_template.replace("{policy_text}", policy_text)

        # Call AI (already parsed JSON)
        parsed_response = call_ai(final_prompt)

        # Validate response
        if not parsed_response or not isinstance(parsed_response, dict):
            raise ValueError("Invalid AI response")

        required_fields = [
            "summary",
            "key_points",
            "policy_type",
            "coverage_scope",
            "effective_date_mentioned",
            "complexity_level"
        ]

        for field in required_fields:
            if field not in parsed_response:
                raise ValueError(f"Missing field: {field}")

        # Ensure key_points = 3
        if not isinstance(parsed_response.get("key_points"), list) or len(parsed_response["key_points"]) != 3:
            raise ValueError("Invalid key_points")

        # Add timestamp
        parsed_response["generated_at"] = datetime.utcnow().isoformat()

        return jsonify(parsed_response), 200

    except Exception:
        # fallback
        return jsonify({
            "summary": "Unable to process policy",
            "key_points": ["N/A", "N/A", "N/A"],
            "policy_type": "Other",
            "coverage_scope": "Unknown",
            "effective_date_mentioned": False,
            "complexity_level": "Medium",
            "is_fallback": True,
            "generated_at": datetime.utcnow().isoformat()
        }), 200