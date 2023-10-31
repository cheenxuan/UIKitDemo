package com.tech.android.base.uikitdemo.recyclerview;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.umpay.linkageguest.R;
import com.tech.android.base.uikitdemo.recyclerview.bean.GlideBean;
import com.tech.android.ui.recyclerviewkit.item.RvAdapter;
import com.tech.android.ui.recyclerviewkit.item.RvDataItem;
import com.tech.android.ui.recyclerviewkit.item.RvViewHolder;
import com.tech.android.ui.recyclerviewkit.pagegridlayout.PageGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class GlideActivity extends AppCompatActivity {
    private static final String TAG = "GlideActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        RecyclerView rv = findViewById(R.id.rv);
        TextView tvPagerIndex = findViewById(R.id.tvPagerIndex);
        TextView tvPagerCount = findViewById(R.id.tvPagerCount);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(10, 10, 10, 10);
            }
        });
        PageGridLayoutManager manager = new PageGridLayoutManager(2, 4, PageGridLayoutManager.HORIZONTAL);
//        MyLinearLayoutManager manager = new MyLinearLayoutManager(this);
        manager.setPageChangedListener(new PageGridLayoutManager.PageChangedListener() {
            @Override
            public void onPageCountChanged(int pagerCount) {
                Log.w(TAG, "onPagerCountChanged-pagerCount:" + pagerCount);
                tvPagerCount.setText(String.valueOf(pagerCount));
            }

            @Override
            public void onPageIndexSelected(int prePagerIndex, int currentPagerIndex) {
                tvPagerIndex.setText(currentPagerIndex == PageGridLayoutManager.NO_ITEM ? "-" : String.valueOf(currentPagerIndex + 1));
                Log.w(TAG, "onPagerIndexSelected-prePagerIndex " + prePagerIndex + ",currentPagerIndex:" + currentPagerIndex);
            }
        });

        rv.setLayoutManager(manager);
        RvAdapter adapter = new RvAdapter(this);
        rv.setAdapter(adapter);

        List<GlideBean> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(new GlideBean(String.valueOf(i)));
        }

        ArrayList<RvDataItem<GlideBean, RvViewHolder>> rvDataItems = new ArrayList<>();
        for (GlideBean glideBean : list) {
            rvDataItems.add(new GlideItem(glideBean));
        }

        adapter.addItems(rvDataItems, true);

    }

    public class GlideItem extends RvDataItem<GlideBean, RvViewHolder> {

        public GlideItem(GlideBean item) {
            super(item);
        }

        public void onBindData(RvViewHolder holder, int position) {
            TextView tv = holder.itemView.findViewById(R.id.tv);
            ImageView iv = holder.itemView.findViewById(R.id.iv);

            tv.setText(getMData().getTitle());

            Glide.with(iv)
                    .load(holder.getLayoutPosition() % 2 == 0 ? "https://img0.baidu.com/it/u=3339583410,2877781326&fm=253&fmt=auto&app=120&f=JPEG" : "https://img2.baidu.com/it/u=805055865,2254304384&fm=253&fmt=auto&app=120&f=JPEG")
                    .into(iv);
        }

        @Override
        public int getItemLayoutRes() {
            return R.layout.item_glide;
        }

        @Override
        public int getSpanSize() {
            return 1;
        }
    }
}