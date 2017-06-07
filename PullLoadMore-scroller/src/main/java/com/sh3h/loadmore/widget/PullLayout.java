package com.sh3h.loadmore.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Scroller;

import com.sh3h.loadmore.R;
import com.sh3h.loadmore.listener.OnPullListener;
import com.sh3h.loadmore.listener.OnPullLoadMoreTrigger;
import com.sh3h.loadmore.listener.OnPullRefreshTrigger;
import com.sh3h.loadmore.listener.OnPullTrigger;

/**
 * Created by dengzhimin on 2017/5/24.
 */

public class PullLayout extends ViewGroup {

    /**
     * 上拉加载状态 初始
     */
    public static final int STATUS_LOAD_DEFAULT = 0;
    /**
     * 上拉加载状态 移动
     */
    public static final int STATUS_LOAD_MOVE = 1;
    /**
     * 上拉加载状态 加载中
     */
    public static final int STATUS_LOADING = 3;
    /**
     * 上拉加载状态 加载完成
     */
    public static final int STATUS_LOADED = 4;
    /**
     * 下拉刷新状态 初始
     */
    public static final int STATUS_REFRESH_DEFAULT = 0;
    /**
     * 下拉刷新状态 移动
     */
    public static final int STATUS_REFRESH_MOVE = 1;
    /**
     * 下拉刷新状态 刷新中
     */
    public static final int STATUS_REFRESHING = 3;
    /**
     * 下拉刷新状态 刷新完成
     */
    public static final int STATUS_REFRESHED = 4;
    /**
     * 无效手势
     */
    private static final int INVALID_POINTER = -1;
    /**
     * 无效坐标
     */
    private static final int INVALID_COORDINATE = -1;
    /**
     * 无效偏移量
     */
    private static final int INVALID_OFFSET = -1;
    /**
     * 默认偏移比例
     */
    private static final float PULL_RATIO_DEFAULT = 0.5f;
    /**
     *
     */
    private View mHeaderView;
    /**
     *
     */
    private View mFooterView;
    /**
     *
     */
    private View mTargetView;
    /**
     * 头部高度
     */
    private int mHeaderHeight;
    /**
     * 底部高度
     */
    private int mFooterHeight;
    /**
     * 是否有头部控件
     */
    private boolean mHasHeaderView;
    /**
     * 是否有底部控件
     */
    private boolean mHasFooterView;

    /**
     * 是否可上拉加载
     */
    private boolean mLoadMoreEnable = true;
    /**
     * 是否可下拉刷新
     */
    private boolean mRefreshEnable = true;
    /**
     * 上拉加载和下拉刷新监听事件
     */
    private OnPullListener mOnPullListener;
    /**
     * 滚动操作 控制控件的移动
     */
    private Scroller mScroller;
    /**
     * 移动的最小移动像素数
     */
    private int mTouchSlop;
    /**
     * 记录最后一次手指在屏幕的Y坐标
     */
    private float mLastY;
    /**
     * 手指按下时的Y坐标
     */
    private float mDownY;
    /**
     * 偏移比例
     */
    private float mPullRatio = PULL_RATIO_DEFAULT;
    /**
     * 多点触控pointer索引
     */
    private int mActivePointerId;
    /**
     * 上拉加载状态码
     */
    private int mLoadMoreStatus = STATUS_LOAD_DEFAULT;
    /**
     * 下拉刷新状态码
     */
    private int mRefreshStatus = STATUS_REFRESH_DEFAULT;

    public PullLayout(Context context) {
        this(context, null);
    }

    public PullLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        mHeaderView = inflater.inflate(R.layout.header, null);
        mFooterView = inflater.inflate(R.layout.footer, null);
        MarginLayoutParams lp = new MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT);
        this.addView(mHeaderView, lp);
        this.addView(mFooterView, lp);
        TypedArray array = null;
        try {
            array = context.obtainStyledAttributes(attrs, R.styleable.PullLayout, defStyleAttr, 0);
            mPullRatio = array.getFloat(array.getIndex(R.styleable.PullLayout_pull_ratio), PULL_RATIO_DEFAULT);
            if(mPullRatio > 1) mPullRatio = PULL_RATIO_DEFAULT;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            array.recycle();
        }
        mScroller = new Scroller(context);
        // 获取TouchSlop值
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = getChildCount();
        if(childCount == 0){
            return;
        }
        if(childCount > 3){
            throw new IllegalStateException("PullLayout's children must no more than 1");
        }
        if(0 < childCount && childCount <= 3){
            for(int i=0; i<childCount; i++){
                final View view = getChildAt(i);
                if(view instanceof HeaderView || view instanceof FooterView){
                    continue;
                }
                mTargetView = view;
            }
        }
        mHasHeaderView = (mHeaderView != null);
        mHasFooterView = (mFooterView != null);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mHeaderView != null){
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
        if(mTargetView != null){
            measureChildWithMargins(mTargetView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        if(mFooterView != null){
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            mFooterHeight = mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        if(mHeaderView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderView.layout(paddingLeft + lp.leftMargin, paddingTop + lp.topMargin - mHeaderHeight,
                    paddingRight + lp.rightMargin + mHeaderView.getMeasuredWidth(), paddingBottom + lp.bottomMargin);
        }
        if(mTargetView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mTargetView.getLayoutParams();
            mTargetView.layout(paddingLeft + lp.leftMargin, paddingTop + lp.topMargin,
                    paddingRight + lp.rightMargin + mTargetView.getMeasuredWidth(), paddingBottom + lp.bottomMargin + mTargetView.getMeasuredHeight());
        }
        if(mFooterView != null){
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            mFooterView.layout(paddingLeft + lp.leftMargin, height - paddingBottom - lp.bottomMargin,
                    paddingRight + lp.rightMargin + mFooterView.getMeasuredWidth(), height - paddingBottom - lp.bottomMargin + mFooterHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //处理多点触控
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mDownY = mLastY = getMotionEventY(ev, mActivePointerId);
                break;
            case MotionEvent.ACTION_MOVE:
                if(mActivePointerId == INVALID_POINTER){
                    return false;
                }
                float y = getMotionEventY(ev, mActivePointerId);
                float offsetY = y - mDownY;
                mLastY = y;
                // 当手指拖动值大于TouchSlop值时，并且可以下拉刷新或者可以上拉加载 拦截子控件的事件
                if (Math.abs(offsetY) > mTouchSlop && ((mRefreshEnable && mHasHeaderView && offsetY > 0 && !checkRefreshable()) ||
                        (mLoadMoreEnable && mHasFooterView && offsetY < 0 && !checkLoadable()))) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mDownY = mLastY = getMotionEventY(ev, mActivePointerId);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        //上拉加载中或下拉刷新中不处理事件
        if(mLoadMoreStatus == STATUS_LOADING || mRefreshStatus == STATUS_REFRESHING){
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                downChanged();
                return true;
            case MotionEvent.ACTION_MOVE:
                float y = getMotionEventY(event, mActivePointerId);
                int offsetY = (int) (y - mLastY);
                mLastY = y;
                scrollBy(0, (int)(-offsetY * mPullRatio * (getMeasuredHeight() - Math.abs(getScrollY()))/getMeasuredHeight()));
                moveChanged(getScrollY());
                return true;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER;
                upChanged(getScrollY());
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                mDownY = mLastY = getMotionEventY(event, mActivePointerId);
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER;
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 多点触控获取Y坐标
     * @param event
     * @param activePointerId
     * @return
     */
    private float getMotionEventY(MotionEvent event, int activePointerId) {
        final int index = event.findPointerIndex(activePointerId);
        if (index < 0) {
            return INVALID_COORDINATE;
        }
        return event.getY(index);
    }

    /**
     * 多点触控获取X坐标
     * @param event
     * @param activePointerId
     * @return
     */
    private float getMotionEventX(MotionEvent event, int activePointerId) {
        final int index = event.findPointerIndex(activePointerId);
        if (index < 0) {
            return INVALID_COORDINATE;
        }
        return event.getX(index);
    }

    /**
     * 处理多点触控手指抬起时获取其他手指的索引
     * @param ev
     */
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * 拦截子控件事件之后
     * 父控件的ACTION_DOWN事件处理
     */
    private void downChanged(){
        mLoadMoreStatus = STATUS_LOAD_DEFAULT;
        mRefreshStatus = STATUS_REFRESH_DEFAULT;
        refreshChildrenView(INVALID_OFFSET);
    }

    /**
     * 拦截子控件事件之后
     * 父控件的ACTION_MOVE事件处理
     */
    private void moveChanged(int scrollY){
        if(scrollY > 0){// pull up
            mLoadMoreStatus = STATUS_LOAD_MOVE;
        }else{// pull down
            mRefreshStatus = STATUS_REFRESH_MOVE;
        }
        refreshChildrenView(scrollY);
    }

    /**
     * 拦截子控件事件之后
     * 父控件的ACTION_UP事件处理
     */
    public void upChanged(int offset){
        if(offset > 0){//上拉
            setLoading(offset > mFooterHeight);
        }else{//下拉
            setRefreshing(-offset > mHeaderHeight);
        }
    }

    /**
     * 更新子控件
     * @param scrollY 下拉或者上拉偏移量
     */
    private void refreshChildrenView(int scrollY){
        if(mFooterView instanceof OnPullTrigger && mFooterView instanceof OnPullLoadMoreTrigger){
            if(mLoadMoreStatus == STATUS_LOAD_DEFAULT){
                ((OnPullTrigger) mFooterView).onPrepare();
            }else if(mLoadMoreStatus == STATUS_LOAD_MOVE){
                ((OnPullTrigger) mFooterView).onMove(scrollY, false);
            }else if(mLoadMoreStatus == STATUS_LOADING){
                ((OnPullLoadMoreTrigger) mFooterView).onLoadMore();
                if(mOnPullListener != null) mOnPullListener.onLoadMore();
            }else if(mLoadMoreStatus == STATUS_LOADED){
                ((OnPullTrigger) mFooterView).onComplete();
            }
        }
        if(mHeaderView instanceof OnPullTrigger && mHeaderView instanceof OnPullRefreshTrigger){
            if(mRefreshStatus == STATUS_REFRESH_DEFAULT){
                ((OnPullTrigger) mHeaderView).onPrepare();
            }else if(mRefreshStatus == STATUS_REFRESH_MOVE){
                ((OnPullTrigger) mHeaderView).onMove(scrollY, false);
            }else if(mRefreshStatus == STATUS_REFRESHING){
                ((OnPullRefreshTrigger) mHeaderView).onRefresh();
                if(mOnPullListener != null) mOnPullListener.onRefresh();
            }else if(mLoadMoreStatus == STATUS_REFRESHED){
                ((OnPullTrigger) mHeaderView).onComplete();
            }
        }
    }

    public void setLoading(boolean isLoading){
        if(!mLoadMoreEnable || !mHasFooterView){
            return;
        }
        if(isLoading){
            mLoadMoreStatus = STATUS_LOADING;
            mScroller.startScroll(0, getScrollY(), 0, mFooterHeight - getScrollY());
        }else{
            if(mLoadMoreStatus == STATUS_LOADING){
                mLoadMoreStatus = STATUS_LOADED;
            }
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
        }
        refreshChildrenView(INVALID_OFFSET);
        invalidate();
    }

    public void setRefreshing(boolean isRefreshing){
        if(!mRefreshEnable || !mHasHeaderView){
            return;
        }
        if(isRefreshing){
            mRefreshStatus = STATUS_REFRESHING;
            mScroller.startScroll(0, getScrollY(), 0, -mHeaderHeight - getScrollY());
        }else{
            if(mRefreshStatus == STATUS_REFRESHING){
                mRefreshStatus = STATUS_REFRESHED;
            }
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
        }
        refreshChildrenView(INVALID_OFFSET);
        invalidate();
    }

    /**
     * 检测是否可以下拉刷新
     * @return
     */
    protected boolean checkRefreshable() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTargetView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTargetView, -1) || mTargetView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTargetView, -1);
        }
    }

    /**
     * 检测是否可以上拉加载
     * @return
     */
    protected boolean checkLoadable() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTargetView;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(mTargetView, 1) || mTargetView.getScrollY() < 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTargetView, 1);
        }
    }

    public void setPullListener(OnPullListener listener){
        this.mOnPullListener = listener;
    }

    public void setLoadMoreEnable(boolean enable){
        this.mLoadMoreEnable = enable;
    }

    public void setRefreshEnable(boolean enable){
        this.mRefreshEnable = enable;
    }

    public void setPullRatio(float ratio){
        this.mPullRatio = ratio;
    }
}
