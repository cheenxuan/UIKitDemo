package com.tech.android.base.uikitdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.tech.android.base.uikitdemo.banner.BannerInfo
import com.tech.android.ui.banner.*

class BannerDemoActivity : AppCompatActivity() {
    
    var banner: Banner? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner_test)

        banner = findViewById<Banner>(R.id.banner)

        val png1 = "https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF"
        val png2 = "https://t7.baidu.com/it/u=2529476510,3041785782&fm=193&f=GIF"
        val png3 = "https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF"
        //https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF
        //https://t7.baidu.com/it/u=2529476510,3041785782&fm=193&f=GIF
        //https://t7.baidu.com/it/u=727460147,2222092211&fm=193&f=GIF
        //https://t7.baidu.com/it/u=2511982910,2454873241&fm=193&f=GIF

        val data = ArrayList<BannerMo>()
        data.add(BannerInfo(png1, png1))
        data.add(BannerInfo(png2, png2))
        data.add(BannerInfo(png3, png3))
//        data.add(BannerInfo(png2, png2))
        
        banner?.setScrollDuration(1200)
        banner?.setAutoPlay(true)
        banner?.setOnBannerClickListener(object : IBanner.OnBannerClickListener{
            override fun onBannerClick(
                viewHolder: BannerAdapter.BannerViewHolder,
                bannerMo: BannerMo,
                position: Int, 
            ) {
                println(bannerMo.imgUrl)
                println(position)
            }
        })
        banner?.setBindAdapter(object : IBindAdapter {
            override fun onBind(
                viewHolder: BannerAdapter.BannerViewHolder,
                mo: BannerMo,
                position: Int,
            ) {
                val img = viewHolder.findViewById<ImageView>(com.tech.android.ui.banner.R.id.banner_img)
                Glide.with(this@BannerDemoActivity).load(mo.imgUrl).into(img!!)
            }
        })
        banner?.setBannerData(data)
        

    }

    override fun onStart() {
        super.onStart()
        println("-----------------onStart-------------------")
        banner?.startPlay()
    }

    override fun onPause() {
        super.onPause()
        println("-----------------onPause-------------------")
        banner?.stopPlay()
        
    }
}