package cz.faltyn.elfall.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cz.faltyn.elfall.shared.AddressCheck
import cz.faltyn.elfall.shared.AddressValidator
import cz.faltyn.elfall.shared.BtcNetwork
import cz.faltyn.elfall.shared.EsploraClient
import cz.faltyn.elfall.shared.TransferFeePreview
import cz.faltyn.elfall.shared.TransferService
import cz.faltyn.elfall.shared.WalletCore
import cz.faltyn.elfall.shared.offramp.FiatCurrency
import cz.faltyn.elfall.shared.offramp.HostedFlow
import cz.faltyn.elfall.shared.offramp.OffRampConfig
import cz.faltyn.elfall.shared.offramp.OffRampProvider
import cz.faltyn.elfall.shared.offramp.OffRampQuote
import cz.faltyn.elfall.shared.offramp.OffRampQuoteRequest
import cz.faltyn.elfall.shared.offramp.defaultOffRampProviders
import cz.faltyn.elfall.shared.parseBtcToSats
import cz.faltyn.elfall.shared.swap.SwapHostedFlow
import cz.faltyn.elfall.shared.swap.SwapProvider
import cz.faltyn.elfall.shared.swap.SwapQuote
import cz.faltyn.elfall.shared.swap.SwapQuoteRequest
import cz.faltyn.elfall.shared.swap.defaultSwapProviders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppMode { RECEIVE, TRANSFER, SWAP, WITHDRAW_CARD }

data class UiState(
    val network: BtcNetwork = BtcNetwork.DEFAULT,
    val mode: AppMode = AppMode.RECEIVE,
    val hasWallet: Boolean = false,
    val newMnemonic: List<String>? = null,
    val address: String? = null,
    val balanceSats: Long? = null,
    // Transfer
    val transferAddress: String = "",
    val amountInput: String = "",
    val addressError: String? = null,
    val transferPreview: TransferFeePreview? = null,
    val lastTxid: String? = null,
    // Card cash-out
    val cashCurrency: FiatCurrency = FiatCurrency.CZK,
    val cashProviderIndex: Int = 0,
    val cashQuote: OffRampQuote? = null,
    val cashFlow: HostedFlow? = null,
    // Swap
    val swapFrom: String = "BTC",
    val swapTo: String = "ETH",
    val swapQuote: SwapQuote? = null,
    val swapFlow: SwapHostedFlow? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

class WalletViewModel(app: Application) : AndroidViewModel(app) {
    private val store = KeystoreSeedStore(app)
    private val network = BtcNetwork.fromName(BuildConfig.BTC_NETWORK)
    private val esplora = EsploraClient(network.esploraUrl)
    private var wallet: WalletCore? = null
    private val offRampProviders: List<OffRampProvider> = defaultOffRampProviders(OffRampConfig())
    private val swapProviders: List<SwapProvider> = defaultSwapProviders()

    private val _state = MutableStateFlow(UiState(network = network))
    val state: StateFlow<UiState> = _state

    val offRampProviderNames get() = offRampProviders.map { it.id.displayName }

    init {
        store.loadMnemonic()?.let { open(it) }
        store.loadPayoutAddress()?.let { addr -> _state.update { it.copy(transferAddress = addr) } }
    }

    private fun open(words: List<String>) {
        val w = WalletCore.fromMnemonic(words, network)
        wallet = w
        _state.update { it.copy(hasWallet = true, address = w.receiveAddress()) }
        refresh()
    }

    fun setMode(mode: AppMode) = _state.update { it.copy(mode = mode, error = null) }

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

    // --- Transfer (crypto address) ---
    fun onTransferAddressChange(v: String) {
        val err = if (v.isBlank()) null else (AddressValidator.check(v, network) as? AddressCheck.Invalid)?.reason
        _state.update { it.copy(transferAddress = v, addressError = err, transferPreview = null) }
    }

    fun saveTransferAddress() {
        val a = _state.value.transferAddress.trim()
        if (AddressValidator.isValid(a, network)) store.savePayoutAddress(a)
    }

    fun onAmountChange(v: String) = _state.update {
        it.copy(amountInput = v, transferPreview = null, cashQuote = null, cashFlow = null, swapQuote = null)
    }

    fun previewTransfer() {
        val w = wallet ?: return
        val s = _state.value
        val sats = parseBtcToSats(s.amountInput) ?: run { _state.update { it.copy(error = "Invalid amount") }; return }
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            runCatching { TransferService(w, esplora).preview(s.transferAddress, sats) }
                .onSuccess { p -> _state.update { it.copy(transferPreview = p, busy = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, busy = false) } }
        }
    }

    fun sendTransferConfirmed() {
        val w = wallet ?: return
        val p = _state.value.transferPreview ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            runCatching { TransferService(w, esplora).send(p) }
                .onSuccess { txid -> _state.update { it.copy(lastTxid = txid, transferPreview = null, busy = false) }; refresh() }
                .onFailure { e -> _state.update { it.copy(error = e.message, busy = false) } }
        }
    }

    // --- Card cash-out ---
    fun setCashCurrency(c: FiatCurrency) = _state.update { it.copy(cashCurrency = c, cashQuote = null, cashFlow = null) }
    fun setCashProvider(index: Int) = _state.update { it.copy(cashProviderIndex = index, cashQuote = null, cashFlow = null) }

    fun quoteCashOut() {
        val addr = wallet?.receiveAddress() ?: return
        val s = _state.value
        val amount = s.amountInput.replace(',', '.').toDoubleOrNull()
            ?: run { _state.update { it.copy(error = "Invalid amount") }; return }
        val provider = offRampProviders.getOrNull(s.cashProviderIndex) ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            runCatching {
                val req = OffRampQuoteRequest(amount, "BTC", s.cashCurrency, addr)
                val q = provider.quote(req)
                q to provider.buildHostedFlowUrl(req, q)
            }.onSuccess { (q, flow) ->
                _state.update { it.copy(cashQuote = q, cashFlow = flow, busy = false) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, busy = false) }
            }
        }
    }

    // --- Swap ---
    fun setSwapAssets(from: String, to: String) = _state.update { it.copy(swapFrom = from, swapTo = to, swapQuote = null, swapFlow = null) }

    fun quoteSwap() {
        val addr = wallet?.receiveAddress() ?: return
        val s = _state.value
        val amount = s.amountInput.replace(',', '.').toDoubleOrNull()
            ?: run { _state.update { it.copy(error = "Invalid amount") }; return }
        val provider = swapProviders.first() // Changelly stub
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            runCatching {
                val req = SwapQuoteRequest(s.swapFrom, s.swapTo, amount, addr)
                val q = provider.quote(req)
                q to provider.buildHostedFlowUrl(req, q)
            }.onSuccess { (q, flow) ->
                _state.update { it.copy(swapQuote = q, swapFlow = flow, busy = false) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, busy = false) }
            }
        }
    }
}
