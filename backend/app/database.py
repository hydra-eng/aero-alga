from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from sqlalchemy import Integer, Float, String, Boolean, DateTime
from datetime import datetime
from app.config import settings

engine = create_async_engine(settings.DATABASE_URL, echo=False)
AsyncSessionLocal = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)

class Base(DeclarativeBase):
    pass

class Node(Base):
    __tablename__ = "nodes"

    id: Mapped[str] = mapped_column(String(64), primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String(128), default="AeroAlga Node")
    location: Mapped[str] = mapped_column(String(128), default="Indoor Lab")
    status: Mapped[str] = mapped_column(String(32), default="ONLINE")
    last_seen: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    ip_address: Mapped[str] = mapped_column(String(64), default="127.0.0.1")
    firmware_version: Mapped[str] = mapped_column(String(32), default="2.0.0-esp32")

class TelemetryRecord(Base):
    __tablename__ = "telemetry_records"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    node_id: Mapped[str] = mapped_column(String(64), index=True)
    timestamp: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, index=True)

    # Flow & Runtime
    flow_rate: Mapped[float] = mapped_column(Float)
    runtime_sec: Mapped[int] = mapped_column(Integer)

    # Particulate Matter (µg/m³)
    pm25_in: Mapped[float] = mapped_column(Float)
    pm25_out: Mapped[float] = mapped_column(Float)
    pm10_in: Mapped[float] = mapped_column(Float)
    pm10_out: Mapped[float] = mapped_column(Float)
    pm_efficiency: Mapped[float] = mapped_column(Float)

    # Gaseous Exchange (ppm)
    co2_in: Mapped[float] = mapped_column(Float)
    co2_out: Mapped[float] = mapped_column(Float)
    co2_efficiency: Mapped[float] = mapped_column(Float)
    o2_out_pct: Mapped[float] = mapped_column(Float, default=21.4)

    # Bio-Homeostasis
    ph: Mapped[float] = mapped_column(Float)
    water_temp: Mapped[float] = mapped_column(Float)
    turbidity_ntu: Mapped[float] = mapped_column(Float)
    dissolved_oxygen: Mapped[float] = mapped_column(Float)
    light_lux: Mapped[float] = mapped_column(Float, default=1200.0)

    # Circular Economy & Yield
    cumulative_volume_l: Mapped[float] = mapped_column(Float)
    biomass_accum_kg: Mapped[float] = mapped_column(Float)
    co2_sequestered_kg: Mapped[float] = mapped_column(Float)
    carbon_credits_t: Mapped[float] = mapped_column(Float)
    n_removal_pct: Mapped[float] = mapped_column(Float)
    p_removal_pct: Mapped[float] = mapped_column(Float)

    # Actuation Feedback
    pump_speed_pct: Mapped[int] = mapped_column(Integer)
    led_assist_on: Mapped[bool] = mapped_column(Boolean)

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

async def get_db():
    async with AsyncSessionLocal() as session:
        yield session
