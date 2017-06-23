package com.example.winner.loopbanner;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringDef;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kad.banner.entity.AbstractPagerData;
import com.kad.banner.listener.OnBannerClickListener;
import com.kad.banner.view.LoopBanner;

import java.util.ArrayList;

public class RecyclerListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    static final int REFRESH_COMPLETE = 0X1112;
    SwipeRefreshLayout mSwipeLayout;
    RecyclerView recyclerView;
    LoopBanner banner;
    String[] images, titles;
    ArrayList<StringData> datas = new ArrayList<>();
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    images = getResources().getStringArray(R.array.url2);
                    setDataList();
                    Toast.makeText(RecyclerListActivity.this, ""+datas.size(), Toast.LENGTH_SHORT).show();
                    banner.notifyData(datas);
                    mSwipeLayout.setRefreshing(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_list);
        images = getResources().getStringArray(R.array.url);
        titles = getResources().getStringArray(R.array.title);
        setDataList();
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeLayout.setOnRefreshListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BaseRecyclerAdapter adapter = new BaseRecyclerAdapter<>(new SampleAdapter());

        /**
         * 将banner添加到recyclerView头部
         */
        View header = LayoutInflater.from(this).inflate(R.layout.header, null);

        banner = (LoopBanner) header.findViewById(R.id.banner);
        banner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
        adapter.addHeader(banner);
        recyclerView.setAdapter(adapter);

        banner.notifyData(datas);
        banner.setOnBannerClickListener(new OnBannerClickListener() {
            @Override
            public void OnBannerClick(AbstractPagerData data) {
                Toast.makeText(RecyclerListActivity.this, "" + data.getImageUrl(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataList() {
        datas.clear();
        int length = images.length;
        int temp = (int) (length / (Math.random() * 5));
        int size = temp <= length ? temp : length;

        for (int i = 0; i < size; i++) {
            StringData pagerData = new StringData();
            pagerData.setImageUrl(images[i]);
            datas.add(pagerData);
        }
    }


    //如果你需要考虑更好的体验，可以这么操作
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("--", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("--", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (banner != null) {
            banner.stopAutoPlay();
        }
    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 500);
    }
}
