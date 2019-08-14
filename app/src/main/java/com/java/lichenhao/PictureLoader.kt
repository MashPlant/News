package com.java.lichenhao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.github.kittinunf.fuel.Fuel

import java.lang.ref.WeakReference

class PictureLoader(
    target: ImageView, private val resultWidth: Int // always fill the width
) : AsyncTask<String, Void, Bitmap>() {
    private val target = WeakReference(target)

    override fun onPostExecute(bitmap: Bitmap) {
        target.get()!!.setImageBitmap(bitmap)
    }

    override fun doInBackground(vararg strings: String): Bitmap {
        val picturePath = strings[0]
        Log.e("my", "picturePath = \"$picturePath\"")
        val byteArray = Fuel.download(picturePath).response().third.get()
        val old = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        val width = old.width
        val height = old.height
        val scale = resultWidth.toFloat() / width
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(old, 0, 0, width, height, matrix, true)
    }
}
