package com.tech.android.ui.recyclerviewkit.item

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/***
 * @auther: QinjianXuan
 * @date  : 2023/10/17 .
 * <P>
 * Description:
 * <P>
 */
open class RvViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val containerView: View
        get() = view
    
    private var viewCache = SparseArray<View>()
    fun <T : View> findViewById(viewId: Int): T? {
        var view = viewCache.get(viewId)
        if (view == null) {
            view = itemView.findViewById<T>(viewId)
            viewCache.put(viewId, view)
        }
        return view as? T
    }

}