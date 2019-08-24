package com.java.lichenhao

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

const val ACCOUNT_FILE_NAME = "Account"

data class AccountInput(val username: String, val password: String)

@Parcelize
data class AccountStore(val username: String, val password: ByteArray) : Parcelable {
    constructor(input: AccountInput) : this(input.username, {
        val md5 = MessageDigest.getInstance("MD5")
        md5.update(input.password.toByteArray())
        md5.digest()
    }())
}

enum class LoginResult {
    Ok, NoSuchUser, WrongPassword
}

enum class RegisterResult {
    Ok, DuplicateUser, InvalidAccount
}

class AccountManager(private val context: Context) {
    private val accounts = ArrayList<AccountStore>(0)

    init {
        try {
            val bytes = context.openFileInput(ACCOUNT_FILE_NAME).use { it.readBytes() }
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            parcel.readTypedList(accounts, ParcelHelper.ACCOUNT_STORE_CREATOR)
            parcel.recycle()
        } catch (e: IOException) { // 正常，应该是文件还不存在
            Log.e("AccountManager.init", e.toString())
        }
    }

    fun login(input: AccountInput): LoginResult {
        val store = AccountStore(input)
        for (x in accounts) {
            if (x.username == input.username) {
                return if (x.password.contentEquals(store.password)) {
                    LoginResult.Ok
                } else {
                    LoginResult.WrongPassword
                }
            }
        }
        return LoginResult.NoSuchUser
    }

    fun register(input: AccountInput): RegisterResult {
        if (input.username.isEmpty() || input.password.isEmpty()) {
            return RegisterResult.InvalidAccount
        }
        for (x in accounts) {
            if (x.username == input.username) {
                return RegisterResult.DuplicateUser
            }
        }
        accounts.add(AccountStore(input))
        val parcel = Parcel.obtain()
        parcel.writeTypedList(accounts)
        context.openFileOutput(ACCOUNT_FILE_NAME, Context.MODE_PRIVATE).use { it.write(parcel.marshall()) }
        parcel.recycle()
        return RegisterResult.Ok
    }
}

fun initAdapterGlobals(username: String, password: String) {
    val password1 = password.toCharArray()
    ALL_KIND = GLOBAL_CONTEXT.resources.getStringArray(R.array.kinds)
    ALL_CATEGORY = GLOBAL_CONTEXT.resources.getStringArray(R.array.categories)
    USERNAME = username
    CIPHER = makeCipher(password1, Cipher.ENCRYPT_MODE)
    try {
        NewsData.loadFromFile(makeCipher(password1, Cipher.DECRYPT_MODE))
    } catch (e: IOException) { // 正常，应该是文件还不存在
        Log.e("initAdapterGlobals", e.toString())
    }
}

private val SALT = byteArrayOf(
    0x43.toByte(),
    0x76.toByte(),
    0x95.toByte(),
    0xc7.toByte(),
    0x5b.toByte(),
    0xd7.toByte(),
    0x45.toByte(),
    0x17.toByte()
)

// mode为Cipher.ENCRYPT_MODE或Cipher.DECRYPT_MODE
private fun makeCipher(password: CharArray, mode: Int): Cipher {
    val keySpec = PBEKeySpec(password)
    val keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
    val key = keyFactory.generateSecret(keySpec)
    val pbeParamSpec = PBEParameterSpec(SALT, 43)
    val cipher = Cipher.getInstance("PBEWithMD5AndDES")
    cipher.init(mode, key, pbeParamSpec)
    return cipher
}