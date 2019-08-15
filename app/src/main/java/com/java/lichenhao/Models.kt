package com.java.lichenhao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import androidx.versionedparcelable.ParcelField
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import com.github.kittinunf.result.success
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

// 这些地方使用List没有任何价值，其实Array更好一些，只是data class对Array处理的不太好
// 所以就用ArrayList了，有些地方的确用到随机访问
@Parcelize // 这玩意真像过程宏啊
data class Response(
    val pageSize: Int, //新闻总页数
    val total: Int, //符合条件的新闻总数
    val `data`: ArrayList<News>,
    val currentPage: Int
) : Parcelable

// 持久保存的内容
@Parcelize
data class NewsExt(
    val news: News,
    val read: Boolean, // 已读
    val favorite: Boolean,
    val imageBitmapList: ArrayList<ByteArray?> // 下载的图片具体内容，下标和news.imageList一一对应
) : Parcelable {
    inline fun downloadImage(which: Int, crossinline handler: (Result<ByteArray, FuelError>) -> Unit) {
        // 处于某些未知的原因，这里不能直接利用之前的缓存
        // 否则NewsActivity中调用Bitmap.createScaledBitmap会返回null，然而文档里完全没有提到会有这种事，类型系统中也标注了Bitmap.createScaledBitmap不会返回null
        // 显然Java世界里也是有UB的，所有文档没有写出来的行为都是UB，在这一点上并没有比C++好
        imageBitmapList[which]?.let {
            handler(Result.success(it.copyOf()))
            return
        }
        news.imageList[which].httpGet().response { _, _, result ->
            when (result) {
                is Result.Success -> imageBitmapList[which] = result.get().copyOf()
            }
            handler(result)
        }
    }
}

@Parcelize
data class News(
    @SerializedName("image")
    val imageString: String, //新闻插图，可能为空(非空时格式是正常的json数组)
    val publishTime: String,  //新闻发布时间，部分新闻由于自身错误可能时间会很大(如9102年)
    val keywords: ArrayList<Keyword>, //关键词
    val language: String, //新闻语言
    val video: String, //视频，一般为空
    val title: String, //新闻题目
    val `when`: ArrayList<When>, //新闻中相关时间和相关度
    val content: String, //正文
    val persons: ArrayList<Person>, //新闻提及人物，提及次数和在xlore中知识卡片url
    val newsID: String, //新闻ID
    val crawlTime: String, //爬取时间
    val organizations: ArrayList<Organization>, //发布新闻组织
    val publisher: String, //出版者
    val locations: ArrayList<Location>, //新闻提及位置，位置经纬度，提及次数
    val `where`: ArrayList<Where>, //新闻相关位置和相关度
    val category: String, //类别
    val who: ArrayList<Who> //新闻相关人和相关度
) : Parcelable {
    @IgnoredOnParcel
    private var _imageList: List<String>? = null

    val imageList: List<String>
        get() = _imageList ?: run {
            val tmp = imageString
                .replace("[", "").replace("]", "")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            _imageList = tmp
            tmp
        }
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
    val linkedURL: String,
    val mention: String
) : Parcelable

@Parcelize
data class Organization(
    val count: Int,
    val linkedURL: String,
    val mention: String
) : Parcelable

@Parcelize
data class Location(
    val count: Int,
    val lat: Float,
    val linkedURL: String,
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