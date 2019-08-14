package com.java.lichenhao

import kotlinx.android.synthetic.main.activity_list.*

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem


import co.lujun.androidtagview.TagView

class ListActivity : AppCompatActivity() {
    private var layoutManager: LinearLayoutManager? = null
    private lateinit var newsAdapter: NewsAdapter
//    private lateinit var groupMenu: SubMenu

    private lateinit var searchView: SearchView
    private lateinit var searchItem: MenuItem

    private var searching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        Log.d("my", "1")

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        layoutManager = LinearLayoutManager(this)
        ultimate_recycler_view.layoutManager = layoutManager
        newsAdapter = NewsAdapter(this)
        ultimate_recycler_view.setAdapter(newsAdapter)
//        this.newsAdapter.updateGroupNotesList()
        Log.d("my", "2")
        this.enableRefresh()

        new_news_button.setOnClickListener {
            startActivityForResult(Intent(this@ListActivity, SelectActivity::class.java), 42)
        }
        Log.d("my", "3")
        setNavigationView()
        initDrawerToggle()
        Log.d("my", "4")
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

        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 42) {
            Log.e("my", data!!.extras.toString())
            val response = data!!.getSerializableExtra("SelectActivityResult") as Response
            for (x in response.data) {
                newsAdapter.add(x)
            }
        }
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


    // navigation view
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
        } else
            super.onBackPressed()
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

    // refresh the list
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