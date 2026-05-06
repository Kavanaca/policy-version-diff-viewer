"""
Day 7 — AI Developer 2
Verify all 4 OWASP ZAP findings are fixed.
Run with: pytest test/test_security_headers.py -v
"""
import sys
import os
import pytest

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))


@pytest.fixture
def client():
    from app import create_app
    app = create_app()
    app.config["TESTING"] = True
    return app.test_client()


class TestZapFindingsFixed:

    # Fix 4 — X-Content-Type-Options Header Missing
    def test_x_content_type_options_present(self, client):
        resp = client.get("/health")
        assert "X-Content-Type-Options" in resp.headers
        assert resp.headers["X-Content-Type-Options"] == "nosniff"

    # Fix 1 — Content Security Policy Header Not Set
    def test_content_security_policy_present(self, client):
        resp = client.get("/health")
        assert "Content-Security-Policy" in resp.headers
        csp = resp.headers["Content-Security-Policy"]
        assert "default-src" in csp
        assert "frame-ancestors" in csp

    # Fix 3 — Server Leaks Version Information
    def test_server_header_not_leaking_version(self, client):
        resp = client.get("/health")
        server = resp.headers.get("Server", "")
        assert "Werkzeug" not in server
        assert "Python" not in server
        assert "Flask" not in server

    # Fix 2 — HTTP Only Site
    def test_strict_transport_security_present(self, client):
        resp = client.get("/health")
        assert "Strict-Transport-Security" in resp.headers
        hsts = resp.headers["Strict-Transport-Security"]
        assert "max-age" in hsts

    # Additional headers
    def test_x_frame_options_present(self, client):
        resp = client.get("/health")
        assert "X-Frame-Options" in resp.headers
        assert resp.headers["X-Frame-Options"] == "DENY"

    def test_referrer_policy_present(self, client):
        resp = client.get("/health")
        assert "Referrer-Policy" in resp.headers

    def test_xss_protection_present(self, client):
        resp = client.get("/health")
        assert "X-XSS-Protection" in resp.headers

    def test_permissions_policy_present(self, client):
        resp = client.get("/health")
        assert "Permissions-Policy" in resp.headers

    def test_health_still_works(self, client):
        resp = client.get("/health")
        assert resp.status_code == 200

    def test_root_endpoint_works(self, client):
        resp = client.get("/")
        assert resp.status_code == 200