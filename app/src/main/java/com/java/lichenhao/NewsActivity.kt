package com.java.lichenhao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import kotlinx.android.synthetic.main.content_news.*

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

import java.util.Objects

class NewsActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_item_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun share() {
        // todo
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            // todo: 直接就是ListActivity的操作
        }
        return super.onOptionsItemSelected(item)
    }

    private val screenWidth: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        outMetrics.widthPixels
    }

    private fun scale(old: Bitmap): Bitmap {
        val resultWidth = screenWidth * 4 / 5
        val scale = resultWidth.toFloat() / old.width
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(old, 0, 0, old.height, old.height, matrix, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        setSupportActionBar(findViewById(R.id.toolbar))
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        val news = intent.getSerializableExtra(NEWS_CONTENT) as News
        news_title.text = news.title
        news_content.text = news.content.toArticle()
        news_content.movementMethod = ScrollingMovementMethod.getInstance()

        if (news.imageList.isNotEmpty()) {
            news.imageList[0].httpGet().response { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Log.e("my", ex.toString())
                    }
                    is Result.Success -> {
                        val byteArray = result.get()
                        news_image.setImageBitmap(scale(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)))
                    }
                }
            }
        } else {
            news_image.visibility = GONE
        }
    }

    companion object {
        const val NEWS_CONTENT = "NEWS_CONTENT"
    }
}
