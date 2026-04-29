from flask import Blueprint, request, jsonify
from datetime import datetime

from services.ai_client import call_ai  # use real AI

recommend_bp = Blueprint("recommend", __name__)


# Load prompt
def load_prompt():
    try:
        with open("prompts/recommend_prompt.txt", "r", encoding="utf-8") as file:
            return file.read()
    except Exception as e:
        raise Exception(f"Failed to load recommend prompt: {str(e)}")


@recommend_bp.route("/recommend", methods=["POST"])
def recommend():
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

        # Call AI (Groq via ai_client)
        parsed_response = call_ai(final_prompt)

        # ❗ Validate AI response
        if (
            not parsed_response
            or not isinstance(parsed_response, list)
            or len(parsed_response) != 3
        ):
            raise ValueError("Invalid AI response")

        # Optional: validate fields inside each item
        for item in parsed_response:
            if not all(key in item for key in ["action_type", "description", "priority"]):
                raise ValueError("Missing required fields in AI response")

        # Success response
        return jsonify({
            "recommendations": parsed_response,
            "generated_at": datetime.utcnow().isoformat()
        }), 200

    except Exception:
        # Fallback (NOW MATCHES PROMPT STRUCTURE)
        return jsonify({
            "recommendations": [
                {
                    "action_type": "REVIEW",
                    "description": "Review the policy for clarity and completeness",
                    "priority": "Medium",
                    "reason": "Fallback due to AI processing failure"
                },
                {
                    "action_type": "UPDATE",
                    "description": "Improve wording to make terms easier to understand",
                    "priority": "Medium",
                    "reason": "Fallback due to AI processing failure"
                },
                {
                    "action_type": "ADD",
                    "description": "Include missing coverage details and conditions",
                    "priority": "High",
                    "reason": "Fallback due to AI processing failure"
                }
            ],
            "is_fallback": True,
            "generated_at": datetime.utcnow().isoformat()
        }), 200