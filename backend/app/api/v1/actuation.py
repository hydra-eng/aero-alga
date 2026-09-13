from fastapi import APIRouter, HTTPException
from app.models.schemas import ActuationCommand
from app.mqtt.client import mqtt_service
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

# In-memory actuation state cache per node
node_actuation_state: dict[str, dict] = {}

@router.post("/{node_id}/control")
async def send_actuation_command(node_id: str, command: ActuationCommand):
    state = node_actuation_state.get(node_id, {"pump_speed_pct": 55, "led_assist_on": False})

    if command.pump_speed_pct is not None:
        state["pump_speed_pct"] = command.pump_speed_pct
    if command.led_assist_on is not None:
        state["led_assist_on"] = command.led_assist_on

    node_actuation_state[node_id] = state

    # Publish actuation command via MQTT to physical ESP32 node
    published = await mqtt_service.publish_control(node_id, state)

    return {
        "status": "command_dispatched",
        "node_id": node_id,
        "current_state": state,
        "mqtt_dispatched": published
    }

@router.get("/{node_id}/state")
async def get_actuation_state(node_id: str):
    state = node_actuation_state.get(node_id, {"pump_speed_pct": 55, "led_assist_on": False})
    return {"node_id": node_id, "state": state}
