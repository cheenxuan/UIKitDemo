package com.tech.android.base.uikitdemo.recyclerview

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.umpay.linkageguest.R


class RecyclerViewDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_demo)

        findViewById<Button>(R.id.button).setOnClickListener {
            startActivity(Intent(this@RecyclerViewDemoActivity, List1Activity::class.java))
        }

        findViewById<Button>(R.id.page_grid).setOnClickListener {
            startActivity(Intent(this@RecyclerViewDemoActivity, PageGridLayoutActivity::class.java))
        }
    }
}