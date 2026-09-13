#pragma once

#include <Arduino.h>

// ================= DEVICE IDENTITY =================
#define NODE_ID "node-01"
#define NODE_NAME "AeroAlga BDPA-v2"
#define FIRMWARE_VERSION "2.0.0-esp32"

// ================= NETWORK CONFIGURATION =================
#define WIFI_SSID "AeroAlga_Net"
#define WIFI_PASSWORD "Bioreactor2026!"
#define MQTT_BROKER "192.168.1.100"
#define MQTT_PORT 1883
#define BACKEND_HTTP_URL "http://192.168.1.100:8000/api/v1/telemetry/ingest"

#define MQTT_TOPIC_TELEMETRY "aeroalga/nodes/" NODE_ID "/telemetry"
#define MQTT_TOPIC_CONTROL   "aeroalga/nodes/" NODE_ID "/control"

// ================= PIN ASSIGNMENTS =================
// UART PMS5003 Inlet (Hardware Serial 1)
#define PIN_PMS_IN_RX 16
#define PIN_PMS_IN_TX 17

// UART PMS5003 Outlet (Software Serial)
#define PIN_PMS_OUT_RX 25
#define PIN_PMS_OUT_TX 26

// UART MH-Z19B Inlet (Hardware Serial 2)
#define PIN_MHZ_IN_RX 32
#define PIN_MHZ_IN_TX 33

// UART MH-Z19B Outlet (Software Serial 2)
#define PIN_MHZ_OUT_RX 18
#define PIN_MHZ_OUT_TX 19

// OneWire Waterproof Temp (DS18B20)
#define PIN_ONEWIRE_TEMP 4

// ADC Channels (12-bit, 0 - 3.3V)
#define PIN_ADC_PH 34
#define PIN_ADC_TURBIDITY 35
#define PIN_ADC_DO 36

// Flow meter pulse interrupt
#define PIN_FLOW_SENSOR 27

// I2C (BH1750 Ambient Light)
#define PIN_I2C_SDA 21
#define PIN_I2C_SCL 22

// Actuator PWM Outputs
#define PIN_ACTUATOR_PUMP 14
#define PIN_ACTUATOR_LED 13

// Actuator PWM Channels
#define PWM_CHANNEL_PUMP 0
#define PWM_CHANNEL_LED 1
#define PWM_FREQ 5000
#define PWM_RESOLUTION 8

// ================= SAFE HOMEOSTASIS BOUNDS =================
#define SAFE_PH_MIN 7.2f
#define SAFE_PH_MAX 8.5f
#define SAFE_TEMP_MIN 20.0f
#define SAFE_TEMP_MAX 28.0f
#define SAFE_DO_MIN 6.0f
#define SAFE_DO_MAX 10.0f

// Telemetry interval (ms)
#define TELEMETRY_INTERVAL_MS 2000
