package com.sh3h.loadmore.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sh3h.loadmore.R;
import com.sh3h.loadmore.listener.OnPullLoadMoreTrigger;
import com.sh3h.loadmore.listener.OnPullTrigger;

/**
 * Created by dengzhimin on 2017/5/25.
 */

public class FooterView extends FrameLayout implements OnPullTrigger, OnPullLoadMoreTrigger {

    private static final String TAG = FooterView.class.getSimpleName();

    private TextView tvLoadMore;
    private ImageView ivSuccess;
    private ProgressBar progressBar;

    public FooterView(@NonNull Context context) {
        this(context, null);
    }

    public FooterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvLoadMore = (TextView) findViewById(R.id.tvLoadMore);
        ivSuccess = (ImageView) findViewById(R.id.ivSuccess);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    public void onPrepare() {
        Log.i(TAG, "onPrepare");
        ivSuccess.setVisibility(GONE);
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onMove(int y, boolean isComplete) {
        Log.i(TAG, "onMove");
        ivSuccess.setVisibility(GONE);
        progressBar.setVisibility(GONE);
        if (y >= getMeasuredHeight()) {
            tvLoadMore.setText(getResources().getString(R.string.text_release_to_load));
        } else {
            tvLoadMore.setText(getResources().getString(R.string.text_pull_up_to_load));
        }
    }

    @Override
    public void onRelease() {
        Log.i(TAG, "onRelease");
    }

    @Override
    public void onComplete() {
        Log.i(TAG, "onComplete");
        tvLoadMore.setText(getResources().getString(R.string.text_loaded));
        progressBar.setVisibility(GONE);
        ivSuccess.setVisibility(VISIBLE);
    }

    @Override
    public void onReset() {
        Log.i(TAG, "onReset");
        ivSuccess.setVisibility(GONE);
    }

    @Override
    public void onLoadMore() {
        Log.i(TAG, "onLoadMore");
        tvLoadMore.setText(getResources().getString(R.string.text_loading));
        progressBar.setVisibility(VISIBLE);
    }
}
