package cz.faltyn.eleanor.android

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.faltyn.eleanor.shared.satsToBtcString

class MainActivity : AppCompatActivity() {
    private val vm: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val s by vm.state.collectAsStateWithLifecycle()
                    EleanorScreen(s, vm, onConfirmSend = {
                        BiometricGate.confirm(this, getString(R.string.biometric_title), getString(R.string.biometric_subtitle)) { ok, err ->
                            if (ok) vm.sendConfirmed() else err?.let { /* surfaced via snackbar in a later iteration */ }
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun EleanorScreen(s: UiState, vm: WalletViewModel, onConfirmSend: () -> Unit) {
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
            Card { Column(Modifier.padding(12.dp)) {
                Text(stringResource(R.string.backup_warning))
                Text(words.joinToString(" "), style = MaterialTheme.typography.bodyLarge)
                Button(onClick = vm::backupConfirmed) { Text("I wrote them down") }
            } }
        }

        Text(stringResource(R.string.receive_address)); SelectionText(s.address.orEmpty())
        Text("${stringResource(R.string.wallet_balance)}: ${s.balanceSats?.satsToBtcString() ?: "…"} BTC")
        OutlinedButton(onClick = vm::refresh) { Text(stringResource(R.string.refresh)) }

        HorizontalDivider()
        Text(stringResource(R.string.withdraw_to_crypto_wallet), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            s.payoutAddress, vm::onPayoutAddressChange,
            label = { Text(stringResource(R.string.destination_address)) },
            isError = s.addressError != null,
            supportingText = { s.addressError?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = vm::savePayoutAddress, enabled = s.addressError == null && s.payoutAddress.isNotBlank()) {
            Text(stringResource(R.string.save_address))
        }
        OutlinedTextField(
            s.amountInput, vm::onAmountChange,
            label = { Text(stringResource(R.string.amount_btc)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = vm::previewFee, enabled = !s.busy && s.addressError == null && s.payoutAddress.isNotBlank()) {
            Text(stringResource(R.string.preview_fee))
        }
        s.preview?.let { p ->
            Card { Column(Modifier.padding(12.dp)) {
                Text("To: ${p.destination}")
                Text("Amount: ${p.amountSats.satsToBtcString()} BTC")
                Text("Fee: ${p.feeSats} sats (${p.feeRateSatPerVb} sat/vB × ~${p.estimatedVbytes} vB)")
                Text("Total: ${p.totalSats.satsToBtcString()} BTC")
                if (p.changeSats > 0) Text("Change back to you: ${p.changeSats.satsToBtcString()} BTC")
                Button(onClick = onConfirmSend, enabled = !s.busy) { Text(stringResource(R.string.confirm_send)) }
            } }
        }
        s.lastTxid?.let { Text("Sent! txid: $it") }
    }
}

@Composable
private fun SelectionText(t: String) = androidx.compose.foundation.text.selection.SelectionContainer { Text(t) }
