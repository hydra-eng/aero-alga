#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "config.h"
#include "sensors.h"
#include "edge_calc.h"

// Prototypes from actuators
void initActuators();
void setPumpSpeed(int speedPct);
void setLedAssist(bool enable);

WiFiClient espClient;
PubSubClient mqttClient(espClient);

SensorTelemetry currentTelemetry = {0};
uint32_t lastTelemetryMillis = 0;
uint32_t bootMillis = 0;

void handleMqttCallback(char* topic, byte* payload, unsigned int length) {
    char message[256];
    if (length < 255) {
        memcpy(message, payload, length);
        message[length] = '\0';
    } else {
        return;
    }

    Serial.printf("[MQTT RX] %s: %s\n", topic, message);

    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, message);
    if (!error) {
        if (doc.containsKey("pump_speed_pct")) {
            int speed = doc["pump_speed_pct"];
            currentTelemetry.pump_speed_pct = speed;
            setPumpSpeed(speed);
            Serial.printf("Pump speed updated: %d%%\n", speed);
        }
        if (doc.containsKey("led_assist_on")) {
            bool led = doc["led_assist_on"];
            currentTelemetry.led_assist_on = led;
            setLedAssist(led);
            Serial.printf("LED assist updated: %s\n", led ? "ON" : "OFF");
        }
    }
}

void connectWiFi() {
    Serial.printf("Connecting to WiFi: %s", WIFI_SSID);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    int retries = 0;
    while (WiFi.status() != WL_CONNECTED && retries < 15) {
        delay(500);
        Serial.print(".");
        retries++;
    }
    if (WiFi.status() == WL_CONNECTED) {
        Serial.printf("\nWiFi connected! IP: %s\n", WiFi.localIP().toString().c_str());
    } else {
        Serial.println("\nWiFi connection failed! Running in offline / local SD mode.");
    }
}

void reconnectMqtt() {
    if (WiFi.status() != WL_CONNECTED) return;

    if (!mqttClient.connected()) {
        Serial.print("Attempting MQTT connection...");
        if (mqttClient.connect(NODE_ID)) {
            Serial.println("connected!");
            mqttClient.subscribe(MQTT_TOPIC_CONTROL);
        } else {
            Serial.printf("failed, rc=%d. Will retry next loop.\n", mqttClient.state());
        }
    }
}

void publishTelemetry() {
    StaticJsonDocument<512> doc;

    doc["node_id"] = NODE_ID;
    doc["runtime_sec"] = currentTelemetry.runtime_sec;
    doc["flow_rate"] = currentTelemetry.flow_rate_lpm;

    doc["pm25_in"] = currentTelemetry.pm25_in;
    doc["pm25_out"] = currentTelemetry.pm25_out;
    doc["pm10_in"] = currentTelemetry.pm10_in;
    doc["pm10_out"] = currentTelemetry.pm10_out;
    doc["pm_efficiency"] = currentTelemetry.pm_efficiency;

    doc["co2_in"] = currentTelemetry.co2_in;
    doc["co2_out"] = currentTelemetry.co2_out;
    doc["co2_efficiency"] = currentTelemetry.co2_efficiency;
    doc["o2_out_pct"] = currentTelemetry.o2_out;

    doc["ph"] = currentTelemetry.ph;
    doc["water_temp"] = currentTelemetry.water_temp;
    doc["turbidity_ntu"] = currentTelemetry.turbidity_ntu;
    doc["dissolved_oxygen"] = currentTelemetry.dissolved_oxygen;
    doc["light_lux"] = currentTelemetry.light_lux;

    doc["cumulative_volume_l"] = currentTelemetry.cumulative_volume_l;
    doc["biomass_accum_kg"] = currentTelemetry.biomass_accum_kg;
    doc["co2_sequestered_kg"] = currentTelemetry.co2_sequestered_kg;
    doc["carbon_credits_t"] = currentTelemetry.carbon_credits_t;

    doc["pump_speed_pct"] = currentTelemetry.pump_speed_pct;
    doc["led_assist_on"] = currentTelemetry.led_assist_on;

    char buffer[512];
    serializeJson(doc, buffer);

    if (mqttClient.connected()) {
        mqttClient.publish(MQTT_TOPIC_TELEMETRY, buffer);
    }
    Serial.printf("[TELEMETRY] %s\n", buffer);
}

void setup() {
    bootMillis = millis();
    initSensors();
    initActuators();

    currentTelemetry.pump_speed_pct = 55;
    currentTelemetry.led_assist_on = false;
    currentTelemetry.o2_out = 21.4f;

    connectWiFi();
    mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
    mqttClient.setCallback(handleMqttCallback);
}

void loop() {
    if (WiFi.status() == WL_CONNECTED) {
        if (!mqttClient.connected()) {
            reconnectMqtt();
        }
        mqttClient.loop();
    }

    uint32_t now = millis();
    if (now - lastTelemetryMillis >= TELEMETRY_INTERVAL_MS) {
        float deltaSec = (now - lastTelemetryMillis) / 1000.0f;
        lastTelemetryMillis = now;

        currentTelemetry.runtime_sec = (now - bootMillis) / 1000;
        pollSensors(currentTelemetry);
        computeEdgeMetrics(currentTelemetry, deltaSec);
        publishTelemetry();
    }
}
