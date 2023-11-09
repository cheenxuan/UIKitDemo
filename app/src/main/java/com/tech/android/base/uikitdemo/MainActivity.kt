package com.tech.android.base.uikitdemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baidu.ocr.sdk.OCR
import com.baidu.ocr.sdk.OnResultListener
import com.baidu.ocr.sdk.exception.OCRError
import com.baidu.ocr.sdk.model.AccessToken
import com.tech.android.base.camerakit.CameraActivity.Companion.ID_CARD_RS
import com.tech.android.base.camerakit.CameraActivity.Companion.KEY_CONTENT_TYPE
import com.tech.android.base.camerakit.CameraActivity.Companion.KEY_OUTPUT_FILE_PATH
import com.tech.android.base.uikitdemo.camera.CommonCamera
import com.tech.android.base.uikitdemo.recyclerview.RecyclerViewDemoActivity
import com.tech.android.base.uikitdemo.update.UpdateActivity
import com.umpay.linkageguest.R


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println(android.os.Build::class.java.fields.map { "Build.${it.name} = ${it.get(it.name)}" }
            .joinToString("\n"))


        val bannerDemo = findViewById<Button>(R.id.banner_demo)
        bannerDemo.setOnClickListener {
            startActivity(Intent(this, BannerDemoActivity::class.java))
        }

        val recyclerViewDemo = findViewById<Button>(R.id.recycler_view_demo)
        recyclerViewDemo.setOnClickListener {
            startActivity(Intent(this@MainActivity, RecyclerViewDemoActivity::class.java))
        }

        val cameraDemo = findViewById<Button>(R.id.camera_demo)
        cameraDemo.setOnClickListener {


            
        }

        val updateDemo = findViewById<Button>(R.id.update_demo)
        updateDemo.setOnClickListener {
            Toast.makeText(this, "哈哈哈哈", Toast.LENGTH_SHORT).show()
        }
    }
}