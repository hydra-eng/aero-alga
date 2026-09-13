from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc
from datetime import datetime, timedelta
from app.database import get_db, TelemetryRecord, Node
from app.models.schemas import TelemetryPayload
from app.services.yield_engine import calculate_efficiencies
from app.services.anomaly import check_anomalies
from app.api.v1.websocket import ws_manager

router = APIRouter()

@router.post("/ingest", response_model=dict)
async def ingest_telemetry(payload: TelemetryPayload, db: AsyncSession = Depends(get_db)):
    # 1. Compute derived metrics via Yield & Performance Engine
    processed = calculate_efficiencies(payload)
    alerts = check_anomalies(processed)

    # 2. Update Node last_seen / register if new
    node_res = await db.execute(select(Node).where(Node.id == processed.node_id))
    node = node_res.scalar_one_or_none()
    if node:
        node.last_seen = datetime.utcnow()
        node.status = "WARNING" if alerts else "ONLINE"
    else:
        node = Node(
            id=processed.node_id,
            name=f"AeroAlga-{processed.node_id}",
            location="Primary Chamber",
            status="WARNING" if alerts else "ONLINE",
            last_seen=datetime.utcnow()
        )
        db.add(node)

    # 3. Store record in DB
    record = TelemetryRecord(
        node_id=processed.node_id,
        timestamp=datetime.utcnow(),
        flow_rate=processed.flow_rate,
        runtime_sec=processed.runtime_sec,
        pm25_in=processed.pm25_in,
        pm25_out=processed.pm25_out,
        pm10_in=processed.pm10_in,
        pm10_out=processed.pm10_out,
        pm_efficiency=processed.pm_efficiency,
        co2_in=processed.co2_in,
        co2_out=processed.co2_out,
        co2_efficiency=processed.co2_efficiency,
        o2_out_pct=processed.o2_out_pct,
        ph=processed.ph,
        water_temp=processed.water_temp,
        turbidity_ntu=processed.turbidity_ntu,
        dissolved_oxygen=processed.dissolved_oxygen,
        light_lux=processed.light_lux,
        cumulative_volume_l=processed.cumulative_volume_l,
        biomass_accum_kg=processed.biomass_accum_kg,
        co2_sequestered_kg=processed.co2_sequestered_kg,
        carbon_credits_t=processed.carbon_credits_t,
        n_removal_pct=processed.n_removal_pct,
        p_removal_pct=processed.p_removal_pct,
        pump_speed_pct=processed.pump_speed_pct,
        led_assist_on=processed.led_assist_on,
    )
    db.add(record)
    await db.commit()

    # 4. Broadcast live telemetry to active WebSockets (Android Apps & Web Dashboards)
    telemetry_dict = processed.model_dump()
    telemetry_dict["alerts"] = [a.model_dump() for a in alerts]
    telemetry_dict["timestamp"] = datetime.utcnow().isoformat()
    await ws_manager.broadcast_telemetry(processed.node_id, telemetry_dict)

    return {"status": "ok", "efficiency_pm": processed.pm_efficiency, "efficiency_co2": processed.co2_efficiency, "alerts": alerts}

@router.get("/latest/{node_id}")
async def get_latest_telemetry(node_id: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(TelemetryRecord)
        .where(TelemetryRecord.node_id == node_id)
        .order_by(desc(TelemetryRecord.timestamp))
        .limit(1)
    )
    record = result.scalar_one_or_none()
    if not record:
        raise HTTPException(status_code=404, detail="No telemetry found for this node")
    return record

@router.get("/history/{node_id}")
async def get_telemetry_history(
    node_id: str,
    hours: int = Query(24, ge=1, le=168),
    db: AsyncSession = Depends(get_db)
):
    cutoff = datetime.utcnow() - timedelta(hours=hours)
    result = await db.execute(
        select(TelemetryRecord)
        .where(TelemetryRecord.node_id == node_id, TelemetryRecord.timestamp >= cutoff)
        .order_by(TelemetryRecord.timestamp)
    )
    records = result.scalars().all()
    return records
