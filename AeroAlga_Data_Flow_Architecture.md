# AeroAlga BDPA-v2 — Telemetry Data Flow Architecture

**From Sensor to Screen: Hardware-to-Software Signal Path**

---

## 1. Overview

This document maps how a physical measurement — light scattered off a particulate, infrared absorbed by CO₂, a millivolt signal off a pH electrode — becomes a number on the operator's dashboard. The path has five layers: **Sensing → Acquisition → Processing → Storage/Transport → Presentation**. Each sensor from the instrumentation spec is placed at its correct interface (UART, I2C, OneWire, or Analog/ADC) rather than treated as a generic "input," since that interface choice is what actually constrains ESP32 firmware design.

---

## 2. Flowchart

```mermaid
flowchart TB

    subgraph L1["LAYER 1 · PHYSICAL SENSING"]
        direction TB

        subgraph AQ_IN["Air Quality — Inlet"]
            PM_IN["PMS5003 / SDS011\nPM2.5 · PM10\n(light scattering)"]
            CO2_IN["MH-Z19B\nCO2 ppm\n(NDIR)"]
        end

        subgraph AQ_OUT["Air Quality — Outlet"]
            PM_OUT["PMS5003 / SDS011\nPM2.5 · PM10\n(light scattering)"]
            CO2_OUT["MH-Z19B\nCO2 ppm\n(NDIR)"]
            O2_OUT["O2 Sensor *optional*\n% O2\n(photosynthesis output)"]
        end

        subgraph CULTURE["Algae Culture Health"]
            PH["pH Electrode\n0–14 scale\n(ion-potential + signal conditioning)"]
            TEMP["DS18B20\nWater Temp °C\n(waterproof probe)"]
            DO["DO Probe\nmg/L or % sat\n(electrochemical / optical)"]
            NTU["Turbidity Sensor\nNTU\n(biomass proxy)"]
        end

        subgraph ENG["Engineering / Environmental"]
            FLOW["Airflow Sensor\nL/min\n(residence time calc)"]
            LUX["BH1750\nLight Intensity, lux\n(photosynthesis input)"]
        end
    end

    subgraph L2["LAYER 2 · DATA ACQUISITION — ESP32"]
        direction TB
        UART_BUS["UART Bus\n(PM + CO2 sensors)"]
        ANALOG_BUS["ADC Channels\n(pH, DO, Turbidity, Flow)"]
        ONEWIRE_BUS["OneWire Bus\n(DS18B20)"]
        I2C_BUS["I2C Bus\n(BH1750, optional O2)"]
        MCU["ESP32 Core\nSampling · Timestamping · Buffering"]

        UART_BUS --> MCU
        ANALOG_BUS --> MCU
        ONEWIRE_BUS --> MCU
        I2C_BUS --> MCU
    end

    subgraph L3["LAYER 3 · EDGE PROCESSING"]
        direction TB
        CALC["Performance Engine\nη_PM = (PMin−PMout)/PMin × 100\nη_CO2 = (CO2in−CO2out)/CO2in × 100"]
        VALIDATE["Range / Fault Validation\n(sensor drift, out-of-band flags)"]
        YIELD["Yield Model\nTreated Volume · Biomass Δ · CO2 Sequestered"]
        MCU --> CALC --> VALIDATE --> YIELD
    end

    subgraph L4["LAYER 4 · STORAGE & TRANSPORT"]
        direction TB
        SD["Local SD Card\nRaw + computed log (CSV/JSON)"]
        WIFI["WiFi Radio\nESP32 → Network"]
        MQTT["MQTT / WebSocket Broker\nTopic: node01/telemetry"]
        API["Backend API\nAuth · Aggregation · History"]
        DB["Time-Series Database\n(session stats, 24h trend)"]

        YIELD --> SD
        YIELD --> WIFI --> MQTT --> API --> DB
    end

    subgraph L5["LAYER 5 · PRESENTATION"]
        direction TB
        WS["Live WebSocket Feed\n(2s telemetry tick)"]
        DASH["Mobile Dashboard\nInlet/Outlet Cards · Efficiency Rings\nBio-Homeostasis Grid"]
        ANALYSIS["Analysis View\nHealth Index · Alerts · Trend Direction"]
        HIST["24h Trend Chart\n(DB-backed history)"]

        API --> WS --> DASH
        DASH --> ANALYSIS
        DB --> HIST --> DASH
    end

    L1 --> L2
```

---

## 3. Interface Reference Table

| # | Sensor | Signal Type | ESP32 Interface | Layer |
|---|---|---|---|---|
| 1 | PMS5003 / SDS011 (Inlet) | Digital, framed serial | UART | Sensing → Acquisition |
| 2 | PMS5003 / SDS011 (Outlet) | Digital, framed serial | UART | Sensing → Acquisition |
| 3 | MH-Z19B (Inlet) | Digital (UART) or PWM | UART | Sensing → Acquisition |
| 4 | MH-Z19B (Outlet) | Digital (UART) or PWM | UART | Sensing → Acquisition |
| 5 | O2 Sensor *(optional)* | Digital / I2C, board-dependent | I2C or UART | Sensing → Acquisition |
| 6 | pH Electrode | Analog millivolt | ADC (with signal-conditioning circuit) | Sensing → Acquisition |
| 7 | DS18B20 | Digital, 1-wire | OneWire | Sensing → Acquisition |
| 8 | DO Probe | Analog or digital, sensor-dependent | ADC / UART | Sensing → Acquisition |
| 9 | Turbidity Sensor | Analog voltage | ADC | Sensing → Acquisition |
| 10 | Airflow Sensor / Flow Meter | Analog or pulse output | ADC / Pulse counter (GPIO interrupt) | Sensing → Acquisition |
| 11 | BH1750 Light Sensor | Digital | I2C | Sensing → Acquisition |

---

## 4. Layer Notes

- **Layer 1 — Physical Sensing.** Inlet and outlet PM/CO2 pairs are kept as separate subgraphs deliberately: the efficiency formulas only mean something if the two readings are unambiguously tied to a position (before/after the reactor), not just "two sensors of the same type."
- **Layer 2 — Acquisition.** Grouping by bus (UART / ADC / OneWire / I2C) rather than by sensor name reflects how ESP32 firmware is actually structured — shared bus sensors (BH1750 + optional O2 on I2C) need address arbitration; UART sensors need multiplexing or multiple hardware UARTs.
- **Layer 3 — Edge Processing.** Runs on-device before anything leaves the node. This is where η_PM and η_CO2 are computed from raw inlet/outlet values, and where out-of-range validation catches a failed or drifted sensor before it corrupts a yield calculation downstream.
- **Layer 4 — Storage & Transport.** SD logging is the resilience path (works with no network); the WiFi → MQTT/WebSocket → API path is what feeds a live dashboard. Both branch off the same validated data, not off raw sensor output.
- **Layer 5 — Presentation.** Two consumption modes: the live 2-second feed for real-time cards, and the database-backed history for 24-hour trend and session statistics — this is the same distinction flagged earlier about not conflating live jitter with slow biological trend data.

---

*Prepared for AeroAlga BDPA-v2 — Node-01 instrumentation review.*
