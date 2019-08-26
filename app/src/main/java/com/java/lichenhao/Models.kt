package com.java.lichenhao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.success
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File

// 这些data class里使用List没有任何价值，直接用Array即可
// 警告的"建议实现equals/hashCode"不用理，没有实现的类都不会用到这些方法

@Parcelize // 这玩意真像过程宏啊
data class Response(
    val pageSize: Int, //新闻总页数
    val total: Int, //符合条件的新闻总数
    val `data`: Array<News>,
    val currentPage: Int
) : Parcelable

// 持久保存的内容
@Parcelize
data class NewsExt(
    val news: News,
    var read: Boolean, // 已读
    var favorite: Boolean,
    val imageDataList: Array<ByteArray?>, // 下载的图片具体内容，下标和news.imageList一一对应
    var videoPath: String? // 下载的视频直接保存到文件中
) : Parcelable {
    constructor(news: News) : this(
        news,
        NewsData.isRead(news),
        NewsData.isFavorite(news),
        arrayOfNulls(news.imageList.size),
        null
    )

    inline fun downloadImage(which: Int, crossinline handler: (Result<Bitmap, FuelError>) -> Unit) {
        imageDataList[which]?.let {
            handler(Result.success(BitmapFactory.decodeByteArray(it, 0, it.size)))
            return
        }
        news.imageList[which].httpGet().response { _, _, result ->
            result.map { BitmapFactory.decodeByteArray(it, 0, it.size) }.apply(handler)
            result.success { imageDataList[which] = it }
        }
    }

    // 调用者保证news.video非空
    // handler处理的是视频文件名(因为视频播放器库不接受视频内容，只接受url)
    inline fun downloadVideo(crossinline handler: (String) -> Unit) {
        val video = news.video!!
        val videoPath = videoPath ?: run {
            handler(video) // 如果没有本地下载，则直接播放video网址(这样用户不需要等待)
            val path = "${GLOBAL_CONTEXT.filesDir.absolutePath}/video${System.currentTimeMillis()}.mp4"
            video.httpDownload().fileDestination { _, _ -> File(path) }.response { _, _, _ -> } // 不阻塞，也不管它什么时候结束
            videoPath = path
            return
        }
        handler(videoPath) // 有本地下载，播放文件
    }

    override fun equals(other: Any?) = other is NewsExt && news == other.news

    override fun hashCode() = news.hashCode()
}

// imageString和video的"为空"，极大概率是空字符串，但也有可能是不存在
@Parcelize
data class News(
    @SerializedName("image")
    val imageString: String?, //新闻插图，可能为空(非空时格式是正常的json数组)
    val publishTime: String,  //新闻发布时间，部分新闻由于自身错误可能时间会很大(如9102年)
    val keywords: Array<Keyword>, //关键词
    val language: String, //新闻语言
    val video: String?, //视频，一般为空
    val title: String, //新闻题目
    val `when`: Array<When>, //新闻中相关时间和相关度
    val content: String, //正文
    val persons: Array<Person>, //新闻提及人物，提及次数和在xlore中知识卡片url
    val newsID: String, //新闻ID
    val crawlTime: String, //爬取时间
    val organizations: Array<Organization>, //发布新闻组织
    val publisher: String, //出版者
    val locations: Array<Location>, //新闻提及位置，位置经纬度，提及次数
    val `where`: Array<Where>, //新闻相关位置和相关度
    val category: String, //类别
    val who: Array<Who> //新闻相关人和相关度
) : Parcelable {
    @IgnoredOnParcel
    private var _imageList: List<String>? = null

    val imageList: List<String>
        get() = _imageList ?: run {
            val tmp = (imageString ?: "")
                .replace("[", "").replace("]", "")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            _imageList = tmp
            tmp
        }

    override fun equals(other: Any?) = other is News && newsID == other.newsID

    override fun hashCode() = newsID.hashCode()
}

@Parcelize
data class Keyword(
    val score: Float, //关建度
    val word: String //词语
) : Parcelable

@Parcelize
data class When(
    val score: Float,
    val word: String
) : Parcelable

@Parcelize
data class Person(
    val count: Int,
    val linkedURL: String?,
    val mention: String
) : Parcelable

@Parcelize
data class Organization(
    val count: Int,
    val linkedURL: String?,
    val mention: String
) : Parcelable

@Parcelize
data class Location(
    val count: Int,
    val lat: Float,
    val linkedURL: String?,
    val lng: Float,
    val mention: String
) : Parcelable

@Parcelize
data class Where(
    val score: Float,
    val word: String
) : Parcelable

@Parcelize
data class Who(
    val score: Float,
    val word: String
) : Parcelable