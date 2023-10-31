package com.tech.android.base.uikitdemo.recyclerview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.umpay.linkageguest.R;
import com.tech.android.base.uikitdemo.recyclerview.adapter.ViewPager2FragmentAdapter;
import com.tech.android.base.uikitdemo.recyclerview.bean.MenuBean;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ShenBen
 */
public class ViewPager2Activity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private ViewPager2 vp;
    private TabLayout tabLayout;
    private TabLayoutMediator mediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager2);
        tabLayout = findViewById(R.id.tabLayout);
        vp = findViewById(R.id.vp);
        ViewPager2FragmentAdapter adapter = new ViewPager2FragmentAdapter(this);
        vp.setAdapter(adapter);

        mediator = new TabLayoutMediator(tabLayout, vp, (tab, position) -> tab.setText(adapter.getItem(position).getTitle()));
        mediator.attach();

        List<MenuBean> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new MenuBean("Menu" + (i + 1), i != 0));
        }
        adapter.setNewData(list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediator.detach();
    }
}