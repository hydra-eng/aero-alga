from app.models.schemas import TelemetryPayload

def calculate_efficiencies(payload: TelemetryPayload) -> TelemetryPayload:
    """
    Computes PM removal efficiency and CO2 reduction efficiency
    according to AeroAlga physics formulas:
    η_PM = ((PM_in - PM_out) / PM_in) * 100
    η_CO2 = ((CO2_in - CO2_out) / CO2_in) * 100
    """
    if payload.pm25_in > 0:
        raw_pm_eff = ((payload.pm25_in - payload.pm25_out) / payload.pm25_in) * 100.0
        payload.pm_efficiency = round(max(0.0, min(100.0, raw_pm_eff)), 2)
    else:
        payload.pm_efficiency = 0.0

    if payload.co2_in > 0:
        raw_co2_eff = ((payload.co2_in - payload.co2_out) / payload.co2_in) * 100.0
        payload.co2_efficiency = round(max(0.0, min(100.0, raw_co2_eff)), 2)
    else:
        payload.co2_efficiency = 0.0

    # Yield accumulation defaults if not supplied by edge
    if payload.cumulative_volume_l is None:
        # Default approximation based on runtime and flow rate
        payload.cumulative_volume_l = round((payload.runtime_sec / 60.0) * payload.flow_rate, 1)

    if payload.biomass_accum_kg is None:
        # Biomass correlates with turbidity and flow rate
        payload.biomass_accum_kg = round(max(0.1, (payload.turbidity_ntu / 40.0) * 0.9), 3)

    if payload.co2_sequestered_kg is None:
        # 1 kg dry biomass captures approx 1.83 kg CO2
        payload.co2_sequestered_kg = round(payload.biomass_accum_kg * 1.83, 3)

    if payload.carbon_credits_t is None:
        # 1 credit ≈ 1 tonne CO2e
        payload.carbon_credits_t = round(payload.co2_sequestered_kg / 1000.0, 5)

    return payload
