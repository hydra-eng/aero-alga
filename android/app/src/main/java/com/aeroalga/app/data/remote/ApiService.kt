package com.aeroalga.app.data.remote

import com.aeroalga.app.data.model.ActuationResponse
import com.aeroalga.app.data.model.ControlCommand
import com.aeroalga.app.data.model.NodeDevice
import com.aeroalga.app.data.model.TelemetryData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/v1/nodes")
    suspend fun getNodes(): Response<List<NodeDevice>>

    @GET("api/v1/nodes/{node_id}")
    suspend fun getNode(@Path("node_id") nodeId: String): Response<NodeDevice>

    @POST("api/v1/nodes")
    suspend fun registerNode(@Body node: NodeDevice): Response<NodeDevice>

    @GET("api/v1/telemetry/latest/{node_id}")
    suspend fun getLatestTelemetry(@Path("node_id") nodeId: String): Response<TelemetryData>

    @GET("api/v1/telemetry/history/{node_id}")
    suspend fun getTelemetryHistory(
        @Path("node_id") nodeId: String,
        @Query("hours") hours: Int = 24
    ): Response<List<TelemetryData>>

    @POST("api/v1/actuation/{node_id}/control")
    suspend fun sendControlCommand(
        @Path("node_id") nodeId: String,
        @Body command: ControlCommand
    ): Response<ActuationResponse>
}
