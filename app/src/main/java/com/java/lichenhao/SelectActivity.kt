package com.java.lichenhao

import android.app.DatePickerDialog
import android.content.Context
import kotlinx.android.synthetic.main.content_select.*

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import java.util.*

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.net.URLEncoder
import java.text.SimpleDateFormat


const val BASE_URL = "https://api2.newsminer.net/svc/news/queryNewsList?"
const val CHARSET = "UTF-8"

data class Query(
    val size: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val words: String? = null,
    val categories: String? = null
)

class SelectActivity : AppCompatActivity() {

    var listItems = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null
    val searchHistory_sharePreferenceName = "searchHistory"
    var MAX_HISTORY = 7

    fun setStartDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) - 1
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

    override fun onDestroy() {
        var index: Int = 0
        val his = getSharedPreferences(searchHistory_sharePreferenceName, Context.MODE_PRIVATE)
        val ed = his.edit()
        while (index < listItems.size) {
            ed.putString(USERNAME + index.toString(), listItems[index])
            ++index
        }
        ed.commit()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.content_select)

        setMoreOption()
        setStartDate()
        setEndDate()
//        setSearchNews()
        val his = getSharedPreferences(searchHistory_sharePreferenceName, Context.MODE_PRIVATE)
        var index: Int = 0
        while (index < MAX_HISTORY) {
            if (!(his.getString(USERNAME + index.toString(), "")).equals("")) {
                listItems.add(his.getString(USERNAME + index.toString(), ""))
            }
            ++index
        }

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listItems
        )
        searchHistory.adapter = adapter

        dislike.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                searchNews.setQuery(searchNews.query, true)
                true
            } else false
        }
        searchHistory.setOnItemClickListener { adapterView, view, i, l ->
            val touch = adapterView.getItemAtPosition(i) as String
            searchNews.setQuery(touch, false)
        }

        searchNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(q: String): Boolean {
                var flag = true
                for (str in listItems) {
                    if (str.equals(q)) {
                        flag = false
                        break
                    }
                }
                if (flag) {
                    listItems.add(0, q)
                }
                if (listItems.size > MAX_HISTORY) {
                    listItems.removeAt(MAX_HISTORY)
                }
                adapter?.notifyDataSetChanged()
                val query = Query(
                    size = 15,
                    startDate = startDate.text.toString(),
                    endDate = endDate.text.toString(),
                    words = q,
                    categories = categories.selectedItem.toString()
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
                            Toast.makeText(
                                this@SelectActivity,
                                "${resources.getString(R.string.network_err)}: $ex", Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Result.Success -> {
                            val data = result.get()
                            val dislikeKw = dislike.text.split(' ').toHashSet()
                            val filteredNews =
                                data.data.filterNot { it.keywords.any { kw -> dislikeKw.contains(kw.word) } }
                                    .toTypedArray()
                            val filteredData = data.copy(data = filteredNews)
                            intent.putExtra("SelectActivityResult", filteredData)
                            setResult(42, intent)
                            finish()
                        }
                    }
                }
                return false
            }

        })
    }
}