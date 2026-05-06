import os
import logging
from flask import Flask, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from flask_cors import CORS
from middleware import register_sanitise_middleware

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(name)s — %(message)s",
)
logger = logging.getLogger(__name__)


def create_app() -> Flask:
    app = Flask(__name__)

    # 🔹 CORS (restricted)
    CORS(
        app,
        origins=[
            "http://localhost",
            "http://localhost:3000",
            "http://localhost:80",
        ],
        methods=["GET", "POST"],
        allow_headers=["Content-Type", "Authorization"]
    )

    # 🔹 Rate Limiter
    Limiter(
        key_func=get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri=os.getenv("REDIS_URL", "memory://"),
        headers_enabled=True,
    )

    # 🔹 Sanitisation Middleware
    register_sanitise_middleware(app)

    # 🔹 Security Headers
    @app.after_request
    def add_security_headers(response):

        response.headers["X-Content-Type-Options"] = "nosniff"

        response.headers["Content-Security-Policy"] = (
            "default-src 'self'; "
            "script-src 'self'; "
            "script-src-elem 'self'; "
            "script-src-attr 'none'; "
            "style-src 'self'; "
            "style-src-elem 'self'; "
            "style-src-attr 'none'; "
            "img-src 'self' data:; "
            "font-src 'self'; "
            "connect-src 'self'; "
            "frame-ancestors 'none'; "
            "form-action 'self'; "
            "base-uri 'self';"
        )

        response.headers["Strict-Transport-Security"] = (
            "max-age=31536000; includeSubDomains"
        )

        # ✅ SAFE FIX (no crash)
        response.headers.pop("Server", None)
        response.headers["Server"] = "ai-service"

        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
        response.headers["Permissions-Policy"] = (
            "geolocation=(), microphone=(), camera=()"
        )

        return response

    # 🔹 Root endpoint
    @app.route("/", methods=["GET"])
    def index():
        return jsonify({
            "service": "ai-service",
            "version": "1.0",
            "status": "running",
            "endpoints": [
                "/health",
                "/describe",
                "/recommend",
                "/generate-report"
            ]
        }), 200

    # 🔹 Register Blueprints
    try:
        from routes.describe import describe_bp
        app.register_blueprint(describe_bp)
        logger.info("describe_bp registered")
    except ImportError:
        logger.warning("describe route not found")

    try:
        from routes.recommend import recommend_bp
        app.register_blueprint(recommend_bp)
        logger.info("recommend_bp registered")
    except ImportError:
        logger.warning("recommend route not found")

    try:
        from routes.health import health_bp
        app.register_blueprint(health_bp)
        logger.info("health_bp registered")
    except ImportError:
        logger.warning("health route not found")

    try:
        from routes.generate_report import report_bp
        app.register_blueprint(report_bp)
        logger.info("report_bp registered")
    except ImportError:
        logger.warning("generate_report route not found")

    # 🔹 Error Handlers
    @app.errorhandler(429)
    def rate_limit_exceeded(e):
        logger.warning("Rate limit exceeded from %s", get_remote_address())
        return jsonify({
            "error": "Rate limit exceeded",
            "detail": "Max 30 requests per minute"
        }), 429

    @app.errorhandler(400)
    def bad_request(e):
        return jsonify({
            "error": "Bad request",
            "detail": str(e)
        }), 400

    @app.errorhandler(500)
    def internal_error(e):
        logger.error("Server error: %s", e)
        return jsonify({
            "error": "Internal server error"
        }), 500

    return app


# 🔹 App Entry
app = create_app()

if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=int(os.getenv("AI_PORT", 5000)),
        debug=False
    )