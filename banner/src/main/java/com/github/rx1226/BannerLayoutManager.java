package com.github.rx1226;

import android.graphics.PointF;
import android.view.View;

import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

public class BannerLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    protected final OrientationHelper mOrientationHelper;

    private boolean loopMode = true;  //預設為無限循環
    protected int itemWidth;
    private int smoothScrollTime = 500;
    private boolean hasLayout;

    public BannerLayoutManager(int orientation) {
        mOrientationHelper = OrientationHelper.createOrientationHelper(this, orientation);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        layoutChildren(recycler, state);
    }

    private void layoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0 || state.isPreLayout()) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        if (hasLayout) {
            return;
        }

        detachAndScrapAttachedViews(recycler);

        View scrap = recycler.getViewForPosition(0);
        measureChildWithMargins(scrap, 0, 0);
        itemWidth = getDecoratedMeasuredWidth(scrap);
        int offsetX = (mOrientationHelper.getTotalSpace() - mOrientationHelper.getDecoratedMeasurement(scrap)) / 2;
        for (int i = 0; i < getItemCount(); i++) {
            if (offsetX > mOrientationHelper.getTotalSpace()) {
                break;
            }
            View viewForPosition = recycler.getViewForPosition(i);
            addView(viewForPosition);
            measureChildWithMargins(viewForPosition, 0, 0);
            offsetX += layoutItem(viewForPosition, offsetX);
        }


        View lastChild = getChildAt(getChildCount() - 1);
        // 如果是循环布局，并且最后一个view已超出父布局，则添加最左边的view
        if (loopMode && lastChild != null && getDecoratedRight(lastChild) > mOrientationHelper.getTotalSpace()) {
            layoutLeftItem(recycler);
        }
        hasLayout = true;
    }

    private void layoutLeftItem(RecyclerView.Recycler recycler) {
        View childCenter = getChildAt(getChildCount() - 2);
        if (childCenter != null) {
            View viewForPosition = recycler.getViewForPosition(getItemCount() - 1);
            addView(viewForPosition, 0);
            measureChildWithMargins(viewForPosition, 0, 0);
            int top = getItemTop(viewForPosition);
            int left = getDecoratedLeft(childCenter) - itemWidth;
            int right = left + itemWidth;
            layoutDecoratedWithMargins(viewForPosition, left, top, right, top + getDecoratedMeasuredHeight(viewForPosition));
        }
    }

    private int layoutItem(View viewForPosition, int offsetX) {
        layoutDecoratedWithMargins(viewForPosition, offsetX, getItemTop(viewForPosition), offsetX + itemWidth, getItemTop(viewForPosition) + getDecoratedMeasuredHeight(viewForPosition));
        return itemWidth;
    }

    @Override
    public boolean canScrollHorizontally() {
        return getItemCount() > 1;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return recycler == null ? 0 : offsetDx(dx, recycler);
    }

    private int offsetDx(int dx, RecyclerView.Recycler recycler) {
        int realScroll = dx;
        // 向左
        if (dx > 0) {
            realScroll = scrollToLeft(dx, recycler);
        }
        // 向右
        if (dx < 0) {
            realScroll = scrollToRight(dx, recycler);
        }
        return realScroll;
    }

    private int scrollToRight(int dx, RecyclerView.Recycler recycler) {
        int realScroll = dx;
        while (true) {
            View leftChild = getChildAt(0);
            if(leftChild == null) continue;
            int left = getDecoratedLeft(leftChild);
            if (left + Math.abs(dx) > getPaddingLeft()) {
                int position = getPosition(leftChild);
                if (!loopMode && position == 0) {
                    break;
                }

                int addPosition = loopMode ? (position - 1 + getItemCount()) % getItemCount() : position - 1;
                View addView = recycler.getViewForPosition(addPosition);
                addView(addView, 0);
                measureChildWithMargins(addView, 0, 0);
                layoutDecoratedWithMargins(addView, left - getDecoratedMeasuredWidth(addView), getItemTop(addView), left, getItemTop(addView) + getDecoratedMeasuredHeight(addView));

            } else {
                break;
            }
        }

        View firstChild = getChildAt(0);

        if(firstChild != null) {
            int right = getDecoratedRight(firstChild);
            if (getPosition(firstChild) == 0) {
                if (right + Math.abs(dx) > mOrientationHelper.getTotalSpace()) {
                    realScroll = -(mOrientationHelper.getTotalSpace() - right);
                }
            }
        }

        offsetChildrenHorizontal(-realScroll);

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if(childAt == null) continue;
            int decoratedLeft = getDecoratedLeft(childAt);
            if (decoratedLeft > mOrientationHelper.getTotalSpace()) {
                removeAndRecycleView(childAt, recycler);
            }
        }
        return realScroll;
    }

    private int scrollToLeft(int dx, RecyclerView.Recycler recycler) {
        int realScroll = dx;
        while (true) {
            // 将需要添加的view添加到RecyclerView中
            View rightView = getChildAt(getChildCount() - 1);
            if(rightView == null) continue;
            int decoratedRight = getDecoratedRight(rightView);
            if (decoratedRight - dx < mOrientationHelper.getTotalSpace()) {
                int position = getPosition(rightView);
                if (!loopMode && position == getItemCount() - 1) {
                    break;
                }

                int addPosition = loopMode ? (position + 1) % getItemCount() : position + 1;
                View lastViewAdd = recycler.getViewForPosition(addPosition);
                addView(lastViewAdd);
                measureChildWithMargins(lastViewAdd, 0, 0);
                layoutDecoratedWithMargins(lastViewAdd, decoratedRight, getItemTop(lastViewAdd), decoratedRight + getDecoratedMeasuredWidth(lastViewAdd), getItemTop(lastViewAdd) + getDecoratedMeasuredHeight(lastViewAdd));
            } else {
                break;
            }
        }

        // 处理滑动
        View lastChild = getChildAt(getChildCount() - 1);
        if(lastChild != null) {
            int left = getDecoratedLeft(lastChild);
            if (getPosition(lastChild) == getItemCount() - 1) {
                // 最后一个view已经到底了，计算实际可以滑动的距离
                if (left - dx < 0) {
                    realScroll = left;
                }
            }
        }
        offsetChildrenHorizontal(-realScroll);

        // 回收滑出父布局的view
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(child == null) continue;
            int decoratedRight = getDecoratedRight(child);
            if (decoratedRight < 0) {
                removeAndRecycleView(child, recycler);
            }
        }
        return realScroll;
    }

    public int getCurrentPosition() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if(childAt == null) continue;
            if (getDecoratedLeft(childAt) >= 0 && getDecoratedRight(childAt) <= mOrientationHelper.getTotalSpace()) {
                return getPosition(childAt);
            }
        }
        return -1;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int targetPosition) {
        if (!loopMode && (targetPosition < 0 || targetPosition > getItemCount() - 1)) {
            return;
        }
        if (loopMode || getItemCount() > 0) {
            targetPosition = (targetPosition % getItemCount() + getItemCount()) % getItemCount();
        }

        int offset;
        recyclerView.requestFocus();
        int currentPosition = getCurrentPosition();
        if (currentPosition == getItemCount() - 1 && targetPosition == 0 && loopMode) {
            offset = itemWidth;
        } else {
            offset = (targetPosition - currentPosition) * itemWidth;
        }
        recyclerView.smoothScrollBy(offset, 0, null, smoothScrollTime);
    }

    private int getTotalHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getItemTop(View item) {
        return (getTotalHeight() - getDecoratedMeasuredHeight(item)) / 2 + getPaddingTop();
    }

    public boolean isLoopMode() {
        return loopMode;
    }

    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        return null;
    }

    public int getSmoothScrollTime() {
        return smoothScrollTime;
    }

    public void setSmoothScrollTime(int smoothScrollTime) {
        this.smoothScrollTime = smoothScrollTime;
    }
}