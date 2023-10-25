package com.tech.android.base.uikitdemo.recyclerview

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.tech.android.base.uikitdemo.R
import com.tech.android.base.uikitdemo.recyclerview.bean.TestBean
import com.tech.android.ui.recyclerviewkit.item.RvAdapter
import com.tech.android.ui.recyclerviewkit.item.RvDataItem
import com.tech.android.ui.recyclerviewkit.item.RvViewHolder
import com.tech.android.ui.recyclerviewkit.pagegridlayout.PageGridLayoutManager

class PageGridLayoutActivity : AppCompatActivity() {

    private val TAG = "PageGridLayoutActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_grid_layout)
        val rgOrientation = findViewById<RadioGroup>(R.id.rgOrientation)
        val rgReverseLayout = findViewById<RadioGroup>(R.id.rgReverseLayout)
        val rbHorizontal = findViewById<RadioButton>(R.id.rbHorizontal)
        val rbVertical = findViewById<RadioButton>(R.id.rbVertical)
        val rbReverseTrue = findViewById<RadioButton>(R.id.rbReverseTrue)
        val etRows = findViewById<EditText>(R.id.etRows)
        val etColumns = findViewById<EditText>(R.id.etColumns)
        val etPosition = findViewById<EditText>(R.id.etPosition)
        val etPagerIndex = findViewById<EditText>(R.id.etPagerIndex)


        findViewById<View>(R.id.btnUseGlide).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    GlideActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.btnVp1).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    ViewPagerActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.btnVp2).setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    ViewPager2Activity::class.java
                )
            )
        }
        val rv = findViewById<RecyclerView>(R.id.rv)
        rv.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                outRect[20, 10, 20] = 10
            }
        })
        val tvPagerIndex = findViewById<TextView>(R.id.tvPagerIndex)
        val tvPagerCount = findViewById<TextView>(R.id.tvPagerCount)
        val layoutManager = PageGridLayoutManager(
            etRows.text.toString().toInt(), etColumns.text.toString().toInt(),
            if (rbHorizontal.isChecked) PageGridLayoutManager.HORIZONTAL else PageGridLayoutManager.VERTICAL
        )

        //是否反向布局

        //是否反向布局
        layoutManager.setReverseLayout(rbReverseTrue.isChecked)

        layoutManager.setPageChangedListener(object : PageGridLayoutManager.PageChangedListener {

            override fun onPageCountChanged(pageCount: Int) {
                Log.w(TAG, "onPagerCountChanged-pagerCount:$pageCount")
                tvPagerCount.text = pageCount.toString()
            }

            override fun onPageIndexSelected(prePageIndex: Int, currentPageIndex: Int) {
                tvPagerIndex.text =
                    if (currentPageIndex == PageGridLayoutManager.NO_ITEM) "-" else (currentPageIndex + 1).toString()
                Log.w(
                    TAG,
                    "onPagerIndexSelected-prePagerIndex $prePageIndex,currentPagerIndex:$currentPageIndex"
                )
            }
        })

        //设置滑动每像素需要花费的时间

        //设置滑动每像素需要花费的时间
        layoutManager.setMillisecondPreInch(100f)
        //设置最大滚动时间
        //设置最大滚动时间
        layoutManager.setMaxScrollOnFlingDuration(500)

        rv.layoutManager = layoutManager
        val adapter = RvAdapter(this)
        rv.adapter = adapter

        val dataItems = mutableListOf<RvDataItem<*, *>>()
        val list: MutableList<TestBean> = ArrayList<TestBean>()
        for (i in 0..49) {
            list.add(TestBean(i, i.toString()))
        }

        list.forEach { item ->
            dataItems.add(DefaultItem(item))
        }

        adapter.addItems(dataItems, true)


        findViewById<View>(R.id.btnSetRows).setOnClickListener { v: View? ->
            val string = etRows.text.toString()
            if (TextUtils.isEmpty(string)) {
                Toast.makeText(this, "行数不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            layoutManager.setRows(string.toInt())
        }
        rgOrientation.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            if (checkedId == R.id.rbHorizontal) {
                layoutManager.setOrientation(PageGridLayoutManager.HORIZONTAL)
            } else if (checkedId == R.id.rbVertical) {
                layoutManager.setOrientation(PageGridLayoutManager.VERTICAL)
            }
        }
        rgReverseLayout.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            if (checkedId == R.id.rbReverseTrue) {
                layoutManager.setReverseLayout(true)
            } else if (checkedId == R.id.rbReverseFalse) {
                layoutManager.setReverseLayout(false)
            }
        }
        findViewById<View>(R.id.btnSetColumns).setOnClickListener { v: View? ->
            val string = etColumns.text.toString()
            if (TextUtils.isEmpty(string)) {
                Toast.makeText(this, "列数不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            layoutManager.setColumns(string.toInt())
        }
        findViewById<View>(R.id.btnScrollToPosition).setOnClickListener { v: View? ->
            val string = etPosition.text.toString()
            if (TextUtils.isEmpty(string)) {
                Toast.makeText(this, "指定位置不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            rv.scrollToPosition(string.toInt())
        }
        findViewById<View>(R.id.btnSmoothScrollToPosition).setOnClickListener { v: View? ->
            val string = etPosition.text.toString()
            if (TextUtils.isEmpty(string)) {
                Toast.makeText(this, "指定位置不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            rv.smoothScrollToPosition(string.toInt())
        }
        findViewById<View>(R.id.btnScrollToPagerIndex).setOnClickListener { v: View? ->
            val string = etPagerIndex.text.toString()
            if (TextUtils.isEmpty(string)) {
                Toast.makeText(this, "指定页不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            layoutManager.scrollToPageIndex(string.toInt())
        }
        findViewById<View>(R.id.btnSmoothScrollToPagerIndex).setOnClickListener { v: View? ->
            val string = etPagerIndex.text.toString()
            if (TextUtils.isEmpty(string)) {
                Toast.makeText(this@PageGridLayoutActivity, "指定页不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            layoutManager.smoothScrollToPageIndex(string.toInt())
        }
        findViewById<View>(R.id.btnPrePager).setOnClickListener { v: View? -> layoutManager.scrollToPrePage() }
        findViewById<View>(R.id.btnNextPager).setOnClickListener { v: View? -> layoutManager.scrollToNextPage() }
        findViewById<View>(R.id.btnSmoothPrePager).setOnClickListener { v: View? -> layoutManager.smoothScrollToPrePage() }
        findViewById<View>(R.id.btnSmoothNextPager).setOnClickListener { v: View? -> layoutManager.smoothScrollToNextPage() }
        findViewById<View>(R.id.btnAddDataToStart).setOnClickListener { v: View? ->
            adapter.addItemAt(
                0,
                DefaultItem(TestBean(0, "A")), true
            )
        }
        findViewById<View>(R.id.btnAddDataToEnd).setOnClickListener { v: View? ->
            adapter.addItem(
                DefaultItem(TestBean(0, "Z")), true
            )
        }
        findViewById<View>(R.id.btnDeleteDataFromStart).setOnClickListener { v: View? ->
            adapter.removeItemAt(0)
        }
        findViewById<View>(R.id.btnDeleteDataFromEnd).setOnClickListener { v: View? ->
            adapter.removeItemAt(adapter.getOriginalItemSize() - 1)
        }
        findViewById<View>(R.id.btnUpdateFirstData).setOnClickListener { v: View? ->
            adapter.refreshItemAt(0, DefaultItem(TestBean(0, "我更新了")))
        }

    }

    inner class DefaultItem(val item: TestBean) : RvDataItem<TestBean, RvViewHolder>(item) {
        override fun onBindData(holder: RvViewHolder, position: Int) {
            val tvItem = holder.itemView.findViewById<TextView>(R.id.tvItem)
            tvItem.setText(item.name ?: "")

            if (position % 3 == 0) {
                tvItem.setTextColor(Color.RED)
            } else if (position % 3 == 1) {
                tvItem.setTextColor(Color.GREEN)
            } else if (position % 3 == 2) {
                tvItem.setTextColor(Color.YELLOW)
            } else {
                tvItem.setTextColor(Color.WHITE)
            }

            holder.view.setOnClickListener {
                Toast.makeText(
                    this@PageGridLayoutActivity,
                    "点击了位置：$position",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun getItemLayoutRes(): Int {
            return R.layout.item_default
        }

        override fun getSpanSize(): Int {
            return 1
        }
    }
}
