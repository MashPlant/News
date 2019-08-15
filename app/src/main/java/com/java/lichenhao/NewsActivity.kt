package com.java.lichenhao

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.content_news.*
import java.util.*

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

        // 注意不能在onCreate中访问news_image.width，结果会是0，因为此时image尚未绘制
        // 这导致Bitmap.createScaledBitmap返回null，这也是非常诡异的，这里抛异常也许更合理
        // 文档中没有说过任何情况下Bitmap.createScaledBitmap会返回null，更过分的是类型系统中它就必须返回非空
        // 总结：垃圾安卓，垃圾文档
        content_news.viewTreeObserver.addOnGlobalLayoutListener {
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
                            news_image.setImageBitmap(scale(result.get(), news_image.width))
                        }
                    }
                }
            } else {
                news_image.visibility = GONE
            }
        }
    }
}