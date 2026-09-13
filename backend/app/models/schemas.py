from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional

class NodeBase(BaseModel):
    id: str
    name: str = "AeroAlga Node"
    location: str = "Indoor Lab"
    status: str = "ONLINE"
    ip_address: str = "127.0.0.1"
    firmware_version: str = "2.0.0-esp32"

class NodeCreate(NodeBase):
    pass

class NodeOut(NodeBase):
    last_seen: datetime

    class Config:
        from_attributes = True

class TelemetryPayload(BaseModel):
    node_id: str
    runtime_sec: int = Field(..., ge=0)
    flow_rate: float = Field(..., ge=0)

    # PM Sensor Data
    pm25_in: float
    pm25_out: float
    pm10_in: float
    pm10_out: float
    pm_efficiency: Optional[float] = None

    # CO2 Sensor Data
    co2_in: float
    co2_out: float
    co2_efficiency: Optional[float] = None
    o2_out_pct: float = 21.4

    # Culture Sensor Data
    ph: float
    water_temp: float
    turbidity_ntu: float
    dissolved_oxygen: float
    light_lux: float = 1200.0

    # Yield & Circular Economy
    cumulative_volume_l: Optional[float] = None
    biomass_accum_kg: Optional[float] = None
    co2_sequestered_kg: Optional[float] = None
    carbon_credits_t: Optional[float] = None
    n_removal_pct: float = 88.0
    p_removal_pct: float = 86.0

    # Actuation
    pump_speed_pct: int = Field(55, ge=0, le=100)
    led_assist_on: bool = False

class ActuationCommand(BaseModel):
    pump_speed_pct: Optional[int] = Field(None, ge=10, le=100)
    led_assist_on: Optional[bool] = None

class AnomalyAlert(BaseModel):
    parameter: str
    value: float
    safe_min: float
    safe_max: float
    severity: str
    message: str
