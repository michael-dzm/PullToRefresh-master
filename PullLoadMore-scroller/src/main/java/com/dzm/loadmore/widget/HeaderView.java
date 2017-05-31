package com.dzm.loadmore.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dzm.loadmore.R;
import com.dzm.loadmore.listener.OnPullRefreshTrigger;
import com.dzm.loadmore.listener.OnPullTrigger;

/**
 * Created by dengzhimin on 2017/5/25.
 */

public class HeaderView extends FrameLayout implements OnPullTrigger, OnPullRefreshTrigger {

    private static final String TAG = HeaderView.class.getSimpleName();

    private ImageView ivArrow;//上下箭头

    private ImageView ivSuccess;//刷新成功图标

    private TextView tvRefresh;

    private ProgressBar progressBar;

    private Animation rotateUp;

    private Animation rotateDown;

    private boolean rotated = false;

    public HeaderView(@NonNull Context context) {
        this(context, null);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        rotateUp = AnimationUtils.loadAnimation(context, R.anim.rotate_up);
        rotateDown = AnimationUtils.loadAnimation(context, R.anim.rotate_down);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        tvRefresh = (TextView) findViewById(R.id.tvRefresh);
        ivArrow = (ImageView) findViewById(R.id.ivArrow);
        ivSuccess = (ImageView) findViewById(R.id.ivSuccess);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    public void onPrepare() {
        Log.i(TAG, "onPrepare");
    }

    @Override
    public void onMove(int y, boolean isComplete) {
        Log.i(TAG, "onMove");
        ivArrow.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        ivSuccess.setVisibility(GONE);
        if(-y > getMeasuredHeight()){
            tvRefresh.setText("松开刷新");
            if(!rotated){
                ivArrow.clearAnimation();
                ivArrow.startAnimation(rotateUp);
                rotated = true;
            }
        }
        if(-y < getMeasuredHeight()){
            tvRefresh.setText("下拉刷新");
            if(rotated){
                ivArrow.clearAnimation();
                ivArrow.startAnimation(rotateDown);
                rotated = false;
            }
        }
    }

    @Override
    public void onRelease() {
        Log.i(TAG, "onRelease");
    }

    @Override
    public void onComplete() {
        Log.i(TAG, "onComplete");
        ivSuccess.setVisibility(VISIBLE);
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onReset() {
        Log.i(TAG, "onReset");
        ivSuccess.setVisibility(GONE);
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh");
        ivSuccess.setVisibility(GONE);
        ivArrow.clearAnimation();
        ivArrow.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
        tvRefresh.setText("刷新中");
    }
}
