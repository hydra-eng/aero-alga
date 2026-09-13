import os
from pydantic import BaseModel

class Settings(BaseModel):
    PROJECT_NAME: str = "AeroAlga BDPA-v2 Telemetry Hub"
    VERSION: str = "2.0.0"
    API_V1_PREFIX: str = "/api/v1"
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite+aiosqlite:///./aeroalga.db")
    MQTT_BROKER_HOST: str = os.getenv("MQTT_BROKER_HOST", "localhost")
    MQTT_BROKER_PORT: int = int(os.getenv("MQTT_BROKER_PORT", "1883"))
    MQTT_TELEMETRY_TOPIC: str = "aeroalga/nodes/+/telemetry"
    MQTT_CONTROL_TOPIC: str = "aeroalga/nodes/{node_id}/control"
    CORS_ORIGINS: list[str] = ["*"]

settings = Settings()
