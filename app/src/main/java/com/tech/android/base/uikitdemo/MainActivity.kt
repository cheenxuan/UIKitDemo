package com.tech.android.base.uikitdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tech.android.base.uikitdemo.camera.CoreCameraActivity
import com.tech.android.base.uikitdemo.recyclerview.RecyclerViewDemoActivity
import com.tech.android.base.uikitdemo.update.UpdateActivity
import com.tech.android.ui.camerakit.CameraActivity.Companion.ID_CARD_FRONT
import com.tech.android.ui.camerakit.CameraActivity.Companion.KEY_CONTENT_TYPE
import com.tech.android.ui.camerakit.CameraActivity.Companion.KEY_OUTPUT_FILE_PATH
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
            val intent = Intent(this@MainActivity, CoreCameraActivity::class.java)
            val bundle = Bundle()
            bundle.putString(KEY_CONTENT_TYPE, ID_CARD_FRONT)
            bundle.putString(
                KEY_OUTPUT_FILE_PATH,
                applicationContext.filesDir.absolutePath + "/pic_aaa.jpg"
            )
            intent.putExtras(bundle)
            startActivity(intent)
        }

        val updateDemo = findViewById<Button>(R.id.update_demo)
        updateDemo.setOnClickListener {
            startActivity(Intent(this@MainActivity, UpdateActivity::class.java))
        }
    }
}