package cz.faltyn.elfall.android

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import cz.faltyn.elfall.shared.SeedStore
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the mnemonic encrypted with an AES-256-GCM key that never leaves Android Keystore
 * (StrongBox-backed where available). There is intentionally no API to export the seed or private keys.
 * TODO: bind the key to biometric auth (setUserAuthenticationRequired) once the unlock flow uses CryptoObject.
 */
class KeystoreSeedStore(context: Context) : SeedStore {
    private val prefs = context.getSharedPreferences("elfall_secure", Context.MODE_PRIVATE)
    private val alias = "elfall_seed_key"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    override fun hasSeed() = prefs.contains("seed")

    override fun saveMnemonic(words: List<String>) {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val ct = c.doFinal(words.joinToString(" ").encodeToByteArray())
        prefs.edit()
            .putString("seed", Base64.encodeToString(ct, Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(c.iv, Base64.NO_WRAP))
            .apply()
    }

    override fun loadMnemonic(): List<String>? {
        val ct = prefs.getString("seed", null) ?: return null
        val iv = prefs.getString("iv", null) ?: return null
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        return c.doFinal(Base64.decode(ct, Base64.NO_WRAP)).decodeToString().split(" ")
    }

    override fun wipe() {
        prefs.edit().clear().apply()
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(alias)
    }

    override fun savePayoutAddress(address: String?) = prefs.edit().putString("payout_address", address).apply()
    override fun loadPayoutAddress(): String? = prefs.getString("payout_address", null)
}
