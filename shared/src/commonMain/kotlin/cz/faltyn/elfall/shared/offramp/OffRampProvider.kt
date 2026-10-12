package cz.faltyn.elfall.shared.offramp

/**
 * Card cash-out ("Výběr na kartu" / Withdraw to card).
 *
 * Withdrawals from the user's wallet go ONLY to payment cards in CZK, EUR or USD.
 * Card details and KYC never touch ELFALL — they stay inside a licensed provider's
 * hosted widget (MoonPay Sell, Transak Off-Ramp, Ramp Off-Ramp stubs).
 *
 * BTC send to another crypto address is a Transfer, not a Withdrawal (see TransferService).
 */
enum class FiatCurrency(val code: String, val symbol: String, val decimals: Int) {
    CZK("CZK", "Kč", 2),
    EUR("EUR", "€", 2),
    USD("USD", "$", 2);

    companion object {
        fun fromCode(code: String): FiatCurrency? =
            entries.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

        /** Only these three currencies are allowed for card cash-out. */
        val supported: List<FiatCurrency> = entries.toList()
    }
}

enum class OffRampProviderId(val displayName: String) {
    MOONPAY("MoonPay Sell"),
    TRANSAK("Transak Off-Ramp"),
    RAMP("Ramp Network"),
}

data class OffRampQuoteRequest(
    val amountCrypto: Double,
    val cryptoAsset: String = "BTC",
    val currency: FiatCurrency,
    /** Public receive address of the wallet that will fund the sell (never a private key). */
    val walletAddress: String,
)

data class OffRampQuote(
    val provider: OffRampProviderId,
    val currency: FiatCurrency,
    val amountCrypto: Double,
    val cryptoAsset: String,
    /** Gross fiat before provider fees, in minor units (haléře / cents). */
    val fiatGrossMinor: Long,
    val feeMinor: Long,
    val fiatNetMinor: Long,
    val rate: Double,
    val expiresAtEpochMs: Long,
) {
    init {
        require(currency in FiatCurrency.supported) { "Unsupported cash-out currency: ${currency.code}" }
        require(amountCrypto > 0) { "Amount must be positive" }
        require(fiatNetMinor >= 0) { "Net fiat cannot be negative" }
    }
}

data class HostedFlow(
    val provider: OffRampProviderId,
    /** HTTPS URL of the provider's hosted KYC + card widget. Open in Custom Tab / browser. */
    val url: String,
)

interface OffRampProvider {
    val id: OffRampProviderId
    suspend fun quote(request: OffRampQuoteRequest): OffRampQuote
    /** Builds the hosted-widget URL. Card PAN / CVV / KYC never enter our process. */
    fun buildHostedFlowUrl(request: OffRampQuoteRequest, quote: OffRampQuote): HostedFlow
}

/** Shared validation used by stubs and unit tests. */
object OffRampValidation {
    const val MIN_BTC = 0.0001
    const val MAX_BTC = 10.0

    fun validateRequest(request: OffRampQuoteRequest) {
        require(request.currency in FiatCurrency.supported) {
            "Cash-out currency must be CZK, EUR or USD (got ${request.currency.code})"
        }
        require(request.amountCrypto in MIN_BTC..MAX_BTC) {
            "Amount must be between $MIN_BTC and $MAX_BTC ${request.cryptoAsset}"
        }
        require(request.walletAddress.isNotBlank()) { "Wallet address required" }
        require(request.cryptoAsset.uppercase() == "BTC") {
            "Only BTC cash-out is wired in this stub (got ${request.cryptoAsset})"
        }
    }

    fun formatFiat(minor: Long, currency: FiatCurrency): String {
        val major = minor / 100.0
        return "%s %.2f".format(currency.code, major)
    }
}
