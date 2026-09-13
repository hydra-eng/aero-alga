from app.models.schemas import TelemetryPayload, AnomalyAlert

def check_anomalies(payload: TelemetryPayload) -> list[AnomalyAlert]:
    alerts: list[AnomalyAlert] = []

    # pH safe zone: 7.2 - 8.5
    if payload.ph < 7.2:
        alerts.append(AnomalyAlert(
            parameter="pH",
            value=payload.ph,
            safe_min=7.2,
            safe_max=8.5,
            severity="HIGH",
            message=f"Acidic drift detected (pH {payload.ph:.2f}). Safe range is 7.2–8.5."
        ))
    elif payload.ph > 8.5:
        alerts.append(AnomalyAlert(
            parameter="pH",
            value=payload.ph,
            safe_min=7.2,
            safe_max=8.5,
            severity="HIGH",
            message=f"Alkaline spike detected (pH {payload.ph:.2f}). Risk of algal cell lysis."
        ))

    # Water Temp safe zone: 20°C - 28°C
    if payload.water_temp < 20.0 or payload.water_temp > 28.0:
        alerts.append(AnomalyAlert(
            parameter="water_temp",
            value=payload.water_temp,
            safe_min=20.0,
            safe_max=28.0,
            severity="MEDIUM",
            message=f"Bioreactor temperature out of optimal growth zone ({payload.water_temp:.1f}°C)."
        ))

    # Dissolved Oxygen safe zone: 6.0 - 10.0 mg/L
    if payload.dissolved_oxygen < 5.0:
        alerts.append(AnomalyAlert(
            parameter="dissolved_oxygen",
            value=payload.dissolved_oxygen,
            safe_min=6.0,
            safe_max=10.0,
            severity="HIGH",
            message=f"Hypoxic culture state detected (DO {payload.dissolved_oxygen:.1f} mg/L)."
        ))

    return alerts
