from flask import Blueprint, request, jsonify
from datetime import datetime

from services.ai_client import call_ai

report_bp = Blueprint("generate_report", __name__)


# Load prompt
def load_prompt():
    try:
        with open("prompts/report_prompt.txt", "r", encoding="utf-8") as file:
            return file.read()
    except Exception as e:
        raise Exception(f"Failed to load report prompt: {str(e)}")


@report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    try:
        if not request.is_json:
            return jsonify({"error": "Request must be JSON"}), 400

        data = request.get_json()
        policy_text = data.get("policy_text")

        if not policy_text or not policy_text.strip():
            return jsonify({"error": "policy_text is required"}), 400

        prompt_template = load_prompt()
        final_prompt = prompt_template.replace("{policy_text}", policy_text)

        parsed_response = call_ai(final_prompt)

        if not parsed_response or not isinstance(parsed_response, dict):
            raise ValueError("Invalid AI response")

        required_fields = [
            "title",
            "summary",
            "overview",
            "key_items",
            "recommendations"
        ]

        for field in required_fields:
            if field not in parsed_response:
                raise ValueError(f"Missing field: {field}")

        if not isinstance(parsed_response.get("key_items"), list) or len(parsed_response["key_items"]) != 3:
            raise ValueError("Invalid key_items")

        if not isinstance(parsed_response.get("recommendations"), list) or len(parsed_response["recommendations"]) != 3:
            raise ValueError("Invalid recommendations")

        parsed_response["generated_at"] = datetime.utcnow().isoformat()

        return jsonify(parsed_response), 200

    except Exception:
        return jsonify({
            "title": "Policy report unavailable",
            "summary": "Unable to generate report at this time.",
            "overview": "The policy report could not be generated due to a service issue.",
            "key_items": ["N/A", "N/A", "N/A"],
            "recommendations": ["N/A", "N/A", "N/A"],
            "is_fallback": True,
            "generated_at": datetime.utcnow().isoformat()
        }), 200