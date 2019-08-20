package com.java.lichenhao

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter
import java.io.IOException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import kotlin.collections.ArrayList

const val LATEST_IDX = 0
const val FAVORITE_IDX = 1
const val READ_IDX = 2
const val SEARCH_IDX = 3

const val ALL_IDX = 0

// ListActivity.onCreate中初始化
lateinit var ALL_KIND: Array<String>
lateinit var ALL_CATEGORY: Array<String>

// 这些都在SplashActivity/AccountManager中接受用户输入初始化
lateinit var USERNAME: String
// 只用来加密
lateinit var CIPHER: Cipher
val FILE_INPUT_FAVORITE = ArrayList<NewsExt>()
val FILE_INPUT_READ = ArrayList<NewsExt>()

const val NEWS_FILE_NAME = "News"

const val GET_COUNT = 100

lateinit var THE_NEWS_ADAPTER: NewsAdapter

class NewsAdapter(private val activity: ListActivity, news_list: UltimateRecyclerView) :
    UltimateViewAdapter<NewsAdapter.ViewHolder>() {
    val allNews = Array(ALL_KIND.size) { Array(ALL_CATEGORY.size) { ArrayList<NewsExt>() } }

    var curKindId = LATEST_IDX
        private set
    var curCategoryId = ALL_IDX
        private set

    private var prevKindId: Int = 0
    val curNews
        inline get() = allNews[curKindId][curCategoryId]

    private var timeTag = 0 // 见ViewHolder.timeTag

    init {
        THE_NEWS_ADAPTER = this
        news_list.setAdapter(this)
        news_list.setDefaultOnRefreshListener {
            if (curKindId == LATEST_IDX) {
                val c = Calendar.getInstance()
                val url =
                    "${BASE_URL}endDate=${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}&size=$GET_COUNT"
                url.httpGet().responseObject<Response> { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            Toast.makeText(
                                activity, "${activity.resources.getString(R.string.network_err)}${result.error}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Success -> {
                            val latestKind = allNews[LATEST_IDX]
                            for (x in latestKind) {
                                x.clear()
                            }
                            for (news in result.value.data) {
                                add1(NewsExt(news), latestKind)
                            }
                            notifyDataSetChanged()
                        }
                    }
                    news_list.setRefreshing(false)
                }
            } else {
                news_list.setRefreshing(false)
            }
        }
        news_list.reenableLoadmore()
        news_list.setOnLoadMoreListener { _, _ ->
            if (curKindId == LATEST_IDX) {
                news_list.setRefreshing(true)
                val latestKind = allNews[LATEST_IDX]
                val endDate = if (latestKind[ALL_IDX].isNotEmpty()) {
                    latestKind[ALL_IDX].last().news.publishTime
                } else {
                    val c = Calendar.getInstance()
                    "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}"
                }
                val url = "${BASE_URL}endDate=$endDate&size=$GET_COUNT"
                url.httpGet().responseObject<Response> { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            Toast.makeText(
                                activity, "${activity.resources.getString(R.string.network_err)}${result.error}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Success -> {
                            for (news in result.value.data) {
                                add1(NewsExt(news), latestKind)
                            }
                            notifyDataSetChanged()
                        }
                    }
                    news_list.setRefreshing(false)
                }
            }
        }
        loadFromFile()
    }

    override fun getAdapterItemCount() = curNews.size

    override fun generateHeaderId(position: Int) = getItem(position).news.title[0].toLong()

    override fun newHeaderHolder(view: View) = ViewHolder(view)

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_header, parent, false)
        return object : RecyclerView.ViewHolder(v) {}
    }

    override fun onBindHeaderViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val textView = viewHolder.itemView.findViewById<TextView>(R.id.stick_text)
        textView.text = generateHeaderId(position).toString()
    }

    override fun newFooterHolder(view: View) = ViewHolder(view)

    // 目前只支持关键词，之后加更多
    fun doSearch(keywords: String, tags: List<String>?) {
        val keywordSet = keywords.split(' ').toHashSet()
        val result = ArrayList<NewsExt>()
        for (x in curNews) {
            for (kw in x.news.keywords) {
                if (keywordSet.contains(kw.word)) {
                    result.add(x)
                }
            }
        }
        setSearch(result)
    }

    // 用于add到最新或者搜索结果，不需要保存，也不需要查重
    private fun add1(news: NewsExt, toKind: Array<ArrayList<NewsExt>>) {
        val cat = ALL_CATEGORY.indexOf(news.news.category)
        if (cat != -1) {
            toKind[cat].add(news)
            toKind[ALL_IDX].add(news)
        }
    }

    // 用于add到收藏或已读，需要保存，也需要查重
    // which = READ_IDX或者FAVORITE_IDX
    fun add(news: NewsExt, which: Int) {
        val cat = ALL_CATEGORY.indexOf(news.news.category)
        val toKind = allNews[which]
        if (cat != -1 && !toKind[ALL_IDX].contains(news)) {
            toKind[cat].add(news)
            toKind[ALL_IDX].add(news)
        }
        storeToFile()
    }


    fun setSearch(newsList: List<NewsExt>) {
        prevKindId = curKindId
        curKindId = SEARCH_IDX
        val searchNews = allNews[SEARCH_IDX]
        for (x in searchNews) {
            x.clear()
        }
        for (news in newsList) {
            add1(news, searchNews)
        }
        notifyDataSetChanged()
    }

    fun finishSearch() {
        curKindId = prevKindId
        notifyDataSetChanged()
    }

    inline fun <R : Comparable<R>> sortBy(crossinline selector: (News) -> R?) {
        curNews.sortBy { selector(it.news) }
        notifyDataSetChanged()
        if (needStore()) {
            storeToFile()
        }
    }

    fun setCurKind(name: CharSequence) {
        curKindId = ALL_KIND.indexOf(name)
        notifyDataSetChanged()
    }

    fun setCurCategory(name: CharSequence) {
        curCategoryId = ALL_CATEGORY.indexOf(name)
        notifyDataSetChanged()
    }

    // 在这些页面下的删除/排序...操作是否需要保存
    fun needStore(): Boolean = curKindId == FAVORITE_IDX || curKindId == READ_IDX

    // 只保存"收藏"和"已读"新闻中的"全部"新闻
    fun storeToFile() {
        val parcel = Parcel.obtain()
        parcel.writeTypedList(allNews[FAVORITE_IDX][ALL_IDX])
        parcel.writeTypedList(allNews[READ_IDX][ALL_IDX])
        val fo = activity.openFileOutput("$NEWS_FILE_NAME-$USERNAME", MODE_PRIVATE)
        CipherOutputStream(fo, CIPHER).use { it.write(parcel.marshall()) }
        parcel.recycle()
    }

    // 这里并不进行io，数据是在AccountManager中读到FILE_INPUT_XXX中的
    private fun loadFromFile() {
        allNews[FAVORITE_IDX][ALL_IDX] = FILE_INPUT_FAVORITE
        allNews[READ_IDX][ALL_IDX] = FILE_INPUT_READ
        allNews[FAVORITE_IDX].let {
            for (x in it[ALL_IDX]) {
                it[ALL_CATEGORY.indexOf(x.news.category)].add(x)
            }
        }
        allNews[FAVORITE_IDX].let {
            for (x in it[ALL_IDX]) {
                it[ALL_CATEGORY.indexOf(x.news.category)].add(x)
            }
        }
        notifyDataSetChanged()
    }

    // 没有保存操作
    fun doRemove(position: Int) {
        val news = getItem(position)
        when (curKindId) {
            FAVORITE_IDX -> news.favorite = false
            READ_IDX -> news.read = false
        }
        val curKind = allNews[curKindId]
        when (curCategoryId) {
            ALL_IDX -> {
                val cat = ALL_CATEGORY.indexOf(news.news.category)
                val position1 = curKind[cat].indexOfFirst { it === news } // 要求引用相等，当时就是这样放进来的
                curKind[cat].removeAt(position1)
            }
            else -> {
                val position1 = curKind[ALL_IDX].indexOfFirst { it === news }
                curKind[ALL_IDX].removeAt(position1)
            }
        }
        notifyDataSetChanged()
    }

    fun clear() {
        while (curNews.isNotEmpty()) {
            doRemove(curNews.size - 1)
        }
        if (needStore()) {
            storeToFile()
        }
    }

    fun getItem(position: Int) = curNews[if (hasHeaderView()) position - 1 else position]

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsExt = getItem(position)
        val news = getItem(position).news
        with(holder) {
            title.text = news.title
            content.text = resources.getString(R.string.news_preview_content, news.content)
            category.text = resources.getString(R.string.news_preview_category, news.category)
            publishTime.text = resources.getString(R.string.news_preview_publish_time, news.publishTime)
            publisher.text = resources.getString(R.string.news_preview_publisher, news.publisher)
            val curTimeTag = this@NewsAdapter.timeTag++ // 这个是在主线程，不必担心
            timeTag = curTimeTag
            if (news.imageList.isEmpty()) {
                image.setImageBitmap(emptyImage)
            } else {
                newsExt.downloadImage(0) { result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.e("my", result.getException().toString())
                        }
                        is Result.Success -> {
                            if (timeTag == curTimeTag) { // 这个也是在主线程，不必担心
                                image.setImageBitmap(scale(result.get()))
                            }
                        }
                    }
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<Any>(itemView), View.OnClickListener,
        View.OnLongClickListener {
        val title: TextView = itemView.findViewById(R.id.text_view_title)
        val content: TextView = itemView.findViewById(R.id.text_view_text)
        val category: TextView = itemView.findViewById(R.id.text_view_category)
        val publishTime: TextView = itemView.findViewById(R.id.text_view_publish_time)
        val publisher: TextView = itemView.findViewById(R.id.text_view_publisher)
        val image: ImageView = itemView.findViewById(R.id.image_view)
        val emptyImage: Bitmap by lazy(LazyThreadSafetyMode.PUBLICATION) {
            scale(BitmapFactory.decodeResource(resources, R.drawable.no_image_available))
        }

        // 图片是异步加载的，而ViewHolder是循环使用的，所以可能先翻倒的图片在被刷掉之后再显示出来，然后又被后翻倒的图片替代，影响体验
        // 用一个时间戳来确定图片的先后
        var timeTag = 0

        private val screenWidth: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
            val outMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            outMetrics.widthPixels
        }

        fun scale(old: Bitmap): Bitmap {
            val resultWidth = screenWidth / 3
            val scale = resultWidth.toFloat() / old.width
            return Bitmap.createScaledBitmap(old, resultWidth, (old.height * scale).toInt(), true)
        }

        init {
            val maxWidth = screenWidth / 3 * 2
            title.width = maxWidth
            content.width = maxWidth
            category.width = maxWidth
            publishTime.width = maxWidth
            publisher.width = maxWidth
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            val selected = getItem(adapterPosition)
            NEWS_ACTIVITY_INTENT_ARG = selected
            selected.read = true
            add(selected, READ_IDX)
            val intent = Intent(activity, NewsActivity::class.java)
            activity.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            val menu = PopupMenu(activity, v)
            menu.menuInflater.inflate(R.menu.list_item_options, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.favorite -> {
                        val news = curNews[adapterPosition]
                        news.favorite = true
                        add(news, FAVORITE_IDX)
                    }
                    R.id.delete -> {
                        doRemove(adapterPosition)
                        if (needStore()) {
                            storeToFile()
                        }
                    }
                    R.id.share -> {
                    } // todo
                }
                true
            }
            menu.show()
            return true
        }
    }
}