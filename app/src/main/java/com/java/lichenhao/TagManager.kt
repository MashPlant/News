package com.java.lichenhao

import android.content.Context
import android.util.AttributeSet
import android.view.View

import java.util.ArrayList

import co.lujun.androidtagview.TagContainerLayout

class TagManager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    TagContainerLayout(context, attrs, defStyleAttr) {

    private var tagList: MutableList<String>? = null
    private var checked: BooleanArray = BooleanArray(0)

    // all checked
    val checkedTags: List<String>?
        get() {
            if (checked[checked.size - 1])
                return null

            val ret = ArrayList<String>()
            for (i in 0 until checked.size - 1)
                if (checked[i])
                    ret.add(tagList!![i])
            return ret
        }

    private fun setChecked(position: Int, checkedState: Boolean) {
        checked[position] = checkedState
        setCheckedTagView(position, checkedState)
    }

    private fun setCheckedTagView(position: Int, checkedState: Boolean) {
        val tagView = getTagView(position)
//        if (checkedState)
//            tagView.setTagBorderColor(resources.getColor(R.color.checked_color, null))
//        else
//            tagView.setTagBorderColor(resources.getColor(R.color.unchecked_color, null))
        tagView.invalidate()
    }

    fun switchCheckedState(position: Int) {
        setChecked(position, !checked[position])
        if (checked[checked.size - 1])
        // with checked tag-all
            if (position != checked.size - 1)
            // check other tag with checked tag-all
                setChecked(checked.size - 1, false) // uncheck tag-all
            else
            // check tag-all with checked tag-all
                for (i in 0 until checked.size - 1)
                // uncheck all other tags
                    if (checked[i])
                        setChecked(i, false)
    }

    private fun init() {
        // initialize
//        tagList = TableOperate.getInstance().getAllTags()
        checked = BooleanArray(tagList!!.size + 1) // initialized false (not checked)
        // the final tag: all checked
        tagList!!.add("all")
        checked[checked.size - 1] = true // initialized true
        // set tags
        this.tags = tagList
        for (i in checked.indices)
            setCheckedTagView(i, checked[i])
    }

    fun show() {
        init()
        this.visibility = View.VISIBLE
    }

    fun hide() {
        this.visibility = View.INVISIBLE
        this.removeAllViews()
    }
}
