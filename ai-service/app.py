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

    # ── Fix: CORS — restrict to known origins only ────────────────────────
    CORS(app,
         origins=[
             "http://localhost",
             "http://localhost:3000",
             "http://localhost:80",
         ],
         methods=["GET", "POST"],
         allow_headers=["Content-Type", "Authorization"]
    )

    # ── Rate limiter: 30 requests per minute per IP ───────────────────────
    limiter = Limiter(
        key_func=get_remote_address,
        app=app,
        default_limits=["30 per minute"],
        storage_uri=os.getenv("REDIS_URL", "memory://"),
        headers_enabled=True,
    )

    # ── Sanitisation middleware ───────────────────────────────────────────
    register_sanitise_middleware(app)

    # ── Fix Z1, Z2, Z3, Z4: Security headers on every response ───────────
    @app.after_request
    def add_security_headers(response):

        # Fix Z4 — X-Content-Type-Options Header Missing
        response.headers["X-Content-Type-Options"] = "nosniff"

        # Fix Z1 — Content Security Policy Header Not Set
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

        # Fix Z2 — HTTP Only Site
        response.headers["Strict-Transport-Security"] = (
            "max-age=31536000; includeSubDomains"
        )

        # Fix Z3 — Server Leaks Version Information
        response.headers.remove("Server")
        response.headers.add("Server", "ai-service")

        # Additional hardening
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["X-XSS-Protection"] = "1; mode=block"
        response.headers["Referrer-Policy"] = "strict-origin-when-cross-origin"
        response.headers["Permissions-Policy"] = (
            "geolocation=(), microphone=(), camera=()"
        )

        return response

    # ── Root route — needed for ZAP scanning ─────────────────────────────
    @app.route("/", methods=["GET"])
    def index():
        return jsonify({
            "service": "ai-service",
            "version": "1.0",
            "status": "running",
            "endpoints": ["/health", "/describe", "/recommend"]
        }), 200

    # ── Register blueprints only if they exist ────────────────────────────
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

    # ── Fallback health endpoint ──────────────────────────────────────────
    @app.route("/health", methods=["GET"])
    def health():
        return jsonify({"status": "ok", "service": "ai-service"}), 200

    # ── Global error handlers ─────────────────────────────────────────────
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
    app.run(
        host="0.0.0.0",
        port=int(os.getenv("AI_PORT", 5000)),
        debug=False
    )