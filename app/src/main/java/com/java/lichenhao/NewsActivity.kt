package com.java.lichenhao

import kotlinx.android.synthetic.main.activity_editor.*

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import java.util.Objects

class NewsActivity : AppCompatActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_editor_viewonly, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun share() {
        // todo
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> finish()

            R.id.menu_save -> Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()

            R.id.viewonly_share, R.id.share -> share()

            R.id.viewonly_export, R.id.export -> { // todo
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        setSupportActionBar(findViewById(R.id.toolbar))
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    companion object {
        const val VIEW_ONLY = "VIEW_ONLY"
        const val INITIAL_NOTE = "INITIAL_NOTE"
    }
}
