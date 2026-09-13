from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from typing import Dict, Set
import json
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

class ConnectionManager:
    def __init__(self):
        # Map node_id -> Set of WebSockets
        self.active_connections: Dict[str, Set[WebSocket]] = {}

    async def connect(self, node_id: str, websocket: WebSocket):
        await websocket.accept()
        if node_id not in self.active_connections:
            self.active_connections[node_id] = set()
        self.active_connections[node_id].add(websocket)
        logger.info(f"WebSocket client connected to node '{node_id}' (active: {len(self.active_connections[node_id])})")

    def disconnect(self, node_id: str, websocket: WebSocket):
        if node_id in self.active_connections:
            self.active_connections[node_id].discard(websocket)
            if not self.active_connections[node_id]:
                del self.active_connections[node_id]
        logger.info(f"WebSocket client disconnected from node '{node_id}'")

    async def broadcast_telemetry(self, node_id: str, data: dict):
        if node_id in self.active_connections:
            message = json.dumps(data)
            dead_sockets = set()
            for connection in self.active_connections[node_id]:
                try:
                    await connection.send_text(message)
                except Exception as e:
                    logger.warning(f"Error sending to client: {e}")
                    dead_sockets.add(connection)
            for dead in dead_sockets:
                self.disconnect(node_id, dead)

ws_manager = ConnectionManager()

@router.websocket("/stream/{node_id}")
async def websocket_telemetry_stream(websocket: WebSocket, node_id: str):
    await ws_manager.connect(node_id, websocket)
    try:
        while True:
            # Keep-alive / incoming client command handling
            data = await websocket.receive_text()
            # If client sends ping or command
            if data == "ping":
                await websocket.send_text(json.dumps({"type": "pong"}))
    except WebSocketDisconnect:
        ws_manager.disconnect(node_id, websocket)
    except Exception as e:
        logger.error(f"WebSocket error on node {node_id}: {e}")
        ws_manager.disconnect(node_id, websocket)
