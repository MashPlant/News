package com.java.lichenhao


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView

import kotlinx.android.synthetic.main.activity_list.*
import android.database.MatrixCursor
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.provider.BaseColumns
import android.view.*
import android.database.Cursor
import android.view.View.OnLongClickListener
import android.support.v4.view.MenuItemCompat.getActionView
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner


const val HISTORY_FILENAME = "History"

// AccountManager.initGlobals中初始化
var HISTORY = ArrayList<String>()

val REMOVED_CAT = ArrayList<Pair<CharSequence, Int>>()

class ListActivity : AppCompatActivity() {
    private lateinit var newsAdapter: NewsAdapter

    private var prevCheckKind = R.id.nav_kind0
    private var prevCheckCategory = R.id.nav_category0

    private lateinit var kindMenu: SubMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        val layoutManager = LinearLayoutManager(this)
        news_list.layoutManager = layoutManager
        newsAdapter = NewsAdapter(this, news_list)

        new_news_button.setOnClickListener {
            startActivityForResult(Intent(this@ListActivity, SelectActivity::class.java), 42)
        }
        setNavigationView()
        initDrawerToggle()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_list, menu)
//        val sv = menu.findItem(R.id.action_search).actionView as SearchView
//        sv.queryHint = resources.getString(R.string.list_search_hint)
//        sv.isSubmitButtonEnabled = true
//
//        sv.setOnCloseListener {
//            newsAdapter.finishSearch()
//            kindMenu.findItem(R.id.nav_kind0).isChecked = true // 回到最初的分类：最新(避免麻烦，懒得记录搜索前的分类)
//            kindMenu.findItem(R.id.nav_kind3).isChecked = false // 搜索结果一栏
//            prevCheckKind = R.id.nav_kind0
//            false
//        }
//        sv.findViewById<SearchView.SearchAutoComplete>(android.support.v7.appcompat.R.id.search_src_text).threshold = 0
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        sv.setSearchableInfo(searchManager.getSearchableInfo(componentName))
//        val coNames = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
//        val viewIds = intArrayOf(android.R.id.text1)
//        val adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, makeCursor(), coNames, viewIds)
//        sv.suggestionsAdapter = adapter
//
//
//        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                doSearch(query)
//                doAsync {
//                    HISTORY.add(0, query) // 逆序
//                    val parcel = Parcel.obtain()
//                    parcel.writeStringList(HISTORY)
//                    val fo = GLOBAL_CONTEXT.openFileOutput("$HISTORY_FILENAME-$USERNAME", Context.MODE_PRIVATE)
//                    CipherOutputStream(fo, CIPHER).use { it.write(parcel.marshall()) }
//                    parcel.recycle()
//                    uiThread {
//                        adapter.swapCursor(makeCursor())
//                    }
//                }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//                doSearch(newText)
//                return true
//            }
//        })

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            menu.findItem(R.id.nightMode).title = "夜间模式（开）"
            menu.findItem(R.id.menu_sort).icon = resources.getDrawable(R.drawable.baseline_sort_white_48dp, null)
        } else {
            menu.findItem(R.id.nightMode).title = "夜间模式（关）"
            menu.findItem(R.id.menu_sort).icon = resources.getDrawable(R.drawable.baseline_sort_grey_48dp, null)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        newsAdapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42 && data != null) {
            val response = data.getParcelableExtra<Response>("SelectActivityResult")
            kindMenu.findItem(prevCheckKind).isChecked = false
            kindMenu.findItem(R.id.nav_kind3).isChecked = true // 搜索结果一栏
            prevCheckKind = R.id.nav_kind3
            newsAdapter.setSearch(response.data.map { NewsExt(it) })
        }
    }

    private fun switchNightMode() {
        startActivity(Intent(this, this.javaClass))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> newsAdapter.clear()
            R.id.sort_title -> {
                item.isChecked = true
                newsAdapter.sortBy({ it.title })
            }
            R.id.sort_publish_time -> {
                item.isChecked = true
                newsAdapter.sortBy({ it.publishTime })
            }
            R.id.sort_title_rev -> {
                item.isChecked = true
                newsAdapter.sortBy({ it.title }, true)
            }
            R.id.sort_publish_time_rev -> {
                item.isChecked = true
                newsAdapter.sortBy({ it.publishTime }, true)
            }
            R.id.nightMode -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    item.title = "夜间模式（关）"
                    switchNightMode()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    item.title = "夜间模式（开）"
                    switchNightMode()
                }
            }
            R.id.add_category -> {
                val builder = AlertDialog.Builder(this)
                val spinner = Spinner(this)
                spinner.adapter =
                    ArrayAdapter(this, android.R.layout.simple_list_item_1, REMOVED_CAT.map { (title, _) -> title })
                builder.setView(spinner)
                    .setPositiveButton("确定") { _, _ ->
                        val title = spinner.selectedItem.toString()
                        val idx = REMOVED_CAT.indexOfFirst { it.first == title }
                        nav_view.menu.findItem(REMOVED_CAT[idx].second).isVisible = true
                        REMOVED_CAT.removeAt(idx)
                    }
                    .setNegativeButton("取消") { _, _ -> }
                builder.create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setTitle(title: CharSequence) {
        supportActionBar!!.title = title
        super.setTitle(title)
    }

    private fun initDrawerToggle() {
        val drawerToggle =
            ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerToggle.syncState()
    }

    // 侧边栏
    private fun setNavigationView() {
        val menu = nav_view.menu
        kindMenu = menu.getItem(0).subMenu
        val categoryMenu = menu.getItem(1).subMenu
        kindMenu.getItem(0).isChecked = true
        categoryMenu.getItem(0).let {
            it.isChecked = true
            toolbar.title = it.title
        }
        var lastClick = 0L
        nav_view.setNavigationItemSelectedListener {
            val cur = System.currentTimeMillis()
            val isDoubleClick = (cur - lastClick) < 500L
            lastClick = cur
            when (it.groupId) {
                R.id.nav_kind -> {
                    newsAdapter.setCurKind(it.title)
                    kindMenu.findItem(prevCheckKind).isChecked = false
                    prevCheckKind = it.itemId
                }
                R.id.nav_category -> {
                    if (isDoubleClick) {
                        val allString = resources.getString(R.string.nav_category0_string)
                        if (it.title != allString) {
                            it.isVisible = false
                            REMOVED_CAT.add(Pair(it.title, it.itemId))
                            if (it.isChecked) {
                                newsAdapter.setCurCategory(allString)
                                toolbar.title = allString
                                it.isChecked = false
                                prevCheckCategory = R.id.nav_category0
                            }
                        }
                    } else {
                        newsAdapter.setCurCategory(it.title)
                        toolbar.title = it.title
                        categoryMenu.findItem(prevCheckCategory).isChecked = false
                        prevCheckCategory = it.itemId
                    }
                }
            }
            it.isChecked = true
            true
        }
    }

    private fun doSearch(query: String) {
        newsAdapter.doSearch(query)
        kindMenu.findItem(prevCheckKind).isChecked = false
        kindMenu.findItem(R.id.nav_kind3).isChecked = true // 搜索结果一栏
        prevCheckKind = R.id.nav_kind3
    }
}

private fun makeCursor(): Cursor {
    val menuCols = arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1)
    val cursor = MatrixCursor(menuCols)
    for ((idx, history) in HISTORY.withIndex()) {
        cursor.addRow(arrayOf(idx, history))
    }
    return cursor
}