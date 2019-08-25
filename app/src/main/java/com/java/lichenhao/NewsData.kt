package com.java.lichenhao

import android.content.Context
import android.os.Parcel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

const val LATEST_IDX = 0
const val FAVORITE_IDX = 1
const val READ_IDX = 2
const val SEARCH_IDX = 3
const val RECOMMEND_IDX = 4

const val ALL_IDX = 0

// AccountManager.initAdapterGlobals中初始化
lateinit var ALL_KIND: Array<String>
lateinit var ALL_CATEGORY: Array<String>

// 这些都在SplashActivity/AccountManager中接受用户输入初始化
lateinit var USERNAME: String
// 只用来加密
lateinit var CIPHER: Cipher

const val NEWS_FILE_NAME = "News"

const val GET_COUNT = 100
const val RECOMMEND_COUNT = 200

object NewsData {
    val allNews = Array(ALL_KIND.size) { Array(ALL_CATEGORY.size) { ArrayList<NewsExt>() } }

    // 保证"已读"和"收藏"不重复
    private val newsSet = Array(2) { HashSet<News>() }

    var curKindId = LATEST_IDX

    // -1表示无效，即不存在prevKind
    var prevKindId = -1

    var curCategoryId = ALL_IDX

    val curNews
        inline get() = allNews[curKindId][curCategoryId]

    inline fun refresh(
        crossinline errorHandler: (error: FuelError) -> Unit,
        crossinline finishHandler: () -> Unit
    ) {
        when (curKindId) {
            LATEST_IDX -> {
                val c = Calendar.getInstance()
                val url =
                    "${BASE_URL}endDate=${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}&size=$GET_COUNT"
                url.httpGet().responseObject<Response> { _, _, result ->
                    when (result) {
                        is Result.Failure -> errorHandler(result.error)
                        is Result.Success -> {
                            val latestKind = allNews[LATEST_IDX]
                            latestKind.forEach(ArrayList<NewsExt>::clear)
                            for (news in result.value.data) {
                                addInternal(NewsExt(news), latestKind)
                            }
                        }
                    }
                    finishHandler()
                }
            }
            RECOMMEND_IDX -> {
                doAsync {
                    val recommendKind = allNews[RECOMMEND_IDX]
                    recommendKind.forEach(ArrayList<NewsExt>::clear)
                    val kwScoreCount = HashMap<String, Float>()
                    var sum = 0F
                    for (news in allNews[READ_IDX][ALL_IDX]) {
                        for (kw in news.news.keywords) {
                            val old = kwScoreCount[kw.word] ?: 0F
                            kwScoreCount[kw.word] = old + kw.score
                            sum += kw.score
                        }
                    }
                    val url = BASE_URL + "startDate=1926-08-17&endDate=2926-08-17"
                    val results = ArrayList<News>(RECOMMEND_COUNT)
                    kwScoreCount
                        .map { (k, v) -> Pair(k, ((v / sum) * RECOMMEND_COUNT).toInt()) }
                        .sortedBy { it.second }
                        .forEach { (kw, cnt) ->
                            when (val result =
                                "$url&words=$kw&size=$cnt".httpGet().responseObject<Response>().third) {
                                is Result.Failure -> errorHandler(result.error)
                                is Result.Success -> {
                                    for (news in result.value.data) {
                                        results.add(news)
                                    }
                                }
                            }
                        }
                    results.distinct()
                        .forEach { addInternal(NewsExt(it), recommendKind) }
                    uiThread {
                        finishHandler()
                    }
                }
            }
            else -> finishHandler()
        }
    }

    // 方便起见，由调用者来判断curKindId == LATEST_IDX
    inline fun loadMore(
        crossinline errorHandler: (error: FuelError) -> Unit,
        crossinline finishHandler: () -> Unit
    ) {
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
                is Result.Failure -> errorHandler(result.error)
                is Result.Success -> {
                    for (news in result.value.data) {
                        addInternal(NewsExt(news), latestKind)
                    }
                }
            }
            finishHandler()
        }
    }

    // 这里并不进行io，数据是在AccountManager中读到FILE_INPUT_XXX中的
    fun loadFromFile(decrypt: Cipher) {
        val fi = GLOBAL_CONTEXT.openFileInput("$NEWS_FILE_NAME-$USERNAME")
        val bytes = CipherInputStream(fi, decrypt).use { it.readBytes() }
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        parcel.readTypedList(allNews[FAVORITE_IDX][ALL_IDX], ParcelHelper.NEWS_EXT_CREATOR)
        parcel.readTypedList(allNews[READ_IDX][ALL_IDX], ParcelHelper.NEWS_EXT_CREATOR)
        parcel.recycle()
        for (i in FAVORITE_IDX..READ_IDX) {
            allNews[i].let {
                val set = newsSet[i - FAVORITE_IDX]
                for (x in it[ALL_IDX]) {
                    it[ALL_CATEGORY.indexOf(x.news.category)].add(x)
                    set.add(x.news)
                }
            }
        }
    }

    fun doSearch(keywords: String) {
        if (prevKindId == -1) {
            prevKindId = curKindId
        }
        val keywordSet = keywords.split(' ').toHashSet()
        val result = ArrayList<NewsExt>()
        for (x in allNews[prevKindId][curCategoryId]) {
            for (kw in x.news.keywords) {
                if (keywordSet.contains(kw.word)) {
                    result.add(x)
                }
            }
        }
        setSearch(result)
    }

    fun setSearch(newsList: List<NewsExt>) {
        if (prevKindId == -1) {
            prevKindId = curKindId
        }
        curKindId = SEARCH_IDX
        val searchNews = allNews[SEARCH_IDX]
        searchNews.forEach(ArrayList<NewsExt>::clear)
        for (news in newsList) {
            addInternal(news, searchNews)
        }
    }

    fun finishSearch() {
        curKindId = prevKindId
        prevKindId = -1
    }

    fun addInternal(news: NewsExt, toKind: Array<ArrayList<NewsExt>>) {
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
        val toSet = newsSet[which - FAVORITE_IDX]
        if (cat != -1 && !toSet.contains(news.news)) {
            toKind[cat].add(news)
            toKind[ALL_IDX].add(news)
            toSet.add(news.news)
        }
        storeToFile()
    }

    fun isFavorite(news: News) = newsSet[FAVORITE_IDX - FAVORITE_IDX].contains(news)

    fun isRead(news: News) = newsSet[READ_IDX - FAVORITE_IDX].contains(news)

    // 在这些页面下的删除/排序...操作是否需要保存
    private fun needStore(): Boolean = curKindId == FAVORITE_IDX || curKindId == READ_IDX

    // 只保存"收藏"和"已读"新闻中的"全部"新闻
    private fun storeToFile() {
        doAsync {
            val parcel = Parcel.obtain()
            parcel.writeTypedList(allNews[FAVORITE_IDX][ALL_IDX])
            parcel.writeTypedList(allNews[READ_IDX][ALL_IDX])
            val fo = GLOBAL_CONTEXT.openFileOutput("$NEWS_FILE_NAME-$USERNAME", Context.MODE_PRIVATE)
            CipherOutputStream(fo, CIPHER).use { it.write(parcel.marshall()) }
            parcel.recycle()
        }
    }

    fun maybeStoreToFile() {
        if (needStore()) {
            storeToFile()
        }
    }

    // 没有保存
    private fun removeInternal(position: Int) {
        val news = curNews[position]
        curNews.removeAt(position)
        when (curKindId) {
            FAVORITE_IDX -> {
                news.favorite = false
                newsSet[FAVORITE_IDX - FAVORITE_IDX].remove(news.news)
            }
            READ_IDX -> {
                news.read = false
                newsSet[READ_IDX - FAVORITE_IDX].remove(news.news)
            }
        }
        val curKind = allNews[curKindId]
        when (curCategoryId) {
            ALL_IDX -> {
                val cat = ALL_CATEGORY.indexOf(news.news.category)
                val position1 = curKind[cat].indexOfFirst { it == news }
                curKind[cat].removeAt(position1)
            }
            else -> {
                val position1 = curKind[ALL_IDX].indexOfFirst { it == news }
                curKind[ALL_IDX].removeAt(position1)
            }
        }
    }

    fun remove(position: Int) {
        removeInternal(position)
        maybeStoreToFile()
    }

    fun clear() {
        while (curNews.isNotEmpty()) {
            removeInternal(curNews.size - 1)
        }
        maybeStoreToFile()
    }
}