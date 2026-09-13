package com.aeroalga.app.data.model

import com.google.gson.annotations.SerializedName

data class NodeDevice(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("location") val location: String,
    @SerializedName("status") val status: String,
    @SerializedName("ip_address") val ipAddress: String,
    @SerializedName("firmware_version") val firmwareVersion: String,
    @SerializedName("last_seen") val lastSeen: String? = null
)

data class TelemetryData(
    @SerializedName("node_id") val nodeId: String = "node-01",
    @SerializedName("runtime_sec") val runtimeSec: Long = 0L,
    @SerializedName("flow_rate") val flowRate: Float = 0f,

    // PM Data
    @SerializedName("pm25_in") val pm25In: Float = 0f,
    @SerializedName("pm25_out") val pm25Out: Float = 0f,
    @SerializedName("pm10_in") val pm10In: Float = 0f,
    @SerializedName("pm10_out") val pm10Out: Float = 0f,
    @SerializedName("pm_efficiency") val pmEfficiency: Float = 0f,

    // CO2 Data
    @SerializedName("co2_in") val co2In: Float = 0f,
    @SerializedName("co2_out") val co2Out: Float = 0f,
    @SerializedName("co2_efficiency") val co2Efficiency: Float = 0f,
    @SerializedName("o2_out_pct") val o2OutPct: Float = 21.4f,

    // Biological Health
    @SerializedName("ph") val ph: Float = 7.6f,
    @SerializedName("water_temp") val waterTemp: Float = 23.5f,
    @SerializedName("turbidity_ntu") val turbidityNtu: Float = 250f,
    @SerializedName("dissolved_oxygen") val dissolvedOxygen: Float = 7.2f,
    @SerializedName("light_lux") val lightLux: Float = 1200f,

    // Yield
    @SerializedName("cumulative_volume_l") val cumulativeVolumeL: Float = 0f,
    @SerializedName("biomass_accum_kg") val biomassAccumKg: Float = 0f,
    @SerializedName("co2_sequestered_kg") val co2SequesteredKg: Float = 0f,
    @SerializedName("carbon_credits_t") val carbonCreditsT: Float = 0f,
    @SerializedName("n_removal_pct") val nRemovalPct: Float = 88f,
    @SerializedName("p_removal_pct") val pRemovalPct: Float = 86f,

    // Actuators
    @SerializedName("pump_speed_pct") val pumpSpeedPct: Int = 55,
    @SerializedName("led_assist_on") val ledAssistOn: Boolean = false,

    // Alerts
    @SerializedName("alerts") val alerts: List<TelemetryAlert> = emptyList(),
    @SerializedName("timestamp") val timestamp: String? = null
)

data class TelemetryAlert(
    @SerializedName("parameter") val parameter: String,
    @SerializedName("value") val value: Float,
    @SerializedName("safe_min") val safeMin: Float,
    @SerializedName("safe_max") val safeMax: Float,
    @SerializedName("severity") val severity: String,
    @SerializedName("message") val message: String
)

data class ControlCommand(
    @SerializedName("pump_speed_pct") val pumpSpeedPct: Int? = null,
    @SerializedName("led_assist_on") val ledAssistOn: Boolean? = null
)

data class ActuationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("node_id") val nodeId: String,
    @SerializedName("current_state") val currentState: Map<String, Any>
)
