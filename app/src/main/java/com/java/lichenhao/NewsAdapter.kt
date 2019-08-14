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
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpDownload
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
            setTitle(preview.title)
            setText(preview.content)
            setCreateDate(preview.publishTime)
            setGroup(preview.group)
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
        private val title: TextView
        private val text: TextView
        private val group: TextView
        private val publishTime: TextView
        private val image: ImageView

        private var screenWidth = -1
        private var emptyImage: Bitmap? = null

        private fun getScreenWidth(): Int {
            if (screenWidth != -1)
                return screenWidth
            val outMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            screenWidth = outMetrics.widthPixels
            return screenWidth
        }

        private fun getEmptyImage(): Bitmap? {
            if (emptyImage != null)
                return emptyImage
            val old = BitmapFactory.decodeResource(resources, R.drawable.no_image_available)
            val width = old.width
            val height = old.height
            val scale = getScreenWidth().toFloat() / 3 / width
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            emptyImage = Bitmap.createBitmap(old, 0, 0, width, height, matrix, true)
            return emptyImage
        }

        init {
            val textWidth = getScreenWidth() / 3 * 2
            // title & text
            this.title = itemView.findViewById(R.id.text_view_title)
            this.title.width = textWidth
            this.text = itemView.findViewById(R.id.text_view_text)
            this.text.width = textWidth
            // group the news belongs to
            this.group = itemView.findViewById(R.id.text_view_group)
            this.group.width = textWidth
            // create & modify time
            this.publishTime = itemView.findViewById(R.id.text_view_create_time)
            this.publishTime.width = textWidth
            // image
            this.image = itemView.findViewById(R.id.image_view)

            // click listener
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        fun setTitle(title: String) {
            if (title.isEmpty())
                this.title.text = resources.getString(R.string.news_preview_title)
            else
                this.title.text = title
        }

        fun setText(text: String) {
            if (text.isEmpty())
                this.text.text = resources.getString(R.string.news_preview_text)
            else
                this.text.text = text
        }

        fun setGroup(group: String?) {
            if (group == null) {
                this.group.visibility = View.GONE
            } else {
                this.group.text = resources.getString(R.string.news_preview_group, group)
                this.group.visibility = View.VISIBLE
            }
        }

        fun setCreateDate(date: String) {
            this.publishTime.text = resources.getString(R.string.news_preview_create_time, date)
        }

        fun setImage(imagePath: String?) {
            if (imagePath == null) {
                this.image.setImageBitmap(this.getEmptyImage())    //clear the previous image
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
                            val old = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            val resultWidth = getScreenWidth() / 3
                            val scale = resultWidth.toFloat() / old.width
                            val matrix = Matrix()
                            matrix.postScale(scale, scale)
                            image.setImageBitmap(Bitmap.createBitmap(old, 0, 0, old.height, old.height, matrix, true))
                        }
                    }
                }
            }
        }

        override fun onClick(v: View) {
            val selectedNews = getItem(adapterPosition)
            val intent = Intent(activity, NewsActivity::class.java)
            intent.putExtra(NewsActivity.INITIAL_NOTE, selectedNews)
            activity.startActivity(intent)
        }

        override fun onLongClick(v: View): Boolean {
            val menu = PopupMenu(activity, v)
            menu.menuInflater.inflate(R.menu.list_item_options, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                val selectedNews = getItem(adapterPosition)
                when (item.itemId) {
                    R.id.delete -> remove(adapterPosition)

                    R.id.preview -> activity.startActivity(
                        Intent(activity, NewsActivity::class.java)
                            .putExtra(NewsActivity.VIEW_ONLY, true)
                            .putExtra(NewsActivity.INITIAL_NOTE, selectedNews)
                    )
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