package com.java.lichenhao

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View.GONE
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.content_news.*
import kotlinx.android.synthetic.main.content_news.view.*
import java.util.*
import kotlin.collections.ArrayList
import android.widget.ArrayAdapter
import com.github.kittinunf.result.success
import java.net.URLEncoder

// Intent里放不下这么大(可能带图片)的数据(也完全没必要复制一遍)，直接用全局变量传参了
var NEWS_ACTIVITY_INTENT_ARG: NewsExt? = null

data class RecommendNews(
    var data: News,
    var rate: Float
)

class NewsActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.list_item_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun scale(old: Bitmap, imgWidth: Int): Bitmap {
        val scale = imgWidth.toFloat() / old.width
        return Bitmap.createScaledBitmap(old, imgWidth, (old.height * scale).toInt(), true)
    }

    private fun getRecommendNews(news: News): ArrayList<RecommendNews> {
        val recommendNews = ArrayList<RecommendNews>()
        for (keyword in news.keywords) {
            val url = BASE_URL + "size=5&startDate=1998-09-07&endDate=2098-09-07&words=" +
                    URLEncoder.encode(keyword.word, CHARSET)
            url.httpGet().responseObject<Response> { _, _, result ->
                result.success {
                    var i = 1.0F
                    for (newsData in it.data) {
                        if (newsData.title != news.title) {
                            recommendNews.add(RecommendNews(newsData, keyword.score * i))
                            i *= 0.9F
                        }
                    }
                }
            }
        }
        return recommendNews
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
        val newsExt = NEWS_ACTIVITY_INTENT_ARG!!
        val news = newsExt.news
        content_news.viewTreeObserver.addOnGlobalLayoutListener {
            news_title.text = news.title
            news_content.text = news.content.toArticle()
            if (news.imageList.isNotEmpty()) {
                newsExt.downloadImage(0) {
                    it.success { img -> news_image.setImageBitmap(scale(img, news_image.width)) }
                }
            } else {
                news_image.visibility = GONE
            }
        }

        val listItems = ArrayList<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        content_news.recommendNews.adapter = adapter
        val recommendNews = getRecommendNews(news)
        val shownRecommendNews = ArrayList<News>()

        clickShowRecommendNews.setOnClickListener {
            recommendNews
                .distinctBy { it.data.title }
                .sortedByDescending { it.rate }
                .take(10)
                .forEach {
                    shownRecommendNews.add(it.data)
                    listItems.add(it.data.title)
                }
            adapter.notifyDataSetChanged()
            content_news.clickShowRecommendNews.text = "猜你喜欢"
        }
        content_news.recommendNews.setOnItemClickListener { _, _, i, _ ->
            val tmp = NewsExt(shownRecommendNews[i])
            NEWS_ACTIVITY_INTENT_ARG = tmp
            THE_NEWS_ADAPTER.add(tmp, READ_IDX)
            startActivity(Intent(this, this.javaClass))
        }
    }
}