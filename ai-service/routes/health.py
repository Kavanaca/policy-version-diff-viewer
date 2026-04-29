from flask import Blueprint, jsonify
from datetime import datetime
import time
import os

health_bp = Blueprint("health", __name__)

# Track app start time (process-level uptime)
START_TIME = time.time()


@health_bp.route("/health", methods=["GET"])
def health():
    try:
        # Calculate uptime safely
        uptime_seconds = max(0, int(time.time() - START_TIME))

        response = {
            "status": "healthy",
            "service": "ai-service",
            "uptime_seconds": uptime_seconds,
            "model": os.getenv("GROQ_MODEL", "unknown"),
            "average_response_time_ms": None,  # placeholder for future metrics
            "generated_at": datetime.utcnow().isoformat()
        }

        return jsonify(response), 200

    except Exception:
        return jsonify({
            "status": "unhealthy",
            "service": "ai-service",
            "generated_at": datetime.utcnow().isoformat()
        }), 500