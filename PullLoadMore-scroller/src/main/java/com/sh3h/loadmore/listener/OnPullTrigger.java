package com.sh3h.loadmore.listener;

/**
 * Created by dengzhimin on 2017/5/25.
 */
public interface OnPullTrigger {
    /**
     * 准备下拉或上拉
     */
    void onPrepare();

    /**
     * 移动
     * @param y offset
     * @param isComplete
     */
    void onMove(int y, boolean isComplete);

    /**
     * 释放
     */
    void onRelease();

    /**
     * 加载或刷新完成
     */
    void onComplete();

    /**
     * 重置
     */
    void onReset();
}
