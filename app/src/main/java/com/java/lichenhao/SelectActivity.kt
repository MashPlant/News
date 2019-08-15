package com.java.lichenhao

import android.net.sip.SipSession
import kotlinx.android.synthetic.main.content_select.*

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import java.util.*

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.content_select.view.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.GregorianCalendar

const val BASE_URL = "https://api2.newsminer.net/svc/news/queryNewsList?"
const val CHARSET = "UTF-8"
val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")

data class Query(
    val size: Int? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val words: String? = null,
    val categories: String? = null
)

class SelectActivity : AppCompatActivity() {

    val query: Query? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_select)

        moreOption.setOnClickListener {
            if (searchOption.visibility == View.VISIBLE) {
                searchOption.visibility = View.GONE
            } else {
                searchOption.visibility = View.VISIBLE
            }

        }

        searchNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                val query = Query(
                    size = 15,
                    startDate = GregorianCalendar(2019, 7 - 1, 1).time,
                    endDate = GregorianCalendar(2019, 7 - 1, 3).time,
                    words = "特朗普",
                    categories = "科技"
                )
                var url = BASE_URL
                query.size?.let { url += "size=" + URLEncoder.encode(it.toString(), CHARSET) + "&" }
                query.startDate?.let { url += "startDate=" + URLEncoder.encode(DATE_FORMAT.format(it), CHARSET) + "&" }
                query.endDate?.let { url += "endDate=" + URLEncoder.encode(DATE_FORMAT.format(it), CHARSET) + "&" }
                query.words?.let { url += "words=" + URLEncoder.encode(it, CHARSET) + "&" }
                query.categories?.let { url += "categories=" + URLEncoder.encode(it, CHARSET) + "&" }
                url.httpGet().responseObject<Response> { _, _, result ->
                    Log.e("my", "enter responseObject")
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            Log.e("my", ex.toString())
                        }
                        is Result.Success -> {
                            val data = result.get()
                            Log.e("my", data.toString())
                            intent.putExtra("SelectActivityResult", data)
                            setResult(42, intent)
                            finish()
                        }
                    }
                }.join()
                return false
            }

        })
    }
}