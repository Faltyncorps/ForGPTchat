package cz.faltyn.eleanor.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.faltyn.eleanor.shared.AddressCheck
import cz.faltyn.eleanor.shared.AddressValidator
import cz.faltyn.eleanor.shared.BtcNetwork
import cz.faltyn.eleanor.shared.EsploraClient
import cz.faltyn.eleanor.shared.FeePreview
import cz.faltyn.eleanor.shared.WalletCore
import cz.faltyn.eleanor.shared.WithdrawService
import cz.faltyn.eleanor.shared.parseBtcToSats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val network: BtcNetwork = BtcNetwork.DEFAULT,
    val hasWallet: Boolean = false,
    val newMnemonic: List<String>? = null, // shown exactly once after creation for paper backup
    val address: String? = null,
    val balanceSats: Long? = null,
    val payoutAddress: String = "",
    val amountInput: String = "",
    val addressError: String? = null,
    val preview: FeePreview? = null,
    val lastTxid: String? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

class WalletViewModel(app: Application) : AndroidViewModel(app) {
    private val store = KeystoreSeedStore(app)
    private val network = BtcNetwork.fromName(BuildConfig.BTC_NETWORK)
    private val esplora = EsploraClient(network.esploraUrl)
    private var wallet: WalletCore? = null

    private val _state = MutableStateFlow(UiState(network = network))
    val state: StateFlow<UiState> = _state

    init {
        store.loadMnemonic()?.let { open(it) }
        _state.update { it.copy(payoutAddress = store.loadPayoutAddress().orEmpty()) }
    }

    private fun open(words: List<String>) {
        val w = WalletCore.fromMnemonic(words, network)
        wallet = w
        _state.update { it.copy(hasWallet = true, address = w.receiveAddress()) }
        refresh()
    }

    fun createWallet() {
        val words = WalletCore.newMnemonic()
        store.saveMnemonic(words)
        open(words)
        _state.update { it.copy(newMnemonic = words) }
    }

    fun restoreWallet(input: String) = runCatching {
        val words = input.trim().lowercase().split(Regex("\\s+"))
        WalletCore.fromMnemonic(words, network)
        store.saveMnemonic(words)
        open(words)
    }.onFailure { e -> _state.update { it.copy(error = "Invalid recovery phrase: ${e.message}") } }

    fun backupConfirmed() = _state.update { it.copy(newMnemonic = null) }

    fun refresh() {
        val addr = wallet?.receiveAddress() ?: return
        viewModelScope.launch {
            runCatching { esplora.balanceSats(addr) }
                .onSuccess { b -> _state.update { it.copy(balanceSats = b, error = null) } }
                .onFailure { e -> _state.update { it.copy(error = "Balance: ${e.message}") } }
        }
    }

    fun onPayoutAddressChange(v: String) {
        val err = if (v.isBlank()) null else (AddressValidator.check(v, network) as? AddressCheck.Invalid)?.reason
        _state.update { it.copy(payoutAddress = v, addressError = err, preview = null) }
    }

    fun savePayoutAddress() {
        val a = _state.value.payoutAddress.trim()
        if (AddressValidator.isValid(a, network)) store.savePayoutAddress(a)
    }

    fun onAmountChange(v: String) = _state.update { it.copy(amountInput = v, preview = null) }

    fun previewFee() {
        val w = wallet ?: return
        val s = _state.value
        val sats = parseBtcToSats(s.amountInput) ?: run { _state.update { it.copy(error = "Invalid amount") }; return }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            runCatching { WithdrawService(w, esplora).preview(s.payoutAddress, sats) }
                .onSuccess { p -> _state.update { it.copy(preview = p, busy = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, busy = false) } }
        }
    }

    /** Must only be called after BiometricGate succeeded. */
    fun sendConfirmed() {
        val w = wallet ?: return
        val p = _state.value.preview ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            runCatching { WithdrawService(w, esplora).send(p) }
                .onSuccess { txid -> _state.update { it.copy(lastTxid = txid, preview = null, busy = false) }; refresh() }
                .onFailure { e -> _state.update { it.copy(error = e.message, busy = false) } }
        }
    }
}
