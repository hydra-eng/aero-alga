import json
import logging
import asyncio
from app.config import settings

logger = logging.getLogger(__name__)

class MQTTService:
    def __init__(self):
        self.client = None
        self.is_connected = False

    def start(self):
        try:
            import paho.mqtt.client as mqtt

            # Support paho-mqtt v2 callback API
            try:
                self.client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id="aeroalga_backend_hub")
            except Exception:
                self.client = mqtt.Client(client_id="aeroalga_backend_hub")

            self.client.on_connect = self._on_connect
            self.client.on_message = self._on_message
            self.client.on_disconnect = self._on_disconnect

            # Connect non-blocking
            self.client.connect_async(settings.MQTT_BROKER_HOST, settings.MQTT_BROKER_PORT, 60)
            self.client.loop_start()
            logger.info(f"MQTT service initialized for broker at {settings.MQTT_BROKER_HOST}:{settings.MQTT_BROKER_PORT}")
        except Exception as e:
            logger.warning(f"MQTT broker connection unavailable ({e}). Running in HTTP/WebSocket direct mode.")
            self.client = None

    def _on_connect(self, client, userdata, flags, rc, properties=None):
        if rc == 0:
            self.is_connected = True
            logger.info("Connected to MQTT broker successfully")
            client.subscribe(settings.MQTT_TELEMETRY_TOPIC)
        else:
            logger.warning(f"Failed to connect to MQTT broker, return code {rc}")

    def _on_disconnect(self, client, userdata, rc, properties=None):
        self.is_connected = False
        logger.info("Disconnected from MQTT broker")

    def _on_message(self, client, userdata, msg):
        try:
            topic = msg.topic
            payload_str = msg.payload.decode("utf-8")
            data = json.loads(payload_str)
            logger.info(f"Received MQTT telemetry from {topic}")
            # Note: ingested data can be processed into async loop or DB
        except Exception as e:
            logger.error(f"Error parsing MQTT message: {e}")

    async def publish_control(self, node_id: str, command: dict) -> bool:
        if self.client and self.is_connected:
            topic = settings.MQTT_CONTROL_TOPIC.format(node_id=node_id)
            payload = json.dumps(command)
            info = self.client.publish(topic, payload, qos=1)
            info.wait_for_publish()
            logger.info(f"Dispatched MQTT control to {topic}: {payload}")
            return True
        return False

    def stop(self):
        if self.client:
            self.client.loop_stop()
            self.client.disconnect()

mqtt_service = MQTTService()
