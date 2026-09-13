<div align="center">

# 🌿 AeroAlga BDPA-v2
### Bio-Digital Photobioreactor Architecture & Urban Carbon Fixation System
**Real-Time Autonomous Air Purification · Microalgal Gaseous Exchange · Dot Matrix UI · Edge Telemetry**

[![Android CI](https://github.com/hydra-eng/aero-alga/actions/workflows/android_ci.yml/badge.svg)](https://github.com/hydra-eng/aero-alga/actions/workflows/android_ci.yml)
[![Backend CI](https://github.com/hydra-eng/aero-alga/actions/workflows/backend_ci.yml/badge.svg)](https://github.com/hydra-eng/aero-alga/actions/workflows/backend_ci.yml)
[![Platform](https://img.shields.io/badge/Hardware-ESP32%20FreeRTOS-32CD32?style=flat-square&logo=espressif)](https://www.espressif.com/)
[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-FastAPI%20%2B%20WebSockets-009688?style=flat-square&logo=fastapi)](https://fastapi.tiangolo.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

<br/>

![AeroAlga Dot Matrix UI](docs/screenshots/web_dashboard.png)

</div>

---

## 📖 Executive Summary

**AeroAlga BDPA-v2** is a cybernetic bioreactive air purification and carbon sequestration appliance engineered for high-density metropolitan spaces. By coupling fluidic venturi scrubbing columns with dense cultures of microalgae (*Spirulina platensis* / *Chlorella vulgaris*), AeroAlga continuously extracts particulate matter ($\text{PM}_{2.5}$ / $\text{PM}_{10}$) and converts ambient carbon dioxide ($\text{CO}_2$) into oxygen and biomass via photosynthetic fixation.

The system integrates an **ESP32 FreeRTOS edge firmware node**, a high-throughput **FastAPI / WebSocket orchestration backend**, an authentic **Dot-Matrix Web Telemetry Portal**, and a native **Kotlin Jetpack Compose Android application**.

---

## 🖥️ User Interface: Authentic Dot-Matrix Design

The system introduces an industrial, high-tech **Dot-Matrix UI** (inspired by retro-futuristic laboratory instrumentation, Teenage Engineering, and modern dot-matrix hardware):
- **5×7 LED Typography**: Digits and operational metrics are rendered through discrete phosphor LED dots with bloom glow halos (`#A6FF4D` lime, `#3FE8D4` cyan, `#FFB454` amber) on unlit recessed chassis apertures (`#132219`).
- **32-Pip Discrete Radial Gauges**: Sequential circular LED pips dynamically display particulate filtration ($\eta_{\text{PM}}$) and carbon fixation ($\eta_{\text{CO}_2}$) efficiencies.
- **Segmented LED Homeostasis Bars**: Color-coded discrete LED bargraphs indicate pH, water temperature, biomass turbidity (NTU), and dissolved oxygen with safe-zone highlighting.
- **Hardware Marquee Ticker**: Real-time scrolling LED status banner displaying live node mesh status and telemetry ingestion.

<div align="center">

![Dot Matrix Full Dashboard](docs/screenshots/web_dashboard_full.png)

</div>

---

## 📐 System Architecture & Flowcharts

### 1. End-to-End System Topology

```mermaid
flowchart TB
    subgraph HARDWARE["Physical Photobioreactor Node (ESP32)"]
        direction TB
        subgraph SENSORS["Multi-Sensor Array"]
            PMS_IN["Dual PMS5003 (Inlet PM)"]
            PMS_OUT["Dual PMS5003 (Outlet PM)"]
            CO2_IN["Dual MH-Z19B (Inlet CO₂)"]
            CO2_OUT["Dual MH-Z19B (Outlet CO₂)"]
            DS18B20["DS18B20 Temp Probe"]
            PH_PROBE["Analog pH Electrode"]
            TURB_PROBE["Turbidity Probe (NTU)"]
            DO_PROBE["Dissolved O₂ Electrode"]
            FLOW_METER["Airflow Hall Sensor"]
        end

        subgraph ACTUATORS["5kHz PWM Actuation"]
            PUMP["Fluidic Circulation Pump (10-100%)"]
            PAR_LED["Photonic LED Assist (660nm/450nm)"]
        end

        ESP["ESP32 Dual-Core Xtensa MCU\n(FreeRTOS Telemetry Loop)"]
        
        SENSORS --> ESP
        ESP --> ACTUATORS
    end

    subgraph BACKEND["AeroAlga Core Orchestration Hub"]
        direction TB
        FASTAPI["FastAPI App (Async ASGI)"]
        MQTT_BROKER["MQTT Ingestion Bridge\naeroalga/nodes/{id}/telemetry"]
        WS_STREAM["WebSocket Telemetry Streamer\n/api/v1/ws/stream/{nodeId}"]
        YIELD_ENG["Yield & Carbon Credit Engine\n(1.83 kg CO₂ / kg biomass)"]
        ANOMALY["Real-Time Anomaly Guard\n(Hypoxia, pH Drift, Thermal Overheat)"]
        DB[(SQLite / TimeSeries Database)]

        MQTT_BROKER --> FASTAPI
        FASTAPI --> YIELD_ENG
        FASTAPI --> ANOMALY
        FASTAPI --> DB
        FASTAPI --> WS_STREAM
    end

    subgraph CLIENTS["Multi-Platform Client Interface"]
        direction TB
        ANDROID["Native Android Application\n(Kotlin + Jetpack Compose Dot-Matrix)"]
        WEB["Web Telemetry Portal\n(Dot-Matrix Canvas + Live WS Link)"]
    end

    ESP -- "WiFi / MQTT JSON (2s ticks)" --> MQTT_BROKER
    WS_STREAM -- "Real-Time 2s WebSocket Broadcast" --> ANDROID
    WS_STREAM -- "Real-Time 2s WebSocket Broadcast" --> WEB
    ANDROID -- "Actuation Overrides (REST/MQTT)" --> FASTAPI
    WEB -- "Actuation Overrides (REST/MQTT)" --> FASTAPI
    FASTAPI -- "Control Packets: aeroalga/nodes/{id}/control" --> ESP
```

---

### 2. Closed-Loop Biological Filtration & Gas Exchange Flowchart

```mermaid
flowchart LR
    A["Contaminated Urban Air\n(High PM2.5, PM10, CO₂)"] --> B["Inlet Manifold &\nParticulate Ionizer"]
    B --> C["Laser Optical Counter 1\n(PMS5003 Inlet Reading)"]
    C --> D["NDIR Infrared Chamber 1\n(MH-Z19B Inlet Reading)"]
    
    D --> E["Fluidic Venturi Bubbling Column\n(Liquid Algal Matrix)"]
    
    subgraph BIOREACTOR["Culture Homeostasis Core"]
        E --> F["Microalgae Suspension\n(Spirulina Platensis)"]
        F --> G["Photonic LED Array\n(PAR 660nm / 450nm Assist)"]
        G --> H["Electrochemical Monitoring\n(pH, Temp, NTU, DO)"]
        H --> I["Fluidic Recirculation Pump\n(10% - 100% PWM Duty)"]
        I --> E
    end
    
    F --> J["Secondary Demister\n& Droplet Trap"]
    J --> K["Laser Optical Counter 2\n(PMS5003 Outlet Reading)"]
    K --> L["NDIR Infrared Chamber 2\n(MH-Z19B Outlet Reading)"]
    L --> M["Purified, Oxygenated Air\n(Low PM, Low CO₂, High O₂)"]
```

---

### 3. Edge Signal Processing & Closed-Loop Actuation Logic

```mermaid
stateDiagram-v2
    [*] --> SensorAcquisition: 2-Second Tick

    state SensorAcquisition {
        [*] --> ReadUART: PMS5003 & MH-Z19B
        [*] --> ReadADC: pH, Turbidity, DO
        [*] --> ReadOneWire: DS18B20 Temp
        [*] --> ReadPulse: Hall Flow Rate
    }

    SensorAcquisition --> EdgeCompute: Raw Quantization

    state EdgeCompute {
        [*] --> CalcEfficiency: η_PM & η_CO2
        CalcEfficiency --> BiologicalBoundsCheck: Homeostasis Limits
    }

    state BiologicalBoundsCheck {
        pH_Check: pH in [7.2, 8.5]
        Temp_Check: Temp in [20°C, 28°C]
        DO_Check: DO ≥ 6.0 mg/L
    }

    BiologicalBoundsCheck --> NominalOperation: Within Thresholds
    BiologicalBoundsCheck --> AdaptiveCompensation: Out-of-Bounds Drift

    state AdaptiveCompensation {
        BoostPump: Increase Fluid Circulation
        EngageLED: Enable 660nm Photonic Assist
        TriggerAlert: Broadcast Warning Frame
    }

    NominalOperation --> TransmitTelemetry
    AdaptiveCompensation --> TransmitTelemetry

    state TransmitTelemetry {
        FormatJSON: Pack TelemetryFrame
        PublishMQTT: aeroalga/nodes/{id}/telemetry
        BroadcastWS: WebSocket Stream 2s
    }

    TransmitTelemetry --> SensorAcquisition: Wait Next Interval
```

---

### 4. Mathematical Modeling & Carbon Credit Formulation

$$\eta_{\text{PM}} = \left( \frac{\text{PM}_{2.5}^{\text{in}} - \text{PM}_{2.5}^{\text{out}}}{\text{PM}_{2.5}^{\text{in}}} \right) \times 100\%$$

$$\eta_{\text{CO}_2} = \left( \frac{\text{CO}_2^{\text{in}} - \text{CO}_2^{\text{out}}}{\text{CO}_2^{\text{in}}} \right) \times 100\%$$

$$\Delta V = Q(t) \cdot \Delta t \quad \left[ \text{Treated Air Volume in Liters} \right]$$

$$\Delta M_{\text{biomass}} = \left( \frac{\Delta \text{CO}_2 \cdot Q(t)}{V_m} \right) \cdot \mu_{\text{bio}} \cdot \Delta t$$

$$\text{CO}_2\text{ Sequestered (kg)} = M_{\text{biomass}}\text{ (kg)} \times 1.83\,\frac{\text{kg CO}_2}{\text{kg dry algae}}$$

$$\text{Carbon Credits Accrued } (t\text{CO}_2\text{e}) = \frac{\text{CO}_2\text{ Sequestered (kg)}}{1000}$$

---

### 5. Android Reactive State Flow & Architecture

```mermaid
graph TD
    subgraph REMOTE_DATA["Remote Ingestion Layer"]
        OKHTTP["OkHttp3 WebSocket Client\n(Auto-reconnect with Heartbeats)"]
        RETROFIT["Retrofit 2 REST Client\n(Nodes Fleet & Actuation Commands)"]
    end

    subgraph REPOSITORY["Data & Domain Layer"]
        REPO["NodeRepository\n(Singleton State Manager)"]
        STATE_TELEMETRY["StateFlow&lt;TelemetryData&gt;"]
        STATE_NODES["StateFlow&lt;List&lt;NodeDevice&gt;&gt;"]
        STATE_CONN["StateFlow&lt;Boolean&gt; (Connection Status)"]

        OKHTTP --> REPO
        RETROFIT --> REPO
        REPO --> STATE_TELEMETRY
        REPO --> STATE_NODES
        REPO --> STATE_CONN
    end

    subgraph UI_LAYER["Jetpack Compose UI (Dot-Matrix System)"]
        DASHBOARD["DashboardScreen"]
        ANALYTICS["AnalyticsScreen"]
        NODES_FLEET["NodesScreen"]
        SETTINGS["SettingsScreen"]

        STATE_TELEMETRY --> DASHBOARD
        STATE_TELEMETRY --> ANALYTICS
        STATE_NODES --> NODES_FLEET
        STATE_CONN --> DASHBOARD

        subgraph COMPONENTS["Custom Dot-Matrix Composables"]
            DM_DISP["DotMatrixDisplay\n(5×7 Canvas LED font)"]
            DM_GAUGE["DotMatrixGauge\n(32-pip radial LED ring)"]
            DM_BAR["DotMatrixBar\n(Segmented LED meter)"]
            DM_FLOW["AnimatedFlowTrack\n(LED conduit cluster)"]
        end

        DASHBOARD --> DM_DISP
        DASHBOARD --> DM_GAUGE
        DASHBOARD --> DM_BAR
        DASHBOARD --> DM_FLOW
        ANALYTICS --> DM_DISP
        ANALYTICS --> DM_BAR
    end
```

---

## ⚡ ESP32 Hardware Pin Allocation & Schematics

| Peripheral | Sensor / Signal | ESP32 GPIO | Protocol / Mode | Voltage Level | Notes |
|---|---|---|---|---|---|
| **Inlet PM** | PMS5003 RX | `GPIO 17` | UART 1 TX | 3.3V Logic | Connects to PMS Pin 5 |
| **Inlet PM** | PMS5003 TX | `GPIO 16` | UART 1 RX | 3.3V Logic | Connects to PMS Pin 4 |
| **Outlet PM** | PMS5003 RX | `GPIO 26` | SoftSerial TX | 3.3V Logic | Downstream scrubbed air |
| **Outlet PM** | PMS5003 TX | `GPIO 25` | SoftSerial RX | 3.3V Logic | Downstream scrubbed air |
| **Inlet CO₂** | MH-Z19B TX | `GPIO 32` | UART 2 RX | 3.3V Logic | Ambient reference concentration |
| **Inlet CO₂** | MH-Z19B RX | `GPIO 33` | UART 2 TX | 3.3V Logic | Command byte polling (9600 baud) |
| **Outlet CO₂** | MH-Z19B TX | `GPIO 18` | SoftSerial RX | 3.3V Logic | Post-fixation stream |
| **Outlet CO₂** | MH-Z19B RX | `GPIO 19` | SoftSerial TX | 3.3V Logic | Post-fixation stream |
| **Water Temp**| DS18B20 Data | `GPIO 4` | OneWire | 3.3V | Requires 4.7kΩ pull-up |
| **Culture pH** | Analog Electrode | `GPIO 34` | ADC1_CH6 | 0 – 3.3V | High-impedance JFET buffer |
| **Turbidity** | Optical NTU Probe | `GPIO 35` | ADC1_CH7 | 0 – 3.3V | Biomass concentration proxy |
| **Dissolved O₂**| Galvanic DO Probe| `GPIO 36` | ADC1_CH0 | 0 – 3.3V | Oxygen saturation monitoring |
| **Airflow Rate**| Hall Pulse Counter| `GPIO 27` | Pulse Interrupt | 3.3V (divided) | Frequency conversion: 7.5 Hz = 1 LPM |
| **Circulation**| Fluidic Pump Gate | `GPIO 14` | LEDC PWM (CH0) | 3.3V PWM (5kHz)| 10% (idle) to 100% (max scrub) |
| **Photonic Assist**| LED Driver Gate | `GPIO 13` | LEDC PWM (CH1) | 3.3V PWM (5kHz)| PAR 660nm / 450nm lighting assist |

---

## 🚀 Quickstart & Deployment

### 1. Backend Orchestration Hub (`backend/`)

```bash
cd backend
python -m venv venv
source venv/bin/activate  # On Windows: .\venv\Scripts\activate
pip install -r requirements.txt

# Run automated tests
pytest

# Start FastAPI server on port 8000
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

* Swagger API Documentation: `http://localhost:8000/docs`
* Live WebSocket Endpoint: `ws://localhost:8000/api/v1/ws/stream/Node-01`

### 2. Native Android Application (`android/`)

The application is built with modern **Kotlin + Jetpack Compose** and includes its own portable Gradle wrapper.

```bash
cd android

# Build the Debug APK
./gradlew assembleDebug

# Output APK path:
# android/app/build/outputs/apk/debug/app-debug.apk
```

To install directly onto a connected physical Android device or emulator:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Web Telemetry Portal (`web/`)

The web portal is a single-page standalone application featuring the Dot-Matrix UI. It automatically binds to the live backend WebSocket when hosted or falls back to realistic edge simulation:

```bash
cd web
python -m http.server 8080
# Open http://localhost:8080 in any modern browser
```

### 4. Hardware Firmware (`firmware/`)

Built with **PlatformIO** targeting the Espressif ESP32 platform:

```bash
cd firmware
pio run --target upload
pio device monitor --speed 115200
```

---

## 📡 REST & WebSocket API Specification

### 1. Ingest Telemetry Frame (`POST /api/v1/telemetry/ingest`)
```json
{
  "node_id": "Node-01",
  "timestamp": 1780350266,
  "metrics": {
    "pm25_inlet": 42.8,
    "pm25_outlet": 7.7,
    "pm10_inlet": 69.2,
    "pm10_outlet": 12.4,
    "co2_inlet": 874.0,
    "co2_outlet": 576.0,
    "ph": 7.64,
    "water_temp": 23.8,
    "turbidity_ntu": 278.0,
    "dissolved_oxygen": 7.6,
    "flow_rate_lpm": 28.4,
    "pump_speed_pct": 55,
    "led_assist_on": false
  }
}
```

### 2. Actuation Control (`POST /api/v1/nodes/{nodeId}/control`)
```json
{
  "pump_speed_pct": 75,
  "led_assist_on": true
}
```

### 3. WebSocket Real-Time Stream (`GET /api/v1/ws/stream/{nodeId}`)
Delivers 2-second synchronized telemetry ticks to mobile apps and dashboards without HTTP polling overhead.

---

## 📂 Repository File Structure

```
aero-alga/
├── .github/
│   └── workflows/
│       ├── android_ci.yml         # GitHub Actions: Android Compose APK build
│       └── backend_ci.yml         # GitHub Actions: Python 3.11 linting & pytest
├── android/
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/aeroalga/app/
│   │   │   │   ├── data/          # Remote REST/WebSocket client & repository
│   │   │   │   └── ui/
│   │   │   │       ├── components/# DotMatrixDisplay, DotMatrixGauge, DotMatrixBar
│   │   │   │       ├── screens/   # Dashboard, Analytics, Nodes, Settings
│   │   │   │       └── theme/     # Botanical Dark & Dot-Matrix Palette
│   │   │   └── res/               # Vector icons, mipmaps, network security
│   │   └── build.gradle.kts
│   ├── gradle/wrapper/            # Portable Gradle 9.2.0 wrapper
│   ├── gradlew & gradlew.bat
│   └── settings.gradle.kts
├── backend/
│   ├── app/
│   │   ├── api/v1/                # Nodes, Telemetry, Actuation & WebSocket routes
│   │   ├── models/                # SQLAlchemy & Pydantic data schemas
│   │   ├── services/              # Yield & Anomaly alert engines
│   │   └── main.py                # FastAPI lifecycle application
│   ├── tests/                     # Automated Pytest suite
│   ├── requirements.txt
│   └── Dockerfile
├── firmware/
│   ├── include/                   # Config, sensor abstractions & edge calc
│   ├── src/                       # ESP32 FreeRTOS loop, UART & OneWire drivers
│   └── platformio.ini             # PlatformIO ESP32 configuration
├── web/
│   └── index.html                 # Standalone Dot-Matrix Web Telemetry Portal
├── docs/
│   └── screenshots/               # Full-fidelity UI screenshots
├── README.md                      # Comprehensive project documentation
└── .gitignore
```

---

## 👥 Authors & Acknowledgments

* **Hydra Engineering**: Core Architecture, Firmware Drivers, Jetpack Compose UI, and Backend Ingestion Hub.
* Developed for **Smart India Hackathon (SIH)** · Urban Air Quality Monitoring & Carbon Fixation Initiative.
