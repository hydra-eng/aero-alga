# AeroAlga BDPA-v2 🌿🔬

> **Bio-Digital Performance Architecture (BDPA-v2)**: An Algae-Based Dual-Culture Photobioreactor for Continuous Indoor Air Purification, Biological $\text{CO}_2$ Fixation, and Circular Bio-Valorization.
> 
> *Team Symbiosis (`GDT022`) · Smart India Hackathon 2026 (Problem Statement ID: `SIH26217`)*

---

## 🌟 Overview & Innovation

Conventional indoor air purifiers rely on mechanical filters (HEPA, activated carbon) that only trap coarse particles, generate non-biodegradable landfill waste, and do not address indoor carbon dioxide buildup.

**AeroAlga** introduces an active biological photobioreactor combining:
1. **Dual-Algal Ecosystem**:
   - **Free-Floating Microalgae (*Chlorella sp.*)**: Photosynthetically sequesters dissolved $\text{CO}_2$ and generates $\text{O}_2$.
   - **Filamentous Cyanobacteria Biofilm**: Secretes Extracellular Polymeric Substances (EPS) forming a sticky matrix that binds $\text{PM}_{2.5}$ and $\text{PM}_{10}$ particulates.
2. **Zig-Zag Bio-Interaction Geometry**:
   - Internal baffles angled at $100^\circ$–$105^\circ$ force rising air into serpentine contact with the algae culture, maximizing residence time.
3. **Circular Economy & Bio-Valorization**:
   - Harvested biomass yields biostimulants, algal fiber, and biofertilizer.
   - Verified $\text{CO}_2$ sequestration is convertible into carbon credit units ($1\text{ credit} \approx 1\text{ tonne } \text{CO}_2\text{e}$).

---

## 🏗️ System Architecture

```
                      ┌──────────────────────────────────────────────┐
                      │          AeroAlga Hardware Node              │
                      │  (ESP32 + Dual PMS5003 + Dual MH-Z19B        │
                      │   + DS18B20 + pH + Turbidity + Flow + LED)   │
                      └──────────────────────┬───────────────────────┘
                                             │ MQTT / JSON Telemetry (WiFi)
                                             ▼
                      ┌──────────────────────────────────────────────┐
                      │          AeroAlga Core Backend               │
                      │   - FastAPI + WebSocket Server               │
                      │   - Embedded / External MQTT Broker Bridge   │
                      │   - SQLite / TimeSeries DB Engine            │
                      │   - Multi-Node Device Registry               │
                      └──────────────┬───────────────────────────────┘
                                     │
                     ┌───────────────┴───────────────┐
                     │ REST & Live WebSocket Feed    │
                     ▼                               ▼
       ┌───────────────────────────┐   ┌───────────────────────────┐
       │    Native Android App     │   │   Web Telemetry Portal    │
       │ (Jetpack Compose / Kotlin)│   │  (Optimized BDPA-v2 Web)  │
       └───────────────────────────┘   └───────────────────────────┘
```

---

## 📂 Repository Structure

* [`firmware/`](file:///c:/Users/Acer/Downloads/areo-alga/firmware) — Production ESP32 PlatformIO C++ firmware with physical sensor drivers, edge efficiency calculations, local SD fallback, and MQTT publishing.
* [`backend/`](file:///c:/Users/Acer/Downloads/areo-alga/backend) — FastAPI telemetry hub, async SQLite database, real-time WebSocket streaming, MQTT bridge, yield model, and anomaly detection.
* [`android/`](file:///c:/Users/Acer/Downloads/areo-alga/android) — Native Android Application in Kotlin & Jetpack Compose featuring botanical dark theme, real OkHttp WebSockets, multi-node fleet management, 24-hour charts, and actuation controls.
* [`web/`](file:///c:/Users/Acer/Downloads/areo-alga/web) — Modern, mobile-responsive web dashboard connecting to the live backend WebSocket stream.
* [`docs/`](file:///c:/Users/Acer/Downloads/areo-alga/docs) — Electrical schematics, pinout assignments, level-shifting, calibration procedures, and API specifications.
* [`.github/workflows/`](file:///c:/Users/Acer/Downloads/areo-alga/.github/workflows) — CI/CD pipelines for automated backend unit testing and Android build verification.

---

## 📊 Core Physical Calculations

$$\eta_{\text{PM}} = \frac{\text{PM}_{\text{in}} - \text{PM}_{\text{out}}}{\text{PM}_{\text{in}}} \times 100$$

$$\eta_{\text{CO}_2} = \frac{\text{CO}_{2,\text{in}} - \text{CO}_{2,\text{out}}}{\text{CO}_{2,\text{in}}} \times 100$$

$$\text{CO}_2 \text{ Sequestered (kg)} = \text{Biomass (kg)} \times 1.83$$

$$\text{Carbon Credits} = \frac{\text{CO}_2 \text{ Sequestered (kg)}}{1000}$$

---

## 🚀 Quick Start Guide

### 1. Start the Telemetry Backend
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
Interactive API Swagger Docs: `http://localhost:8000/docs`

### 2. Run Backend Test Suite
```bash
cd backend
pytest
```

### 3. Flash ESP32 Firmware
Using PlatformIO CLI or VSCode PlatformIO Extension:
```bash
cd firmware
pio run --target upload
```

### 4. Build & Run the Android App
Open the `android/` directory in Android Studio or build with Gradle:
```bash
cd android
./gradlew assembleDebug
```
Deploy the APK to your connected Android phone or emulator. Configure the server IP under **Settings** $\rightarrow$ **Backend Hub Host**.

---

## 👥 Authors & Acknowledgments
* **Team**: Symbiosis (`GDT022`)
* **Event**: Smart India Hackathon (SIH) 2026
* **Academic References**: Journal of Environmental Management (Desta & Dadheech, 2021), Frontiers in Plant Science (2026).
