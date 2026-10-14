package cz.faltyn.elfall.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Zabalí datový klíč libsodium klíčem AES-256-GCM z Android Keystore (nevyjmutelný, ideálně StrongBox).
 * [requireStrongBox] (přepínač BuildConfig.STRONGBOX_REQUIRED, výchozí false): bez StrongBoxu výjimka místo fallbacku na TEE.
 */
class KeystoreKeyWrapper(private val alias: String, private val requireStrongBox: Boolean = BuildConfig.STRONGBOX_REQUIRED) : KeyWrapper {
    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        fun gen(strongBox: Boolean): SecretKey {
            val b = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256)
            if (strongBox && android.os.Build.VERSION.SDK_INT >= 28) b.setIsStrongBoxBacked(true)
            return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply { init(b.build()) }.generateKey()
        }
        if (requireStrongBox) return strongBoxOnly(android.os.Build.VERSION.SDK_INT) { gen(true) }
        return runCatching { gen(true) }.getOrElse { gen(false) }
    }

    override fun wrap(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        return c.iv + c.doFinal(plain)
    }

    override fun unwrap(wrapped: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, wrapped.copyOfRange(0, 12)))
        return c.doFinal(wrapped.copyOfRange(12, wrapped.size))
    }
}

/** StrongBox vyžadován: API < 28 nebo chybějící StrongBox => výjimka (fail-closed), nikdy tichý fallback. */
internal fun <T> strongBoxOnly(sdk: Int, gen: () -> T): T {
    check(sdk >= 28) { "StrongBox vyžaduje Android 9+ (API 28)" }
    return runCatching(gen).getOrElse { throw IllegalStateException("StrongBox není k dispozici", it) }
}
