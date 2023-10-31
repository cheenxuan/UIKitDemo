package com.tech.android.base.uikitdemo.recyclerview

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.umpay.linkageguest.R

import com.tech.android.ui.recyclerviewkit.item.RvDataItem
import com.tech.android.ui.recyclerviewkit.item.RvViewHolder


class List1Activity : ListDemoActivity() {

    private var names: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        names = arrayListOf()

        updateUI()
        
        enableLoadMore {
            updateUI()
        }
    }

    private fun updateUI() {

        for (i in 1..21) {
            names?.add("测试数据$i")
        }


        val dataItems = mutableListOf<RvDataItem<*, *>>()
        names?.forEachIndexed { index, s ->
            dataItems.add(BankNameItem(s))
        }

        finishRefresh(dataItems)
    }

    override fun getToolBarTitle(): String? {
        return "开户行"
    }

    override fun enableRefresh(): Boolean {
        return true
    }

    override fun onRefresh() {
        super.onRefresh()
        names?.clear()
        updateUI()
    }

    inner class BankNameItem(val item: String) :
        RvDataItem<String, RvViewHolder>(item) {
        override fun onBindData(holder: RvViewHolder, position: Int) {
            val bankNameTv = holder.itemView.findViewById<TextView>(R.id.tv_bank_name)
            val lineView = holder.itemView.findViewById<View>(R.id.line)
            bankNameTv.setText(item)

            holder.view.setOnClickListener {
                val intent = Intent()
                intent.putExtra("bankName", item)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        override fun getItemLayoutRes(): Int {
            return R.layout.item_bank_agent
        }

    }
}
