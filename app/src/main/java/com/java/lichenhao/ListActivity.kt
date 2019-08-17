package com.java.lichenhao


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import co.lujun.androidtagview.TagView
import kotlinx.android.synthetic.main.activity_list.*
import java.io.IOException

class ListActivity : AppCompatActivity() {
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var newsAdapter: NewsAdapter

    private lateinit var searchView: SearchView
    private lateinit var searchItem: MenuItem

    private var searching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        android.util.Log.e("fuck", "fuck")
        setContentView(R.layout.activity_list)

        setSupportActionBar(toolbar)

//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            R.menu.activity_list.nightMode .setTitle("夜间模式（关）")
//        } else {
//            R.id.nightMode.setTitle("夜间模式（开）")
//        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        layoutManager = LinearLayoutManager(this)
        ultimate_recycler_view.layoutManager = layoutManager
        newsAdapter = NewsAdapter(this)
        try {
            newsAdapter.loadFromFile()
        } catch (e: IOException) {
            // 正常，应该是第一次创建文件不存在
            Log.e("fuck", "newsAdapter.loadFromFile failed: $e")
        }
        ultimate_recycler_view.setAdapter(newsAdapter)
//        this.newsAdapter.updateGroupNotesList()
        this.enableRefresh()

        new_news_button.setOnClickListener {
            startActivityForResult(Intent(this@ListActivity, SelectActivity::class.java), 42)
        }
        setNavigationView()
        initDrawerToggle()
    }

    // options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // search
        this.menuInflater.inflate(R.menu.activity_list, menu)
        searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        this.configureSearchView()

        // launch from short cut
        if (intent.hasExtra("SEARCH")) {
            searchView.onActionViewExpanded()
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            menu.findItem(R.id.nightMode).setTitle("夜间模式（开）")
        } else {
            menu.findItem(R.id.nightMode).setTitle("夜间模式（关）")
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42) {
            val response = data!!.getParcelableExtra("SelectActivityResult") as Response
            newsAdapter.addAll(response.data)
        }
    }

    fun switchNightMode() {
//        Handler().postDelayed({ recreate() }, 100)
//                    finish()
//                    Handler().postDelayed({ recreate() }, 100)
//                    startActivity(mintent)
        startActivity(Intent(this, this.javaClass))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                newsAdapter.clear()
            }
            R.id.sort_title -> {
                item.isChecked = true
                newsAdapter.sortBy { it.title }
            }

            R.id.sort_publish_time -> {
                item.isChecked = true
                newsAdapter.sortBy { it.publishTime }
            }

            R.id.nightMode -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//                    item.isChecked = false
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    item.setTitle("夜间模式（关）")
//                    val mintent = intent
                    switchNightMode()
                } else {
//                    item.isChecked = true
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    item.setTitle("夜间模式（开）")
//                    val mintent = intent
                    Handler().postDelayed({ recreate() }, 100)
                  switchNightMode()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onSaveInstanceState(outState: Bundle?) {
//        super.onSaveInstanceState(outState)
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
//        super.onRestoreInstanceState(savedInstanceState)
//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            R.id.nightMode.setTitle("夜间模式（关）")
//        } else {
//            R.id.nightMode.setTitle("夜间模式（开）")
//        }
//    }

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
        nav_view.itemIconTintList = null

        for (i in 1 until ALL_CATEGORY.size) {
            menu.add(ALL_CATEGORY[i])
        }

        nav_view.setNavigationItemSelectedListener {
            newsAdapter.setCurCategory(it.title)
            true
        }
    }

    override fun onBackPressed() {
        if (searching) {
            tag_group_manager!!.hide()
//            newsAdapter.updateGroupNotesList()
            searching = false
            searchView.onActionViewCollapsed()
        } else { // 会退出该页面
            super.onBackPressed()
        }
    }

    // search
    private fun configureSearchView() {
        searchView.queryHint = resources.getString(R.string.list_search_hint)
        searchView.isSubmitButtonEnabled = true

        // open searchView
        searchView.setOnSearchClickListener {
            tag_group_manager.show()
            searching = true
        }

        // close searchView
        searchView.setOnCloseListener {
            tag_group_manager.hide()
            newsAdapter.finishSearch()
//            newsAdapter.updateGroupNotesList()
            searching = false
            false
        }

        // search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                performSearch(query, tag_group_manager)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                performSearch(newText, tag_group_manager)
                return true
            }
        })

        // click tag
        tag_group_manager.setOnTagClickListener(object : TagView.OnTagClickListener {
            override fun onTagClick(position: Int, text: String) {
                tag_group_manager!!.switchCheckedState(position)
                performSearch(searchView.query.toString(), tag_group_manager)
            }

            override fun onTagLongClick(position: Int, text: String) {
                // do nothing
            }

            override fun onTagCrossClick(position: Int) {
                // do nothing
            }
        })
    }

    private fun performSearch(query: String, tag_group_manager: TagManager) {
        val tags = tag_group_manager.checkedTags
        newsAdapter.doSearch(query, tags)
    }

    private fun enableRefresh() {
        ultimate_recycler_view.setDefaultOnRefreshListener {
            Handler().postDelayed({
                if (searching)
                    performSearch(searchView.query.toString(), tag_group_manager!!)
//                else
//                    newsAdapter.updateGroupNotesList()
                ultimate_recycler_view.setRefreshing(false)
                layoutManager!!.scrollToPosition(0)
            }, 500)
        }
    }
}


// todo: wtf
//    override fun onResume() {
//        newsAdapter.updateGroupNotesList()
//        super.onResume()
//    }