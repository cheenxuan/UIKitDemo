package com.tech.android.base.uikitdemo.recyclerview.fragment;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.umpay.linkageguest.R;
import com.tech.android.base.uikitdemo.recyclerview.bean.TestBean;
import com.tech.android.ui.recyclerviewkit.item.RvAdapter;
import com.tech.android.ui.recyclerviewkit.item.RvDataItem;
import com.tech.android.ui.recyclerviewkit.item.RvViewHolder;
import com.tech.android.ui.recyclerviewkit.pagegridlayout.PageGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class ViewPager2Fragment extends Fragment {
    private static final String TAG = "ViewPager2Fragment";

    public static ViewPager2Fragment newInstance() {
        ViewPager2Fragment fragment = new ViewPager2Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_pager2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv = view.findViewById(R.id.rv);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(10, 10, 10, 10);
            }
        });
        TextView tvPagerIndex = view.findViewById(R.id.tvPagerIndex);
        TextView tvPagerCount = view.findViewById(R.id.tvPagerCount);
        final PageGridLayoutManager layoutManager = new PageGridLayoutManager(3, 3, PageGridLayoutManager.HORIZONTAL);
        layoutManager.setPageChangedListener(new PageGridLayoutManager.PageChangedListener() {
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

        /*
         是否启用处理滑动冲突滑动冲突，default: true；若不需要库中自带的处理方式，则置为false，自行处理。
         setHandlingSlidingConflictsEnabled() 必须要在{@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)} 之前调用，否则无效
         you must call this method before {@link RecyclerView#setLayoutManager(RecyclerView.LayoutManager)}
        */
        layoutManager.setHandlingSlidingConflictsEnabled(true);
        rv.setLayoutManager(layoutManager);
        RvAdapter adapter = new RvAdapter(this.getContext());
        rv.setAdapter(adapter);

        List<TestBean> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(new TestBean(i, String.valueOf(i)));
        }
        ArrayList<RvDataItem<TestBean, RvViewHolder>> dataItems = new ArrayList<>();
        for (TestBean testBean : list) {
            dataItems.add(new DefaultItem(testBean));
        }

        adapter.addItems(dataItems, true);
    }

    public class DefaultItem extends RvDataItem<TestBean, RvViewHolder> {

        public DefaultItem(TestBean item) {
            super(item);
        }

        public void onBindData(RvViewHolder holder, int position) {
            TextView tvItem = holder.itemView.findViewById(R.id.tvItem);

            tvItem.setText(getMData().getName());

            if (position % 3 == 0) {
                tvItem.setTextColor(Color.RED);
            } else if (position % 3 == 1) {
                tvItem.setTextColor(Color.GREEN);
            } else if (position % 3 == 2) {
                tvItem.setTextColor(Color.YELLOW);
            } else {
                tvItem.setTextColor(Color.WHITE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),"点击了位置："+position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemLayoutRes() {
            return R.layout.item_default;
        }

        @Override
        public int getSpanSize() {
            return 1;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.w(TAG, "onDestroyView: ");
    }
}