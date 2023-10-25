package com.tech.android.ui.recyclerviewkit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes


/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
open class RvEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private var icon: ImageView? = null
    private var title: TextView? = null
    private var button: Button? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER

        LayoutInflater.from(context).inflate(R.layout.rvkit_empty_view, this, true)

        icon = findViewById(R.id.empty_icon)
        title = findViewById(R.id.empty_text)
        button = findViewById(R.id.empty_action)
    }

    fun setIcon(@DrawableRes iconRes: Int) {
        icon!!.setImageDrawable(context.getDrawable(iconRes))
    }

    fun setText(text: String) {
        title!!.text = text
        title!!.visibility = if (TextUtils.isDigitsOnly(text)) View.GONE else View.VISIBLE
    }


    fun setButton(text: String, listener: OnClickListener? = null) {
        if (TextUtils.isDigitsOnly(text)) {
            button!!.visibility = View.GONE
        } else {
            button!!.visibility = View.VISIBLE
            button!!.setText(text)
            if (listener != null)
                button!!.setOnClickListener(listener)
        }
    }


}