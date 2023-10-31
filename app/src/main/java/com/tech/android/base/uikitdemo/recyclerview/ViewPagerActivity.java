package com.tech.android.base.uikitdemo.recyclerview;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.umpay.linkageguest.R;
import com.tech.android.base.uikitdemo.recyclerview.adapter.ViewPagerFragmentAdapter;
import com.tech.android.base.uikitdemo.recyclerview.bean.MenuBean;


import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends AppCompatActivity {
    public static final String TAG = "TestActivity";
    private TabLayout tabLayout;
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        tabLayout = findViewById(R.id.tabLayout);
        vp = findViewById(R.id.vp);
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        vp.setAdapter(adapter);
        tabLayout.setupWithViewPager(vp);
        List<MenuBean> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new MenuBean("Menu" + (i + 1), i != 0));
        }
        adapter.setNewData(list);
    }
}