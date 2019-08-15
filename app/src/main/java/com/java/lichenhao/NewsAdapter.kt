package com.java.lichenhao

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter

// 最新和收藏两个界面下不能有加号
// 最新和搜索结果不需要保存，搜索结果是下标0，不显示出来
val ALL_CATEGORY = arrayOf("", "最新", "收藏", "全部主题", "娱乐", "军事", "教育", "文化", "健康", "财经", "体育", "汽车", "科技", "社会")

class NewsAdapter(private val activity: ListActivity) : UltimateViewAdapter<NewsAdapter.ViewHolder>() {
    private val allCategory: Array<ArrayList<News>> = Array(ALL_CATEGORY.size + 1) { ArrayList<News>() }
    private var curCategoryId: Int = 0
    // 搜索的时候设置prevCategoryId = curCategoryId，搜索结束后设置回来
    private var prevCategoryId: Int = 0

    private val curCategory
        inline get() = allCategory[curCategoryId]

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preview = getItem(position).preview
        with(holder) {
            title.text = preview.title
            content.text = preview.content
            category.text = resources.getString(R.string.news_preview_category, preview.category)
            publishTime.text = resources.getString(R.string.news_preview_create_time, preview.publishTime)
            setImage(preview.image)
        }
    }

    override fun getAdapterItemCount() = curCategory.size

    override fun generateHeaderId(position: Int) = getItem(position).preview.title[0].toLong()

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
        prevCategoryId = curCategoryId
        curCategoryId = 0
        val keywordSet = keywords.split(' ')
        for (x in allCategory[prevCategoryId]) {
            for (kw in x.keywords) {
                if (keywordSet.contains(kw.word)) {
                    curCategory.add(x)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun finishSearch() {
        curCategoryId = prevCategoryId
    }

    fun <R : Comparable<R>> sortBy(selector: (News) -> R?) {
        curCategory.sortBy(selector)
        notifyDataSetChanged()
        // save()
    }

    fun setCurCategory(name: CharSequence) {
        curCategoryId = ALL_CATEGORY.indexOf(name)
        notifyDataSetChanged()
    }

    fun add(news: News) = insertInternal(curCategory, news, curCategory.size)

    fun remove(position: Int) = removeInternal(curCategory, position)

    fun clear() = clearInternal(curCategory)

    fun getItem(position: Int) = curCategory[if (hasHeaderView()) position - 1 else position]

    inner class ViewHolder(itemView: View) : UltimateRecyclerviewViewHolder<Any>(itemView), View.OnClickListener,
        View.OnLongClickListener {
        internal val title: TextView = itemView.findViewById(R.id.text_view_title)
        internal val content: TextView = itemView.findViewById(R.id.text_view_text)
        internal val category: TextView = itemView.findViewById(R.id.text_view_category)
        internal val publishTime: TextView = itemView.findViewById(R.id.text_view_create_time)
        private val image: ImageView = itemView.findViewById(R.id.image_view)
        private val emptyImage: Bitmap by lazy(LazyThreadSafetyMode.PUBLICATION) {
            scale(BitmapFactory.decodeResource(resources, R.drawable.no_image_available))
        }
        private val screenWidth: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
            val outMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            outMetrics.widthPixels
        }

        private fun scale(old: Bitmap): Bitmap {
            val resultWidth = screenWidth / 3
            val scale = resultWidth.toFloat() / old.width
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            return Bitmap.createBitmap(old, 0, 0, old.width, old.height, matrix, true)
        }

        init {
            val textWidth = screenWidth / 3 * 2
            title.width = textWidth
            content.width = textWidth
            category.width = textWidth
            publishTime.width = textWidth
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun setImage(imagePath: String?) {
            if (imagePath == null) {
                this.image.setImageBitmap(emptyImage)
            } else {
                Log.e("my", "imagePath = \"$imagePath\"")
                imagePath.httpGet().response { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            Log.e("my", ex.toString())
                        }
                        is Result.Success -> {
                            val byteArray = result.get()
                            image.setImageBitmap(scale(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)))
                        }
                    }
                }
            }
        }

        override fun onClick(v: View) {
            val selectedNews = getItem(adapterPosition)
            val intent = Intent(activity, NewsActivity::class.java)
                .putExtra(NewsActivity.NEWS_CONTENT, selectedNews)
            activity.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            val menu = PopupMenu(activity, v)
            menu.menuInflater.inflate(R.menu.list_item_options, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.favorite -> {
                    } // todo
                    R.id.delete -> remove(adapterPosition)
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


// Group news
//    fun updateGroupNewsList() {
//        this.updateList(TableOperate.getInstance().getAllNews(currentGroup, null))
//    }

//    fun getCurrentGroup(): String? {
//        return this.currentGroup
//    }
//
//    fun setCurrentGroup(groupName: String) {
//        this.currentGroup = groupName
//        updateGroupNewsList()
//    }


//    val curCategoryName
//        get() = ALL_CATEGORY[curCategoryId]