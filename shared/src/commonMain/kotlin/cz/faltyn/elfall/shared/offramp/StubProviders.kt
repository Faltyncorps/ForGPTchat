package cz.faltyn.elfall.shared.offramp

/**
 * Hosted-widget stubs. No real API keys — read placeholders from [OffRampConfig].
 * Quotes use rough public spot rates for UI preview only; the provider's widget is authoritative.
 */
data class OffRampConfig(
    val moonPayPublishableKey: String = "pk_test_REPLACE_ME",
    val transakApiKey: String = "REPLACE_ME",
    val rampApiKey: String = "REPLACE_ME",
    /** Rough BTC→fiat mid rates for stub quotes (update via Track B /prices later). */
    val btcCzk: Double = 2_000_000.0,
    val btcEur: Double = 80_000.0,
    val btcUsd: Double = 85_000.0,
    val feeBps: Int = 150, // 1.5%
) {
    fun rate(currency: FiatCurrency): Double = when (currency) {
        FiatCurrency.CZK -> btcCzk
        FiatCurrency.EUR -> btcEur
        FiatCurrency.USD -> btcUsd
    }
}

abstract class HostedOffRampStub(
    override val id: OffRampProviderId,
    protected val config: OffRampConfig,
) : OffRampProvider {

    override suspend fun quote(request: OffRampQuoteRequest): OffRampQuote {
        OffRampValidation.validateRequest(request)
        val rate = config.rate(request.currency)
        val grossMinor = (request.amountCrypto * rate * 100).toLong()
        val feeMinor = grossMinor * config.feeBps / 10_000
        return OffRampQuote(
            provider = id,
            currency = request.currency,
            amountCrypto = request.amountCrypto,
            cryptoAsset = request.cryptoAsset.uppercase(),
            fiatGrossMinor = grossMinor,
            feeMinor = feeMinor,
            fiatNetMinor = grossMinor - feeMinor,
            rate = rate,
            expiresAtEpochMs = 0L, // filled by UI clock if needed
        )
    }
}

class MoonPaySellStub(config: OffRampConfig = OffRampConfig()) : HostedOffRampStub(OffRampProviderId.MOONPAY, config) {
    override fun buildHostedFlowUrl(request: OffRampQuoteRequest, quote: OffRampQuote): HostedFlow {
        OffRampValidation.validateRequest(request)
        // Docs: https://dev.moonpay.com/docs/off-ramp-browse-wallet
        val url = buildString {
            append("https://sell.moonpay.com?")
            append("apiKey=${config.moonPayPublishableKey}")
            append("&baseCurrencyCode=btc")
            append("&baseCurrencyAmount=${request.amountCrypto}")
            append("&quoteCurrencyCode=${request.currency.code.lowercase()}")
            append("&refundWalletAddress=${request.walletAddress}")
        }
        return HostedFlow(id, url)
    }
}

class TransakOffRampStub(config: OffRampConfig = OffRampConfig()) : HostedOffRampStub(OffRampProviderId.TRANSAK, config) {
    override fun buildHostedFlowUrl(request: OffRampQuoteRequest, quote: OffRampQuote): HostedFlow {
        OffRampValidation.validateRequest(request)
        // Docs: https://docs.transak.com/
        val url = buildString {
            append("https://global.transak.com?")
            append("apiKey=${config.transakApiKey}")
            append("&productsAvailed=SELL")
            append("&cryptoCurrencyCode=BTC")
            append("&network=bitcoin")
            append("&walletAddress=${request.walletAddress}")
            append("&fiatCurrency=${request.currency.code}")
            append("&cryptoAmount=${request.amountCrypto}")
        }
        return HostedFlow(id, url)
    }
}

class RampOffRampStub(config: OffRampConfig = OffRampConfig()) : HostedOffRampStub(OffRampProviderId.RAMP, config) {
    override fun buildHostedFlowUrl(request: OffRampQuoteRequest, quote: OffRampQuote): HostedFlow {
        OffRampValidation.validateRequest(request)
        // Docs: https://docs.ramp.network/
        val url = buildString {
            append("https://app.ramp.network?")
            append("hostApiKey=${config.rampApiKey}")
            append("&swapAsset=BTC_BTC")
            append("&swapAmount=${(request.amountCrypto * 100_000_000).toLong()}") // sats
            append("&userAddress=${request.walletAddress}")
            append("&fiatCurrency=${request.currency.code}")
            append("&enabledFlows=OFFRAMP")
        }
        return HostedFlow(id, url)
    }
}

/** Default catalogue of stubbed licensed providers. */
fun defaultOffRampProviders(config: OffRampConfig = OffRampConfig()): List<OffRampProvider> = listOf(
    MoonPaySellStub(config),
    TransakOffRampStub(config),
    RampOffRampStub(config),
)
