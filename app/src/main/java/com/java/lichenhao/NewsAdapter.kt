package com.java.lichenhao

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.success
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter


class NewsAdapter(private val activity: ListActivity, news_list: UltimateRecyclerView) :
    UltimateViewAdapter<NewsAdapter.ViewHolder>() {
    private var timeTag = 0 // 见ViewHolder.timeTag

    init {
        news_list.setAdapter(this)
        news_list.setDefaultOnRefreshListener {
            NewsData.refresh({ makeErrorToast(it) }) {
                news_list.setRefreshing(false)
                notifyDataSetChanged()
            }
        }
        news_list.reenableLoadmore()
        news_list.setOnLoadMoreListener { _, _ ->
            if (NewsData.curKindId == LATEST_IDX) {
                news_list.setRefreshing(true)
                NewsData.loadMore({ makeErrorToast(it) }) {
                    news_list.setRefreshing(false)
                    notifyDataSetChanged()
                }
            }
        }
    }

    private fun makeErrorToast(error: FuelError) {
        Toast.makeText(activity, "${activity.resources.getString(R.string.network_err)}: $error", Toast.LENGTH_SHORT)
            .show()
    }

    override fun getAdapterItemCount() = NewsData.curNews.size

    override fun generateHeaderId(position: Int) = NewsData.curNews[position].news.title[0].toLong()

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

    fun doSearch(keywords: String) {
        NewsData.doSearch(keywords)
        notifyDataSetChanged()
    }

    fun setSearch(newsList: List<NewsExt>) {
        NewsData.setSearch(newsList)
        notifyDataSetChanged()
    }

    fun finishSearch() {
        NewsData.finishSearch()
        notifyDataSetChanged()
    }

    fun clear() {
        NewsData.clear()
        notifyDataSetChanged()
    }

    inline fun <R : Comparable<R>> sortBy(crossinline selector: (News) -> R?, rev: Boolean = false) {
        if (rev) {
            NewsData.curNews.sortByDescending { selector(it.news) }
        } else {
            NewsData.curNews.sortBy { selector(it.news) }
        }
        NewsData.maybeStoreToFile()
        notifyDataSetChanged()
    }

    fun setCurKind(name: CharSequence) {
        NewsData.curKindId = ALL_KIND.indexOf(name)
        notifyDataSetChanged()
    }

    fun setCurCategory(name: CharSequence) {
        NewsData.curCategoryId = ALL_CATEGORY.indexOf(name)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsExt = NewsData.curNews[position]
        val news = NewsData.curNews[position].news
        with(holder) {
            if (newsExt.read) {
                layout.setBackgroundColor(if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) Color.DKGRAY else Color.LTGRAY)
            } else {
                layout.background = null
            }
            val r = resources
            title.text = news.title
            @SuppressLint
            content.text = r.getString(R.string.news_preview_content) + news.content
            @SuppressLint
            category.text = r.getString(R.string.news_preview_category) + news.category
            @SuppressLint
            publishTime.text = r.getString(R.string.news_preview_publish_time) + news.publishTime
            @SuppressLint
            publisher.text = r.getString(R.string.news_preview_publisher) + news.publisher +
                    if (newsExt.read) r.getString(R.string.read_string) else "" + if (newsExt.favorite) r.getString(R.string.favorite_string) else ""
            val curTimeTag = this@NewsAdapter.timeTag++ // 这个是在主线程，不必担心
            timeTag = curTimeTag
            if (news.imageList.isEmpty()) {
                image.setImageBitmap(emptyImage)
            } else {
                newsExt.downloadImage(0) { result ->
                    result.success {
                        if (timeTag == curTimeTag) { // 这个也是在主线程，不必担心
                            image.setImageBitmap(scale(it))
                        }
                    }
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<Any>(itemView), View.OnClickListener,
        View.OnLongClickListener {
        val layout: View = itemView
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
            val selected = NewsData.curNews[adapterPosition]
            NEWS_ACTIVITY_INTENT_ARG = selected
            NEWS_ACTIVITY_INTENT_ARG_ADAPTOR_POSITION = adapterPosition
            selected.read = true
            NewsData.add(selected, READ_IDX)
            notifyDataSetChanged()
            activity.startActivity(Intent(activity, NewsActivity::class.java))
        }

        override fun onLongClick(v: View): Boolean {
            val menu = PopupMenu(activity, v)
            menu.menuInflater.inflate(R.menu.list_item_options, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.favorite -> {
                        val news = NewsData.curNews[adapterPosition]
                        news.favorite = true
                        NewsData.add(news, FAVORITE_IDX)
                        notifyDataSetChanged()
                    }
                    R.id.delete -> {
                        NewsData.remove(adapterPosition)
                        notifyDataSetChanged()
                    }
                    R.id.shareText -> {
                        val news = NewsData.curNews[adapterPosition].news
                        val mIntent = Intent(Intent.ACTION_SEND)
                        mIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "标题：\n" + news.title + "\n" + "正文：\n" + news.content + "..."
                        )
                        mIntent.type = "text/plain"
                        activity.startActivity(Intent.createChooser(mIntent, "分享文本"))
                    }
                    R.id.shareImage -> {
                        val newsExt = NewsData.curNews[adapterPosition]
                        val mIntent = Intent(Intent.ACTION_SEND)
                        if (newsExt.imageDataList.isNotEmpty()) {
                            val byteArray = newsExt.imageDataList[0]
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
                            val path =
                                MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
                            val imageUri = Uri.parse(path)
                            mIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                            mIntent.type = "image/*"
                            activity.startActivity(Intent.createChooser(mIntent, "分享图片"))
                        } else {
                            Toast.makeText(activity, "这个新闻没有图片", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                true
            }
            menu.show()
            return true
        }
    }
}