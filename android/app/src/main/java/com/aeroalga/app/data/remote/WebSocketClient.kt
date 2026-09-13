package com.aeroalga.app.data.remote

import android.util.Log
import com.aeroalga.app.data.model.TelemetryData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class TelemetryWebSocketClient(
    private val baseUrl: String,
    private val gson: Gson = Gson()
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentNodeId: String? = null
    private var shouldReconnect = true

    private val _telemetryFlow = MutableSharedFlow<TelemetryData>(replay = 1)
    val telemetryFlow: SharedFlow<TelemetryData> = _telemetryFlow.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>(replay = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect(nodeId: String) {
        currentNodeId = nodeId
        shouldReconnect = true
        initiateConnection(nodeId)
    }

    private fun initiateConnection(nodeId: String) {
        val wsUrl = baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/api/v1/ws/stream/$nodeId"

        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i("TelemetryWS", "WebSocket connected to node $nodeId")
                scope.launch { _connectionState.emit(true) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val telemetry = gson.fromJson(text, TelemetryData::class.java)
                    scope.launch { _telemetryFlow.emit(telemetry) }
                } catch (e: Exception) {
                    Log.e("TelemetryWS", "Error parsing WebSocket payload: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("TelemetryWS", "WebSocket closed: $reason")
                scope.launch { _connectionState.emit(false) }
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("TelemetryWS", "WebSocket failure: ${t.message}")
                scope.launch { _connectionState.emit(false) }
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect || currentNodeId == null) return
        scope.launch {
            delay(3000)
            currentNodeId?.let { initiateConnection(it) }
        }
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }
}
