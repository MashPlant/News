package com.java.lichenhao

import android.app.DatePickerDialog
import android.net.sip.SipSession
import kotlinx.android.synthetic.main.content_select.*

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
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
    val startDate: String? = null,
    val endDate: String? = null,
    val words: String? = null,
    val categories: String? = null
)

class SelectActivity : AppCompatActivity() {

    val query: Query? = null

    fun setStartDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) - 1
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)

//        Log.e("year", "" + year)
//        Log.e("month", "" + month)
//        Log.e("day", "" + day)

        val y = year.toString()
        var m = month.toString()
        var d = day.toString()
        if (month < 10) {
            m = "0" + m
        }
        if (day < 10) {
            d = "0" + d
        }
        startDate.setText(y + "-" + m + "-" + d)

        startDate.setOnClickListener {
            var dateList: List<String> = startDate.getText().split('-')
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val y = year.toString()
                var m = (monthOfYear + 1).toString()
                var d = dayOfMonth.toString()

                if (monthOfYear < 10) {
                    m = "0" + m
                }
                if (dayOfMonth < 10) {
                    d = "0" + d
                }
                year.toString()
                startDate.setText(y + "-" + m + "-" + d)
            }, dateList[0].toInt(), dateList[1].toInt() - 1, dateList[2].toInt())
            dpd.show()
        }
    }

    fun setEndDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)

        val y = year.toString()
        var m = month.toString()
        var d = day.toString()
        if (month < 10) {
            m = "0" + m
        }
        if (day < 10) {
            d = "0" + d
        }
        endDate.setText(y + "-" + m + "-" + d)

        endDate.setOnClickListener {
            var dateList: List<String> = endDate.getText().split('-')
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val y = year.toString()
                var m = (monthOfYear + 1).toString()
                var d = dayOfMonth.toString()

                Log.e("year", y)
                Log.e("month", m)
                Log.e("day", d)

                if (monthOfYear < 10) {
                    m = "0" + m
                }
                if (dayOfMonth < 10) {
                    d = "0" + d
                }
                year.toString()
                endDate.setText(y + "-" + m + "-" + d)
            }, dateList[0].toInt(), dateList[1].toInt() - 1, dateList[2].toInt())
            dpd.show()
        }
    }

    fun setMoreOption() {
        moreOption.setOnClickListener {
            if (searchOption.visibility == View.VISIBLE) {
                searchOption.visibility = View.GONE
                moreOption.setText("更多选项")
            } else {
                searchOption.visibility = View.VISIBLE
                moreOption.setText("收起")
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.content_select)

        setMoreOption()
        setStartDate()
        setEndDate()

        searchNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(q: String): Boolean {
                var query = Query(
                    size = 15,
                    startDate = startDate.getText().toString(),
                    endDate = endDate.getText().toString(),
                    words = q,
                    categories = categories.getSelectedItem().toString()
                )
                var url = BASE_URL
                query.size?.let { url += "size=" + URLEncoder.encode(it.toString(), CHARSET) + "&" }
                url += "startDate=" + URLEncoder.encode(query.startDate, CHARSET) + "&"
                url += "endDate=" + URLEncoder.encode(query.endDate, CHARSET) + "&"
                query.words?.let { url += "words=" + URLEncoder.encode(it, CHARSET) + "&" }
                if (!query.categories.equals("全部")) {
                    url += "categories=" + URLEncoder.encode(query.categories, CHARSET) + "&"
                }
                Log.e("URL", url)
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