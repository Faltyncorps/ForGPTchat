package cz.faltyn.eleanor.shared

import fr.acinq.bitcoin.Chain

/** Supported networks. TESTNET is the default everywhere; MAINNET is opt-in via the `mainnet` build flavor. */
enum class BtcNetwork(val chain: Chain, val esploraUrl: String, val coinType: Int) {
    TESTNET(Chain.Testnet3, "https://mempool.space/testnet/api", 1),
    SIGNET(Chain.Signet, "https://mempool.space/signet/api", 1),
    MAINNET(Chain.Mainnet, "https://mempool.space/api", 0);

    val chainHash get() = chain.chainHash

    companion object {
        val DEFAULT = TESTNET
        fun fromName(name: String?): BtcNetwork = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
