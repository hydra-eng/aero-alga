# AeroAlga BDPA-v2 REST & WebSocket API Specification

Base URL: `http://{host}:8000/api/v1`

---

## 1. Node Fleet Endpoints

### `GET /nodes`
Returns all registered photobioreactor hardware nodes.
* **Response `200 OK`**:
```json
[
  {
    "id": "node-01",
    "name": "Bioreactor Alpha",
    "location": "Symbiosis Lab",
    "status": "ONLINE",
    "ip_address": "192.168.1.101",
    "firmware_version": "2.0.0-esp32",
    "last_seen": "2026-09-13T06:40:00Z"
  }
]
```

### `POST /nodes`
Registers or updates node metadata.
* **Request Body**:
```json
{
  "id": "node-02",
  "name": "Bioreactor Beta",
  "location": "Greenhouse 1",
  "status": "ONLINE",
  "ip_address": "192.168.1.102",
  "firmware_version": "2.0.0-esp32"
}
```

---

## 2. Telemetry Endpoints

### `POST /telemetry/ingest`
Ingests telemetry packet from physical ESP32 or gateway.
* **Request Body**:
```json
{
  "node_id": "node-01",
  "runtime_sec": 3600,
  "flow_rate": 28.5,
  "pm25_in": 42.0,
  "pm25_out": 7.5,
  "pm10_in": 68.0,
  "pm10_out": 12.0,
  "co2_in": 880.0,
  "co2_out": 560.0,
  "o2_out_pct": 21.6,
  "ph": 7.75,
  "water_temp": 24.1,
  "turbidity_ntu": 265.0,
  "dissolved_oxygen": 7.8,
  "light_lux": 1350.0,
  "pump_speed_pct": 55,
  "led_assist_on": false
}
```
* **Response `200 OK`**:
```json
{
  "status": "ok",
  "efficiency_pm": 82.14,
  "efficiency_co2": 36.36,
  "alerts": []
}
```

### `GET /telemetry/latest/{node_id}`
Returns the most recent metrics recorded for the specified node.

### `GET /telemetry/history/{node_id}?hours=24`
Returns time-series telemetry data points within the specified hour window.

---

## 3. WebSocket Real-Time Stream

### `WS /ws/stream/{node_id}`
Establishes a continuous bidirectional WebSocket connection. Every telemetry ingestion triggers an immediate broadcast:
```json
{
  "node_id": "node-01",
  "flow_rate": 28.5,
  "runtime_sec": 3600,
  "pm25_in": 42.0,
  "pm25_out": 7.5,
  "pm_efficiency": 82.14,
  "co2_in": 880.0,
  "co2_out": 560.0,
  "co2_efficiency": 36.36,
  "ph": 7.75,
  "water_temp": 24.1,
  "turbidity_ntu": 265.0,
  "dissolved_oxygen": 7.8,
  "cumulative_volume_l": 1710.0,
  "biomass_accum_kg": 5.96,
  "co2_sequestered_kg": 10.91,
  "carbon_credits_t": 0.01091,
  "pump_speed_pct": 55,
  "led_assist_on": false,
  "alerts": [],
  "timestamp": "2026-09-13T06:40:02Z"
}
```

---

## 4. Hardware Actuation Endpoints

### `POST /actuation/{node_id}/control`
Sends real control directives to the ESP32 node via MQTT.
* **Request Body**:
```json
{
  "pump_speed_pct": 70,
  "led_assist_on": true
}
```
* **Response `200 OK`**:
```json
{
  "status": "command_dispatched",
  "node_id": "node-01",
  "current_state": {
    "pump_speed_pct": 70,
    "led_assist_on": true
  },
  "mqtt_dispatched": true
}
```
