import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.database import init_db

@pytest.mark.asyncio
async def test_health_check():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        res = await ac.get("/health")
        assert res.status_code == 200
        data = res.json()
        assert data["status"] == "HEALTHY"

@pytest.mark.asyncio
async def test_node_registration_and_telemetry_flow():
    await init_db()
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # 1. Register a node
        node_payload = {
            "id": "node-01",
            "name": "Bioreactor Alpha",
            "location": "Symbiosis Lab",
            "status": "ONLINE",
            "ip_address": "192.168.1.101",
            "firmware_version": "2.0.0-esp32"
        }
        res = await ac.post("/api/v1/nodes", json=node_payload)
        assert res.status_code == 200
        node = res.json()
        assert node["id"] == "node-01"

        # 2. Ingest real telemetry from node-01
        telemetry_payload = {
            "node_id": "node-01",
            "runtime_sec": 7200,
            "flow_rate": 28.5,
            "pm25_in": 45.0,
            "pm25_out": 9.0,
            "pm10_in": 75.0,
            "pm10_out": 15.0,
            "co2_in": 900.0,
            "co2_out": 600.0,
            "o2_out_pct": 21.6,
            "ph": 7.8,
            "water_temp": 24.2,
            "turbidity_ntu": 280.0,
            "dissolved_oxygen": 7.5,
            "light_lux": 1500.0,
            "pump_speed_pct": 60,
            "led_assist_on": True
        }
        res_tel = await ac.post("/api/v1/telemetry/ingest", json=telemetry_payload)
        assert res_tel.status_code == 200
        data = res_tel.json()
        assert data["status"] == "ok"
        # Verify PM efficiency: ((45 - 9) / 45) * 100 = 80.0%
        assert data["efficiency_pm"] == 80.0
        # Verify CO2 efficiency: ((900 - 600) / 900) * 100 = 33.33%
        assert data["efficiency_co2"] == 33.33

        # 3. Retrieve latest telemetry
        res_latest = await ac.get("/api/v1/telemetry/latest/node-01")
        assert res_latest.status_code == 200
        latest = res_latest.json()
        assert latest["node_id"] == "node-01"
        assert latest["pm_efficiency"] == 80.0
        assert latest["ph"] == 7.8

        # 4. Dispatch actuation control
        res_ctrl = await ac.post("/api/v1/actuation/node-01/control", json={
            "pump_speed_pct": 75,
            "led_assist_on": True
        })
        assert res_ctrl.status_code == 200
        ctrl = res_ctrl.json()
        assert ctrl["current_state"]["pump_speed_pct"] == 75
        assert ctrl["current_state"]["led_assist_on"] is True
