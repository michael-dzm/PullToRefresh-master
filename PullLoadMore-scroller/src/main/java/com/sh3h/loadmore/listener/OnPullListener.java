package com.sh3h.loadmore.listener;

/**
 * Created by dengzhimin on 2017/5/25.
 */

public interface OnPullListener {

    /**
     * 刷新数据
     */
    void onRefresh();

    /**
     * 加载更多数据
     */
    void onLoadMore();

}
