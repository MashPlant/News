package com.java.lichenhao

import android.util.Log
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat


data class Response(
    val pageSize: Int, //新闻总页数
    val total: Int, //符合条件的新闻总数
    val `data`: List<News>,
    val currentPage: Int
) : Serializable

data class News(
    @SerializedName("image")
    val imageString: String, //新闻插图，可能为空(非空时格式是正常的json数组)
    val publishTime: String,  //新闻发布时间，部分新闻由于自身错误可能时间会很大(如9102年)
    val keywords: List<Keyword>, //关键词
    val language: String, //新闻语言
    val video: String, //视频，一般为空
    val title: String, //新闻题目
    val `when`: List<When>, //新闻中相关时间和相关度
    val content: String, //正文
    val persons: List<Person>, //新闻提及人物，提及次数和在xlore中知识卡片url
    val newsID: String, //新闻ID
    val crawlTime: String, //爬取时间
    val organizations: List<Organization>, //发布新闻组织
    val publisher: String, //出版者
    val locations: List<Location>, //新闻提及位置，位置经纬度，提及次数
    val `where`: List<Where>, //新闻相关位置和相关度
    val category: String, //类别
    val who: List<Who> //新闻相关人和相关度
) : Serializable {
    @Transient
    private var _imageList: List<String>? = null

    val imageList: List<String>
        get() {
            if (_imageList == null) {
                _imageList = imageString
                    .replace("[", "").replace("]", "")
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
            return _imageList!!
        }

    val preview
        get(): NewsPreview {
            Log.e("my", "image: \"$imageString\" $imageList")
            return NewsPreview(
                title = this.title,
                image = if (imageList!!.isNotEmpty()) imageList!![0] else null,
                content = this.content, // todo: shorten
                publishTime = this.publishTime,
                category = this.category
            )
        }
}

data class Keyword(
    val score: Double, //关建度
    val word: String //词语
) : Serializable

data class When(
    val score: Double,
    val word: String
) : Serializable

data class Person(
    val count: Int,
    val linkedURL: String,
    val mention: String
) : Serializable

data class Organization(
    val count: Int,
    val linkedURL: String,
    val mention: String
) : Serializable

data class Location(
    val count: Int,
    val lat: Double,
    val linkedURL: String,
    val lng: Double,
    val mention: String
) : Serializable

data class Where(
    val score: Int,
    val word: String
) : Serializable

data class Who(
    val score: Double,
    val word: String
) : Serializable

class NewsPreview(// using public field just for convenience
    val title: String,
    val content: String,
    val image: String?,
    val publishTime: String,
    val category: String
) : Serializable