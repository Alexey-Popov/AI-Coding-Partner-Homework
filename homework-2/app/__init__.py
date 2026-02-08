"""
Flask application factory.

Creates and configures the Flask app, registers blueprints.
"""

import os

from flask import Flask

from app.config import config_map


def create_app(config_name: str | None = None) -> Flask:
    """
    Build and return a configured Flask application.

    *config_name* selects the configuration class from config_map.
    Defaults to the FLASK_ENV environment variable, then "default".
    """
    if config_name is None:
        config_name = os.environ.get("FLASK_ENV", "default")

    app = Flask(__name__)
    app.config.from_object(config_map[config_name])

    # Register blueprints
    from app.routes.tickets import tickets_bp
    app.register_blueprint(tickets_bp)

    return app
