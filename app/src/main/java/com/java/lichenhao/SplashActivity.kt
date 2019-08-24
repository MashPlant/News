package com.java.lichenhao

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast

import java.lang.ref.WeakReference

// 没必要警告内存泄漏，因为App的生命周期本来就是全局的
@SuppressLint("StaticFieldLeak")
lateinit var GLOBAL_CONTEXT: Context

class SplashActivity : Activity() {
    internal class ResourceInit(ref: SplashActivity, val input: AccountInput) : AsyncTask<Void?, String, Void?>() {
        private val ref = WeakReference(ref)

        override fun doInBackground(vararg voids: Void?): Void? {
            initAdapterGlobals(input.username, input.password)
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            val activity = ref.get()!!
            val srcIntent = activity.intent
            val dstIntent = Intent(activity, ListActivity::class.java)
            dstIntent.putExtras(srcIntent)
            activity.startActivity(dstIntent)
            activity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        GLOBAL_CONTEXT = applicationContext

        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
        }

        val accounts = AccountManager(this)
        val dialog = LoginDialog(this)
        dialog.show { username, password, isRegister ->
            val input = AccountInput(username, password)
            Log.e("input", "$input")
            if (isRegister) {
                when (accounts.register(input)) {
                    RegisterResult.Ok -> {
                        ResourceInit(this, input).execute()
                        dialog.dismiss()
                    }
                    RegisterResult.DuplicateUser -> Toast.makeText(this, "用户名已经存在", Toast.LENGTH_SHORT).show()
                    RegisterResult.InvalidAccount -> Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show()
                }
            } else {
                when (accounts.login(input)) {
                    LoginResult.Ok -> {
                        ResourceInit(this, input).execute()
                        dialog.dismiss()
                    }
                    LoginResult.NoSuchUser -> Toast.makeText(this, "用户名不存在", Toast.LENGTH_SHORT).show()
                    LoginResult.WrongPassword -> Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
