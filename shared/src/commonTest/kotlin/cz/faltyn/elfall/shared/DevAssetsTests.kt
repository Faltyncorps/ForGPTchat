package cz.faltyn.elfall.shared

import cz.faltyn.elfall.shared.assets.DevAssets
import cz.faltyn.elfall.shared.assets.FLC_REFERENCE_USD
import cz.faltyn.elfall.shared.network.ElfallNetwork
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DevAssetsTests {
    @Test fun fiveChainsPlusFlc() {
        val ids = DevAssets.catalogue.map { it.id }
        assertTrue(ids.containsAll(listOf("tBTC", "XLM", "sepETH", "devSOL", "amoyMATIC", "FLC", "yFLC")))
        assertEquals(9.33, FLC_REFERENCE_USD)
    }

    @Test fun defaultNetworkIsTestnet() {
        assertEquals(ElfallNetwork.TESTNET, ElfallNetwork.DEFAULT)
        assertTrue(ElfallNetwork.TESTNET.isTestnet)
        assertTrue(!ElfallNetwork.MAINNET.isTestnet)
    }
}
