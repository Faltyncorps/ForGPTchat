package cz.faltyn.elfall.shared.stellar

/**
 * Vlastní (owner) Stellar peněženka projektu ELFALL – Freighter, "Account 2" vlastníka Ivo Faltýna.
 *
 * POUZE veřejná G-adresa (bezpečné commitnout). Tajný klíč / seed se do repozitáře NIKDY nedává;
 * podepisování probíhá výhradně ve Freighteru (SEP-7 / WalletConnect / @stellar/freighter-api).
 * Kontext projektu: Stellar testnet (viz integrity/critical.json → stellar.network).
 */
object StellarOwnerWallet {
    /** Veřejná adresa vlastní peněženky projektu (příjem / výplaty). */
    const val OWNER_PUBLIC_ADDRESS: String = "GCGDAOAUBYMOOEYPJH44CIDLH3A4CJVB6M2AXWIHKQ3BC5UWQ7OL22UT"

    /** Peněženka, ve které je účet spravován. */
    val wallet: StellarWalletId = StellarWalletId.FREIGHTER

    /** SEP-7 platební požadavek s cílem = vlastní peněženka projektu. */
    fun payToOwner(
        amount: String? = null,
        assetCode: String? = null,
        assetIssuer: String? = null,
        memo: String? = null,
    ): Sep7.PayRequest = Sep7.PayRequest(
        destination = OWNER_PUBLIC_ADDRESS,
        amount = amount,
        assetCode = assetCode,
        assetIssuer = assetIssuer,
        memo = memo,
        memoType = if (memo != null) "MEMO_TEXT" else null,
    )
}
