package com.java.lichenhao

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.text.method.ScrollingMovementMethod
import android.util.ArraySet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.content_news.*
import kotlinx.android.synthetic.main.content_news.view.*
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.BitmapFactory
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.content_select.*

// Intent里放不下这么大(可能带图片)的数据(也完全没必要复制一遍)，直接用全局变量传参了
var NEWS_ACTIVITY_INTENT_ARG: NewsExt? = null

data class RecommendNews_t (
    var data: News,
    var rate: Float
)

class NewsActivity(newsData: News? = null) : AppCompatActivity() {

    private var newsData: News? = null
    private var createByNet:Boolean? = null

    init {
        if (newsData != null) {
            this.newsData = newsData
            Log.e("newsData", this.newsData?.title)
            this.createByNet = true
            Log.e("init: createByNet", createByNet.toString())
        } else {
            this.createByNet = false
            Log.e("init: createByNet", createByNet.toString())
        }
    }

    private var recommendNewsList = ArrayList<RecommendNews_t>()
    var listItems = ArrayList<String>()
    private var adapter: ArrayAdapter<String>? = null

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

    private fun setRecommendNews(news: News) {
        for (keyword in news.keywords) {
            var url = BASE_URL + "size=5&startDate=1998-09-07&endDate=2098-09-07&words=" + java.net.URLEncoder.encode(keyword.word, CHARSET)
            url.httpGet().responseObject<Response> { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Log.e("fuck", ex.toString())
                    }
                    is Result.Success -> {
                        val data = result.get()
                        var i: Float = 1.0.toFloat()
                        for (newsData in data.data) {
                            if (!newsData.title.equals(news.title)) {
                                recommendNewsList.add(RecommendNews_t(newsData, keyword.score * i))
                                i *= 0.9.toFloat()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_news)
        setSupportActionBar(findViewById(R.id.toolbar))
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        // 注意不能在onCreate中访问news_image.width，结果会是0，因为此时image尚未绘制
        // 这导致Bitmap.createScaledBitmap返回null，这也是非常诡异的，这里抛异常也许更合理
        // 文档中没有说过任何情况下Bitmap.createScaledBitmap会返回null，更过分的是类型系统中它就必须返回非空
        // 总结：垃圾安卓，垃圾文档

        Log.e("createByNet", createByNet.toString())
        if (!createByNet!!) {
            val newsExt = NEWS_ACTIVITY_INTENT_ARG!!
            val news = newsExt.news
            content_news.viewTreeObserver.addOnGlobalLayoutListener {
                news_title.text = news.title
                news_content.text = news.content.toArticle()
//            news_content.movementMethod = ScrollingMovementMethod.getInstance()

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
            setRecommendNews(news)
        } else {
            Log.e("fuck", "fuck")
            if (newsData!!.imageList.isNotEmpty()) {
                Log.e("what", newsData!!.imageList[0])
                val url = java.net.URL(newsData?.imageString)
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                content_news.news_image.setImageBitmap(bmp)
            }
            content_news.news_title.text = newsData?.title
            content_news.news_content.text = newsData?.content?.toArticle()
        }

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listItems
        )
        content_news.recommendNews.adapter = adapter

        clickShowRecommendNews.setOnClickListener {
            val distinctList = recommendNewsList.distinctBy {
                it.data.title
            }
            val sortedList = distinctList.sortedBy {
                1 - it.rate
            }
            recommendNewsList.clear()
            listItems.clear()
            Log.e("----------", "----------")
            var MAX_RECOMMEND = 10
            var recommend_count = 0
            for (item in sortedList) {
                if (recommend_count >= MAX_RECOMMEND) {
                    break
                }
                recommendNewsList.add(item)
                listItems.add(item.data.title)
                Log.i("title", item.data.title)
                ++recommend_count
            }

            adapter?.notifyDataSetChanged()
            content_news.clickShowRecommendNews.text = "猜你喜欢"
        }
        content_news.recommendNews.setOnItemClickListener { adapterView, view, i, l ->
            Log.e("...", "...")
            val intent = android.content.Intent(this, NewsActivity(recommendNewsList[i].data)::class.java)
            this.startActivity(intent)
        }
    }
}