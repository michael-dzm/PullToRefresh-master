package com.sh3h.refresh.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.sh3h.loadmore.listener.OnPullListener;
import com.sh3h.loadmore.widget.PullLayout;
import com.sh3h.refresh.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dengzhimin on 2017/5/24.
 */

public class PullActivity extends AppCompatActivity {

    private PullLayout layout;

    private ListView target;

    private List<String> datas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pull);
        datas = new ArrayList<>();
        for (int i=0; i<5; i++){
            datas.add("数据" + i);
        }
        target = (ListView) findViewById(R.id.lv);
        target.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datas));

        layout = (PullLayout) findViewById(R.id.layout_scroller);
        layout.setPullListener(new OnPullListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        datas.add(0, "刷新数据");
                        ((BaseAdapter)target.getAdapter()).notifyDataSetChanged();
                        layout.setRefreshing(false);
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       datas.add("下拉数据");
                       ((BaseAdapter)target.getAdapter()).notifyDataSetChanged();
                       layout.setLoading(false);
                   }
               }, 2000);
            }
        });
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.setRefreshing(true);
            }
        });
    }
}
