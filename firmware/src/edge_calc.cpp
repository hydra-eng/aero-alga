#include "edge_calc.h"
#include <Arduino.h>

void computeEdgeMetrics(SensorTelemetry &telemetry, float deltaSec) {
    // 1. Particulate removal efficiency: η_PM = ((PM_in - PM_out) / PM_in) * 100
    if (telemetry.pm25_in > 0.0f) {
        float raw_pm_eff = ((telemetry.pm25_in - telemetry.pm25_out) / telemetry.pm25_in) * 100.0f;
        telemetry.pm_efficiency = constrain(raw_pm_eff, 0.0f, 100.0f);
    } else {
        telemetry.pm_efficiency = 0.0f;
    }

    // 2. CO2 reduction efficiency: η_CO2 = ((CO2_in - CO2_out) / CO2_in) * 100
    if (telemetry.co2_in > 0.0f) {
        float raw_co2_eff = ((telemetry.co2_in - telemetry.co2_out) / telemetry.co2_in) * 100.0f;
        telemetry.co2_efficiency = constrain(raw_co2_eff, 0.0f, 100.0f);
    } else {
        telemetry.co2_efficiency = 0.0f;
    }

    // 3. Cumulative volume: L = flow_rate (L/min) * (deltaSec / 60)
    telemetry.cumulative_volume_l += telemetry.flow_rate_lpm * (deltaSec / 60.0f);

    // 4. Biomass accumulation model:
    // Estimated through optical density / turbidity calibration
    telemetry.biomass_accum_kg = max(0.1f, (telemetry.turbidity_ntu / 40.0f) * 0.9f);

    // 5. CO2 Sequestered: 1 kg dry biomass ≈ 1.83 kg CO2
    telemetry.co2_sequestered_kg = telemetry.biomass_accum_kg * 1.83f;

    // 6. Carbon credits: 1 credit ≈ 1 tonne CO2e
    telemetry.carbon_credits_t = telemetry.co2_sequestered_kg / 1000.0f;
}
