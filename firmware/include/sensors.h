#pragma once

#include <Arduino.h>

struct SensorTelemetry {
    // Air Quality
    float pm25_in;
    float pm25_out;
    float pm10_in;
    float pm10_out;
    float co2_in;
    float co2_out;
    float o2_out;

    // Culture Health
    float ph;
    float water_temp;
    float turbidity_ntu;
    float dissolved_oxygen;

    // Engineering
    float flow_rate_lpm;
    float light_lux;
    uint32_t runtime_sec;

    // Edge Computed
    float pm_efficiency;
    float co2_efficiency;
    float cumulative_volume_l;
    float biomass_accum_kg;
    float co2_sequestered_kg;
    float carbon_credits_t;

    // Actuation state
    int pump_speed_pct;
    bool led_assist_on;
};

void initSensors();
void pollSensors(SensorTelemetry &telemetry);
void IRAM_ATTR flowPulseCounter();
