package com.java.lichenhao

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.TextView
import android.widget.Toast

import java.lang.ref.WeakReference

class SplashActivity : Activity() {

    private var state: TextView? = null

    internal class ResourceInit(ref: SplashActivity) : AsyncTask<Void?, String, Void?>() {
        private val ref = WeakReference(ref)

        override fun doInBackground(vararg voids: Void?): Void? {
            publishProgress("loading...")
            return null
        }

        override fun onProgressUpdate(vararg values: String) {
            super.onProgressUpdate(*values)
            ref.get()!!.state!!.text = values[0]
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

        // manually ask for RW permission
        while (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }

        state = findViewById(R.id.splash_init_state)

        ResourceInit(this).execute()
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
