package com.java.lichenhao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import kotlinx.android.synthetic.main.content_news.*

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import com.github.kittinunf.result.Result

import java.util.Objects

// Intent里放不下这么大(可能带图片)的数据(也完全没必要复制一遍)，直接用全局变量传参了
var NEWS_ACTIVITY_INTENT_ARG: NewsExt? = null

class NewsActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_item_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            // todo: 直接就是ListActivity的操作
        }
        return super.onOptionsItemSelected(item)
    }

    private fun scale(old: Bitmap, imgWidth: Int): Bitmap {
        val scale = imgWidth.toFloat() / old.width
        return Bitmap.createScaledBitmap(old, imgWidth, (old.height * scale).toInt(), true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        setSupportActionBar(findViewById(R.id.toolbar))
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        val newsExt = NEWS_ACTIVITY_INTENT_ARG!!
        val news = newsExt.news
        news_title.text = news.title
        news_content.text = news.content.toArticle()
        news_content.movementMethod = ScrollingMovementMethod.getInstance()

        if (news.imageList.isNotEmpty()) {
            newsExt.downloadImage(0) { result ->
                when (result) {
                    is Result.Failure -> {
                        Log.e("my", result.getException().toString())
                    }
                    is Result.Success -> {
                        val byteArray = result.get()
                        Log.e("fuck", "$byteArray ${byteArray.contentToString()}")
                        news_image.setImageBitmap(
                            scale(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size), news_image.width)
                        )
                    }
                }
            }
        } else {
            news_image.visibility = GONE
        }
    }
}

// 2019-08-16 01:47:36.264 18933-18933/com.java.lichenhao E/fuck: [B@2a7507e [-1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 1, 44, 1, 44, 0, 0, -1, -31, 47, 21, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 0, 60, 63, 120, 112, 97, 99, 107, 101, 116, 32, 98, 101, 103, 105, 110, 61, 34, -17, -69, -65, 34, 32, 105, 100, 61, 34, 87, 53, 77, 48, 77, 112, 67, 101, 104, 105, 72, 122, 114, 101, 83, 122, 78, 84, 99, 122, 107, 99, 57, 100, 34, 63, 62, 32, 60, 120, 58, 120, 109, 112, 109, 101, 116, 97, 32, 120, 109, 108, 110, 115, 58, 120, 61, 34, 97, 100, 111, 98, 101, 58, 110, 115, 58, 109, 101, 116, 97, 47, 34, 32, 120, 58, 120, 109, 112, 116, 107, 61, 34, 88, 77, 80, 32, 67, 111, 114, 101, 32, 52, 46, 52, 46, 48, 45, 69, 120, 105, 118, 50, 34, 62, 32, 60, 114, 100, 102, 58, 82, 68, 70, 32, 120, 109, 108, 110, 115, 58, 114, 100, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 49, 57, 57, 57, 47, 48, 50, 47, 50, 50, 45, 114, 100, 102, 45, 115, 121, 110, 116, 97, 120, 45, 110, 115, 35, 34, 62, 32, 60, 114, 100, 102, 58, 68, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 32, 114, 100, 102, 58, 97, 98, 111, 117, 116, 61, 34, 34, 32, 120, 109, 108, 110, 115, 58, 100, 99, 61, 34, 104, 116, 116, 112, 58, 47, 47, 112, 117, 114, 108, 46, 111, 114, 103, 47, 100, 99, 47, 101, 108, 101, 109, 101, 110, 116, 115, 47, 49, 46, 49, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 71, 73, 109, 103, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 103, 47, 105, 109, 103, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 77, 77, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 109, 109, 47, 34, 32, 120, 109, 108, 110, 115, 58, 115, 116, 82, 101, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 115, 84, 121, 112, 101, 47, 82, 101, 115, 111, 117, 114, 99, 101, 82, 101, 102, 35, 34, 32, 120, 109, 108, 110, 115, 58, 115, 116, 69, 118, 116, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 115, 84, 121, 112, 101, 47, 82, 101, 115, 111, 117, 114, 99, 101, 69, 118, 101, 110, 116, 35, 34, 32, 120, 109, 108, 110, 115, 58, 105, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 105, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 47, 49, 46, 48, 47, 34, 32, 120, 109, 108, 110, 115, 58, 112, 100, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 112, 100, 102, 47, 49, 46, 51, 47, 34, 32, 100, 99, 58, 102, 111, 114, 109, 97, 116, 61, 34, 105, 109, 97, 103, 101, 47, 106, 112, 101, 103, 34, 32, 120, 109, 112, 58, 77, 101, 116, 97, 100, 97, 116, 97, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 57, 58, 51, 56, 58, 49, 52, 43, 48, 56, 58, 48, 48, 34, 32, 120, 109, 112, 58, 77, 111, 100, 105, 102, 121, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 49, 58, 51, 56, 58, 49, 54, 90, 34, 32, 120, 109, 112, 58, 67, 114, 101, 97, 116, 101, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 57, 58, 51, 56, 58, 49, 52, 43, 48, 56, 58, 48, 48, 34, 32, 120, 109, 112, 58, 67, 114, 101, 97, 116, 111, 114, 84, 111, 111, 108, 61, 34, 65, 100, 111, 98, 101, 32, 73, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 32, 67, 67, 32, 50, 50, 46, 48, 32, 40, 87, 105, 110, 100, 111, 119, 115, 41, 34, 32, 120, 109, 112, 77, 77, 58, 73, 110, 115, 116, 97, 110, 99, 101, 73, 68, 61, 34, 120, 109, 112, 46, 105, 105, 100, 58, 57, 55, 5
// 2019-08-16 01:48:47.501 19224-19224/com.java.lichenhao E/fuck: [B@2a7507e [-1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 1, 44, 1, 44, 0, 0, -1, -31, 47, 21, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 0, 60, 63, 120, 112, 97, 99, 107, 101, 116, 32, 98, 101, 103, 105, 110, 61, 34, -17, -69, -65, 34, 32, 105, 100, 61, 34, 87, 53, 77, 48, 77, 112, 67, 101, 104, 105, 72, 122, 114, 101, 83, 122, 78, 84, 99, 122, 107, 99, 57, 100, 34, 63, 62, 32, 60, 120, 58, 120, 109, 112, 109, 101, 116, 97, 32, 120, 109, 108, 110, 115, 58, 120, 61, 34, 97, 100, 111, 98, 101, 58, 110, 115, 58, 109, 101, 116, 97, 47, 34, 32, 120, 58, 120, 109, 112, 116, 107, 61, 34, 88, 77, 80, 32, 67, 111, 114, 101, 32, 52, 46, 52, 46, 48, 45, 69, 120, 105, 118, 50, 34, 62, 32, 60, 114, 100, 102, 58, 82, 68, 70, 32, 120, 109, 108, 110, 115, 58, 114, 100, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 49, 57, 57, 57, 47, 48, 50, 47, 50, 50, 45, 114, 100, 102, 45, 115, 121, 110, 116, 97, 120, 45, 110, 115, 35, 34, 62, 32, 60, 114, 100, 102, 58, 68, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 32, 114, 100, 102, 58, 97, 98, 111, 117, 116, 61, 34, 34, 32, 120, 109, 108, 110, 115, 58, 100, 99, 61, 34, 104, 116, 116, 112, 58, 47, 47, 112, 117, 114, 108, 46, 111, 114, 103, 47, 100, 99, 47, 101, 108, 101, 109, 101, 110, 116, 115, 47, 49, 46, 49, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 71, 73, 109, 103, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 103, 47, 105, 109, 103, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 77, 77, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 109, 109, 47, 34, 32, 120, 109, 108, 110, 115, 58, 115, 116, 82, 101, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 115, 84, 121, 112, 101, 47, 82, 101, 115, 111, 117, 114, 99, 101, 82, 101, 102, 35, 34, 32, 120, 109, 108, 110, 115, 58, 115, 116, 69, 118, 116, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 115, 84, 121, 112, 101, 47, 82, 101, 115, 111, 117, 114, 99, 101, 69, 118, 101, 110, 116, 35, 34, 32, 120, 109, 108, 110, 115, 58, 105, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 105, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 47, 49, 46, 48, 47, 34, 32, 120, 109, 108, 110, 115, 58, 112, 100, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 112, 100, 102, 47, 49, 46, 51, 47, 34, 32, 100, 99, 58, 102, 111, 114, 109, 97, 116, 61, 34, 105, 109, 97, 103, 101, 47, 106, 112, 101, 103, 34, 32, 120, 109, 112, 58, 77, 101, 116, 97, 100, 97, 116, 97, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 57, 58, 51, 56, 58, 49, 52, 43, 48, 56, 58, 48, 48, 34, 32, 120, 109, 112, 58, 77, 111, 100, 105, 102, 121, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 49, 58, 51, 56, 58, 49, 54, 90, 34, 32, 120, 109, 112, 58, 67, 114, 101, 97, 116, 101, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 57, 58, 51, 56, 58, 49, 52, 43, 48, 56, 58, 48, 48, 34, 32, 120, 109, 112, 58, 67, 114, 101, 97, 116, 111, 114, 84, 111, 111, 108, 61, 34, 65, 100, 111, 98, 101, 32, 73, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 32, 67, 67, 32, 50, 50, 46, 48, 32, 40, 87, 105, 110, 100, 111, 119, 115, 41, 34, 32, 120, 109, 112, 77, 77, 58, 73, 110, 115, 116, 97, 110, 99, 101, 73, 68, 61, 34, 120, 109, 112, 46, 105, 105, 100, 58, 57, 55, 5
// 2019-08-16 01:50:25.841 19545-19545/com.java.lichenhao E/fuck: [B@4f0f8cf [-1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 1, 44, 1, 44, 0, 0, -1, -31, 47, 21, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 0, 60, 63, 120, 112, 97, 99, 107, 101, 116, 32, 98, 101, 103, 105, 110, 61, 34, -17, -69, -65, 34, 32, 105, 100, 61, 34, 87, 53, 77, 48, 77, 112, 67, 101, 104, 105, 72, 122, 114, 101, 83, 122, 78, 84, 99, 122, 107, 99, 57, 100, 34, 63, 62, 32, 60, 120, 58, 120, 109, 112, 109, 101, 116, 97, 32, 120, 109, 108, 110, 115, 58, 120, 61, 34, 97, 100, 111, 98, 101, 58, 110, 115, 58, 109, 101, 116, 97, 47, 34, 32, 120, 58, 120, 109, 112, 116, 107, 61, 34, 88, 77, 80, 32, 67, 111, 114, 101, 32, 52, 46, 52, 46, 48, 45, 69, 120, 105, 118, 50, 34, 62, 32, 60, 114, 100, 102, 58, 82, 68, 70, 32, 120, 109, 108, 110, 115, 58, 114, 100, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 119, 119, 119, 46, 119, 51, 46, 111, 114, 103, 47, 49, 57, 57, 57, 47, 48, 50, 47, 50, 50, 45, 114, 100, 102, 45, 115, 121, 110, 116, 97, 120, 45, 110, 115, 35, 34, 62, 32, 60, 114, 100, 102, 58, 68, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 32, 114, 100, 102, 58, 97, 98, 111, 117, 116, 61, 34, 34, 32, 120, 109, 108, 110, 115, 58, 100, 99, 61, 34, 104, 116, 116, 112, 58, 47, 47, 112, 117, 114, 108, 46, 111, 114, 103, 47, 100, 99, 47, 101, 108, 101, 109, 101, 110, 116, 115, 47, 49, 46, 49, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 71, 73, 109, 103, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 103, 47, 105, 109, 103, 47, 34, 32, 120, 109, 108, 110, 115, 58, 120, 109, 112, 77, 77, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 109, 109, 47, 34, 32, 120, 109, 108, 110, 115, 58, 115, 116, 82, 101, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 115, 84, 121, 112, 101, 47, 82, 101, 115, 111, 117, 114, 99, 101, 82, 101, 102, 35, 34, 32, 120, 109, 108, 110, 115, 58, 115, 116, 69, 118, 116, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 120, 97, 112, 47, 49, 46, 48, 47, 115, 84, 121, 112, 101, 47, 82, 101, 115, 111, 117, 114, 99, 101, 69, 118, 101, 110, 116, 35, 34, 32, 120, 109, 108, 110, 115, 58, 105, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 105, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 47, 49, 46, 48, 47, 34, 32, 120, 109, 108, 110, 115, 58, 112, 100, 102, 61, 34, 104, 116, 116, 112, 58, 47, 47, 110, 115, 46, 97, 100, 111, 98, 101, 46, 99, 111, 109, 47, 112, 100, 102, 47, 49, 46, 51, 47, 34, 32, 100, 99, 58, 102, 111, 114, 109, 97, 116, 61, 34, 105, 109, 97, 103, 101, 47, 106, 112, 101, 103, 34, 32, 120, 109, 112, 58, 77, 101, 116, 97, 100, 97, 116, 97, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 57, 58, 51, 56, 58, 49, 52, 43, 48, 56, 58, 48, 48, 34, 32, 120, 109, 112, 58, 77, 111, 100, 105, 102, 121, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 49, 58, 51, 56, 58, 49, 54, 90, 34, 32, 120, 109, 112, 58, 67, 114, 101, 97, 116, 101, 68, 97, 116, 101, 61, 34, 50, 48, 49, 57, 45, 48, 56, 45, 49, 50, 84, 49, 57, 58, 51, 56, 58, 49, 52, 43, 48, 56, 58, 48, 48, 34, 32, 120, 109, 112, 58, 67, 114, 101, 97, 116, 111, 114, 84, 111, 111, 108, 61, 34, 65, 100, 111, 98, 101, 32, 73, 108, 108, 117, 115, 116, 114, 97, 116, 111, 114, 32, 67, 67, 32, 50, 50, 46, 48, 32, 40, 87, 105, 110, 100, 111, 119, 115, 41, 34, 32, 120, 109, 112, 77, 77, 58, 73, 110, 115, 116, 97, 110, 99, 101, 73, 68, 61, 34, 120, 109, 112, 46, 105, 105, 100, 58, 57, 55, 5