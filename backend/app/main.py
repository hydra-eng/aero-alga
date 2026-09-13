from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging

from app.config import settings
from app.database import init_db
from app.mqtt.client import mqtt_service
from app.api.v1 import nodes, telemetry, actuation, websocket

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("aeroalga")

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Initializing database schema...")
    await init_db()
    logger.info("Starting MQTT bridge service...")
    mqtt_service.start()
    yield
    # Shutdown
    logger.info("Stopping MQTT bridge service...")
    mqtt_service.stop()

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Production Telemetry & Orchestration Server for AeroAlga BDPA-v2 Photobioreactors",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount API Routers
app.include_router(nodes.router, prefix=f"{settings.API_V1_PREFIX}/nodes", tags=["Nodes"])
app.include_router(telemetry.router, prefix=f"{settings.API_V1_PREFIX}/telemetry", tags=["Telemetry"])
app.include_router(actuation.router, prefix=f"{settings.API_V1_PREFIX}/actuation", tags=["Actuation"])
app.include_router(websocket.router, prefix=f"{settings.API_V1_PREFIX}/ws", tags=["WebSocket Stream"])

@app.get("/health")
async def health_check():
    return {"status": "HEALTHY", "version": settings.VERSION, "service": settings.PROJECT_NAME}
