# AeroAlga BDPA-v2 Hardware Schematics & Pinout Specification

## 1. Power Distribution Network
* **Primary System Supply**: 12V DC, 3A Switching Power Supply.
* **5V Regulated Rail**: Buck converter (12V $\rightarrow$ 5V DC @ 2.5A) powering:
  * MH-Z19B NDIR sensors (requires 5V $\pm$ 0.1V, peak 150mA heaters).
  * PMS5003 laser particulate sensors (5V fan & laser diode).
  * 12V to PWM Driver for Fluidic Pump.
* **3.3V System Rail**: ESP32 onboard LDO regulator powering:
  * ESP32 microcontroller core and WiFi radio.
  * BH1750 Ambient light sensor (I2C 3.3V).
  * DS18B20 digital temperature probe (3.3V with 4.7kΩ pull-up resistor).
  * Analog buffer amplifiers.

---

## 2. ESP32 Pin Allocation Table

| Peripheral | Sensor / Signal | ESP32 GPIO | Protocol / Mode | Voltage Level | Notes |
|---|---|---|---|---|---|
| **Inlet PM** | PMS5003 RX | GPIO 17 | UART 1 TX | 3.3V logic | Connect to PMS Pin 5 (RX) |
| **Inlet PM** | PMS5003 TX | GPIO 16 | UART 1 RX | 3.3V logic | Connect to PMS Pin 4 (TX) |
| **Outlet PM** | PMS5003 RX | GPIO 26 | SoftSerial TX | 3.3V logic | Software serial output |
| **Outlet PM** | PMS5003 TX | GPIO 25 | SoftSerial RX | 3.3V logic | Software serial input |
| **Inlet CO₂** | MH-Z19B TX | GPIO 32 | UART 2 RX | 3.3V logic | MH-Z19 Pin 2 (TX) |
| **Inlet CO₂** | MH-Z19B RX | GPIO 33 | UART 2 TX | 3.3V logic | MH-Z19 Pin 3 (RX) |
| **Outlet CO₂** | MH-Z19B TX | GPIO 18 | SoftSerial RX | 3.3V logic | Post-bioreactor CO₂ |
| **Outlet CO₂** | MH-Z19B RX | GPIO 19 | SoftSerial TX | 3.3V logic | Command bytes transmission |
| **Culture Temp**| DS18B20 Data | GPIO 4 | OneWire | 3.3V | Requires 4.7kΩ pullup to 3.3V |
| **Culture pH** | Analog Electrode | GPIO 34 | ADC1_CH6 | 0 – 3.3V | Op-Amp conditioning circuit |
| **Biomass Proxy**| Turbidity Probe | GPIO 35 | ADC1_CH7 | 0 – 3.3V | Phototransistor scatter voltage |
| **Dissolved O₂** | DO Electrode | GPIO 36 | ADC1_CH0 | 0 – 3.3V | Galvanic/Optical DO module |
| **Airflow** | Flow Meter Hall | GPIO 27 | Pulse Counter | 3.3V (divided) | Frequency interrupt (7.5 Hz = 1 LPM) |
| **Illumination**| BH1750 SDA | GPIO 21 | I2C Data | 3.3V | Address `0x23` |
| **Illumination**| BH1750 SCL | GPIO 22 | I2C Clock | 3.3V | 400 kHz fast mode |
| **Pump Actuator**| MOSFET / ESC | GPIO 14 | LEDC PWM (CH0) | 3.3V PWM (5kHz)| Duty cycle 10% – 100% |
| **Photonic Assist**| LED Driver Gate | GPIO 13 | LEDC PWM (CH1) | 3.3V PWM (5kHz)| Photosynthesis boost array |

---

## 3. Signal Conditioning & Level Shifting
1. **MH-Z19B & PMS5003 Logic Compatibility**:
   * While MH-Z19B is powered by 5V, its UART pins operate safely at 3.3V logic levels.
   * If using 5V UART signals from older sensors, inline $1\text{k}\Omega$ resistors with $2\text{k}\Omega$ pulldowns provide a clean 3.3V step-down divider.
2. **pH Probe High-Impedance Interface**:
   * Glass pH electrodes have internal impedances $>10^8\,\Omega$.
   * An ultra-low input bias current JFET or CMOS operational amplifier (e.g., TL081 or CA3140) provides unity-gain impedance buffering with a precision $2.5\text{V}$ voltage reference offset to translate $-414\text{mV} \dots +414\text{mV}$ into the positive ADC range.
3. **Turbidity Calibration Standard**:
   * Clean deionized water ($0\text{ NTU}$) outputs approximately $4.1\text{V}$ from the raw sensor board, clamped down to $3.3\text{V}$ before ESP32 ADC1.
