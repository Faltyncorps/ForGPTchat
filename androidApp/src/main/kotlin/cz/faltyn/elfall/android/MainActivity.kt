package cz.faltyn.elfall.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.faltyn.elfall.shared.offramp.FiatCurrency
import cz.faltyn.elfall.shared.offramp.OffRampValidation
import cz.faltyn.elfall.shared.satsToBtcString

class MainActivity : AppCompatActivity() {
    private val vm: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val s by vm.state.collectAsStateWithLifecycle()
                    ElfallScreen(s, vm, onConfirmTransfer = {
                        BiometricGate.confirm(
                            this,
                            getString(R.string.biometric_title),
                            getString(R.string.biometric_subtitle),
                        ) { ok, _ -> if (ok) vm.sendTransferConfirmed() }
                    })
                }
            }
        }
    }
}

@Composable
fun ElfallScreen(s: UiState, vm: WalletViewModel, onConfirmTransfer: () -> Unit) {
    val ctx = LocalContext.current
    fun openUrl(url: String) = ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.wallet_title), style = MaterialTheme.typography.headlineSmall)
        Text("Network: ${s.network.name}", style = MaterialTheme.typography.labelMedium)
        s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (!s.hasWallet) {
            var restore by remember { mutableStateOf("") }
            Button(onClick = vm::createWallet) { Text(stringResource(R.string.create_wallet)) }
            OutlinedTextField(restore, { restore = it }, label = { Text(stringResource(R.string.restore_wallet)) }, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { vm.restoreWallet(restore) }) { Text(stringResource(R.string.restore_wallet)) }
            return@Column
        }

        s.newMnemonic?.let { words ->
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.backup_warning))
                    Text(words.joinToString(" "), style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = vm::backupConfirmed) { Text("I wrote them down / Zapsáno") }
                }
            }
        }

        Text(stringResource(R.string.receive_address)); SelectionText(s.address.orEmpty())
        Text("${stringResource(R.string.wallet_balance)}: ${s.balanceSats?.satsToBtcString() ?: "…"} BTC")
        OutlinedButton(onClick = vm::refresh) { Text(stringResource(R.string.refresh)) }

        // Mode tabs
        ScrollableTabRow(selectedTabIndex = s.mode.ordinal) {
            AppMode.entries.forEach { mode ->
                Tab(
                    selected = s.mode == mode,
                    onClick = { vm.setMode(mode) },
                    text = {
                        Text(
                            when (mode) {
                                AppMode.RECEIVE -> stringResource(R.string.mode_receive)
                                AppMode.TRANSFER -> stringResource(R.string.mode_transfer)
                                AppMode.SWAP -> stringResource(R.string.mode_swap)
                                AppMode.WITHDRAW_CARD -> stringResource(R.string.mode_withdraw_card)
                            }
                        )
                    },
                )
            }
        }

        when (s.mode) {
            AppMode.RECEIVE -> {
                Text("Share the address above to receive BTC / Sdílejte adresu výše pro příjem BTC.")
            }

            AppMode.TRANSFER -> {
                Text(stringResource(R.string.transfer_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    s.transferAddress, vm::onTransferAddressChange,
                    label = { Text(stringResource(R.string.destination_address)) },
                    isError = s.addressError != null,
                    supportingText = { s.addressError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = vm::saveTransferAddress) { Text(stringResource(R.string.save_address)) }
                AmountField(s.amountInput, vm::onAmountChange)
                Button(onClick = vm::previewTransfer, enabled = !s.busy && s.addressError == null && s.transferAddress.isNotBlank()) {
                    Text(stringResource(R.string.preview_fee))
                }
                s.transferPreview?.let { p ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text("To: ${p.destination}")
                            Text("Amount: ${p.amountSats.satsToBtcString()} BTC")
                            Text("Fee: ${p.feeSats} sats (${p.feeRateSatPerVb} sat/vB)")
                            Button(onClick = onConfirmTransfer, enabled = !s.busy) { Text(stringResource(R.string.confirm_send)) }
                        }
                    }
                }
                s.lastTxid?.let { Text("Sent / Odesláno: $it") }
            }

            AppMode.WITHDRAW_CARD -> {
                Text(stringResource(R.string.withdraw_card_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.withdraw_card_hint), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.currency))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiatCurrency.supported.forEach { c ->
                        FilterChip(
                            selected = s.cashCurrency == c,
                            onClick = { vm.setCashCurrency(c) },
                            label = { Text(c.code) },
                        )
                    }
                }
                Text(stringResource(R.string.provider))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vm.offRampProviderNames.forEachIndexed { i, name ->
                        FilterChip(
                            selected = s.cashProviderIndex == i,
                            onClick = { vm.setCashProvider(i) },
                            label = { Text(name) },
                        )
                    }
                }
                AmountField(s.amountInput, vm::onAmountChange)
                Button(onClick = vm::quoteCashOut, enabled = !s.busy) { Text(stringResource(R.string.get_quote)) }
                s.cashQuote?.let { q ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text("${q.amountCrypto} ${q.cryptoAsset} → ${OffRampValidation.formatFiat(q.fiatNetMinor, q.currency)}")
                            Text("Fee / Poplatek: ${OffRampValidation.formatFiat(q.feeMinor, q.currency)}")
                            Text("Rate: 1 BTC ≈ ${q.rate} ${q.currency.code}")
                            s.cashFlow?.let { flow ->
                                Button(onClick = { openUrl(flow.url) }) {
                                    Text(stringResource(R.string.open_provider))
                                }
                            }
                        }
                    }
                }
            }

            AppMode.SWAP -> {
                Text(stringResource(R.string.swap_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.swap_hint), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(s.swapFrom, { vm.setSwapAssets(it, s.swapTo) }, label = { Text(stringResource(R.string.from_asset)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(s.swapTo, { vm.setSwapAssets(s.swapFrom, it) }, label = { Text(stringResource(R.string.to_asset)) }, modifier = Modifier.fillMaxWidth())
                AmountField(s.amountInput, vm::onAmountChange)
                Button(onClick = vm::quoteSwap, enabled = !s.busy) { Text(stringResource(R.string.get_quote)) }
                s.swapQuote?.let { q ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text("${q.amountIn} ${q.fromAsset} → ~${q.amountOut} ${q.toAsset}")
                            Text(q.feeNote)
                            s.swapFlow?.let { flow ->
                                Button(onClick = { openUrl(flow.url) }) { Text(stringResource(R.string.open_provider)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value, onChange,
        label = { Text(stringResource(R.string.amount_btc)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SelectionText(t: String) = androidx.compose.foundation.text.selection.SelectionContainer { Text(t) }
