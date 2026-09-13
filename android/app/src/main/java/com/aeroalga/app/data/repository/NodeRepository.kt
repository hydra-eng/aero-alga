package com.aeroalga.app.data.repository

import com.aeroalga.app.data.model.ControlCommand
import com.aeroalga.app.data.model.NodeDevice
import com.aeroalga.app.data.model.TelemetryData
import com.aeroalga.app.data.remote.ApiService
import com.aeroalga.app.data.remote.TelemetryWebSocketClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NodeRepository(
    var backendBaseUrl: String = "http://10.0.2.2:8000/" // Default Android Emulator host mapping
) {
    private var apiService: ApiService
    private var wsClient: TelemetryWebSocketClient

    private val _nodes = MutableStateFlow<List<NodeDevice>>(emptyList())
    val nodes: StateFlow<List<NodeDevice>> = _nodes.asStateFlow()

    private val _activeNodeId = MutableStateFlow("node-01")
    val activeNodeId: StateFlow<String> = _activeNodeId.asStateFlow()

    private val _latestTelemetry = MutableStateFlow(TelemetryData())
    val latestTelemetry: StateFlow<TelemetryData> = _latestTelemetry.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(backendBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        wsClient = TelemetryWebSocketClient(backendBaseUrl)

        // Observe incoming WebSocket messages
        scope.launch {
            wsClient.telemetryFlow.collect { telemetry ->
                _latestTelemetry.value = telemetry
            }
        }
        scope.launch {
            wsClient.connectionState.collect { connected ->
                _isConnected.value = connected
            }
        }

        refreshNodes()
        switchNode(_activeNodeId.value)
    }

    fun updateBaseUrl(newUrl: String) {
        backendBaseUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        wsClient.disconnect()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(backendBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)
        wsClient = TelemetryWebSocketClient(backendBaseUrl)

        scope.launch {
            wsClient.telemetryFlow.collect { telemetry -> _latestTelemetry.value = telemetry }
        }
        scope.launch {
            wsClient.connectionState.collect { connected -> _isConnected.value = connected }
        }
        switchNode(_activeNodeId.value)
        refreshNodes()
    }

    fun switchNode(nodeId: String) {
        _activeNodeId.value = nodeId
        wsClient.connect(nodeId)
        scope.launch {
            try {
                val res = apiService.getLatestTelemetry(nodeId)
                if (res.isSuccessful && res.body() != null) {
                    _latestTelemetry.value = res.body()!!
                }
            } catch (_: Exception) {}
        }
    }

    fun refreshNodes() {
        scope.launch {
            try {
                val response = apiService.getNodes()
                if (response.isSuccessful && response.body() != null) {
                    _nodes.value = response.body()!!
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun sendControl(nodeId: String, pumpSpeed: Int?, ledAssist: Boolean?): Boolean {
        return try {
            val res = apiService.sendControlCommand(nodeId, ControlCommand(pumpSpeed, ledAssist))
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchHistory(nodeId: String, hours: Int = 24): List<TelemetryData> {
        return try {
            val res = apiService.getTelemetryHistory(nodeId, hours)
            if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
