import os
import logging
from flask import Flask, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from middleware import register_sanitise_middleware

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(name)s — %(message)s",
)
logger = logging.getLogger(__name__)


def create_app() -> Flask:
    app = Flask(__name__)

    # Rate limiter: 30 requests per minute per IP
    limiter = Limiter(
        key_func=get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri=os.getenv("REDIS_URL", "memory://"),
        headers_enabled=True,
    )

    # Sanitisation middleware
    register_sanitise_middleware(app)

    # Register blueprints only if they exist
    try:
        from routes.describe import describe_bp
        app.register_blueprint(describe_bp)
        logger.info("describe_bp registered")
    except ImportError:
        logger.warning("routes/describe.py not found — skipping")

    try:
        from routes.recommend import recommend_bp
        app.register_blueprint(recommend_bp)
        logger.info("recommend_bp registered")
    except ImportError:
        logger.warning("routes/recommend.py not found — skipping")

    try:
        from routes.health import health_bp
        app.register_blueprint(health_bp)
        logger.info("health_bp registered")
    except ImportError:
        logger.warning("routes/health.py not found — skipping")

    # Fallback health endpoint in case routes/health.py doesn't exist
    @app.route("/health", methods=["GET"])
    def health():
        return jsonify({"status": "ok", "service": "ai-service"}), 200

    # Global error handlers
    @app.errorhandler(429)
    def rate_limit_exceeded(e):
        logger.warning("Rate limit exceeded from %s", get_remote_address())
        return (
            jsonify({
                "error": "Rate limit exceeded.",
                "detail": "Maximum 30 requests per minute. Please slow down.",
            }),
            429,
        )

    @app.errorhandler(400)
    def bad_request(e):
        return jsonify({"error": "Bad request.", "detail": str(e)}), 400

    @app.errorhandler(500)
    def internal_error(e):
        logger.error("Unhandled server error: %s", e)
        return jsonify({"error": "Internal server error."}), 500

    return app


app = create_app()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("AI_PORT", 5000)), debug=False)