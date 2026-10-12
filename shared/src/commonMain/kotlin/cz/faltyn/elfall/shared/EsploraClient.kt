package cz.faltyn.elfall.shared

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Utxo(val txid: String, val vout: Long, val value: Long, val status: UtxoStatus = UtxoStatus())

@Serializable
data class UtxoStatus(val confirmed: Boolean = false, @SerialName("block_height") val blockHeight: Long? = null)

@Serializable
data class FeeRecommendation(
    val fastestFee: Long,
    val halfHourFee: Long,
    val hourFee: Long,
    val economyFee: Long = 1,
    val minimumFee: Long = 1,
)

/** Minimal, key-free client for the public Esplora / mempool.space REST API. */
class EsploraClient(
    private val baseUrl: String,
    private val http: HttpClient = defaultHttpClient(),
) {
    internal val base get() = baseUrl
    internal val httpClient get() = http

    suspend fun utxos(address: String): List<Utxo> = http.get("$baseUrl/address/$address/utxo").body()

    suspend fun balanceSats(address: String): Long = utxos(address).sumOf { it.value }

    suspend fun recommendedFees(): FeeRecommendation =
        http.get("$baseUrl/v1/fees/recommended").body()

    /** Broadcasts a raw transaction hex; returns the txid. */
    suspend fun broadcast(txHex: String): String {
        val resp = http.post("$baseUrl/tx") { setBody(txHex) }
        val text = resp.bodyAsText()
        if (!resp.status.isSuccess()) error("Broadcast failed: $text")
        return text.trim()
    }

    companion object {
        fun defaultHttpClient() = HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }
}
