package com.github.rx1226;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * An implementation of {@link RecyclerView.LayoutManager} which behaves like view pager.
 * Please make sure your child view have the same size.
 */
public  class BannerLayoutManager extends RecyclerView.LayoutManager {
    public static final int DETERMINE_BY_MAX_AND_MIN = -1;
    private static final int DIRECTION_NO_WHERE = -1;
    private static final int DIRECTION_FORWARD = 0;
    private static final int DIRECTION_BACKWARD = 1;
    protected static final int INVALID_SIZE = Integer.MAX_VALUE;

    private final SparseArray<View> positionCache = new SparseArray<>();
    protected int mDecoratedMeasurement;
    protected int mDecoratedMeasurementInOther;
    int mOrientation;
    protected int mSpaceMain;
    protected int mSpaceInOther;

    /**
     * The offset of property which will change while scrolling
     */
    protected float mOffset;
    protected OrientationHelper mOrientationHelper;

    /**
     * Defines if layout should be calculated from end to start.
     */
    private boolean mReverseLayout = false;
    private boolean mShouldReverseLayout = false;

    /**
     * When LayoutManager needs to scroll to a position, it sets this variable and requests a
     * layout which will check this variable and re-layout accordingly.
     */
    private int mPendingScrollPosition = NO_POSITION;
    private SavedState mPendingSavedState = null;
    protected float mInterval; //the mInterval of each item's mOffset
    private boolean loopMode;
    private boolean mEnableBringCenterToFront;
    private int mLeftItems;
    private int mRightItems;

    /**
     * max visible item count
     */
    private int mMaxVisibleItemCount = DETERMINE_BY_MAX_AND_MIN;
    private int mDistanceToBottom = INVALID_SIZE;

    /**
     * use for handle focus
     */
    private View currentFocusView;

    float centerScale = 1.0f;
    float moveSpeed = 1.0f;

    protected float getDistanceRatio() {
        if (moveSpeed == 0) return Float.MAX_VALUE;
        return 1 / moveSpeed;
    }


    protected float setInterval() {
        int itemSpace = 20;
        return mDecoratedMeasurement * ((centerScale - 1) / 2 + 1) + itemSpace;
    }

    protected void setItemViewProperty(View itemView, float targetOffset) {
        float scale = calculateScale(targetOffset + mSpaceMain);
        itemView.setScaleX(scale);
        itemView.setScaleY(scale);
    }
    /**
     * @param x start positon of the view you want scale
     * @return the scale rate of current scroll mOffset
     */
    private float calculateScale(float x) {
        float deltaX = Math.abs(x - (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2f);
        float diff = 0f;
        if ((mDecoratedMeasurement - deltaX) > 0) diff = mDecoratedMeasurement - deltaX;
        return (centerScale - 1f) / mDecoratedMeasurement * diff + 1;
    }

    /**
     * cause elevation is not support below api 21,
     * so you can set your elevation here for supporting it below api 21
     * or you can just setElevation in {@link #setItemViewProperty(View, float)}
     */
    protected float setViewElevation() {
        return 0;
    }

    /**
     * @param orientation   Layout orientation. Should be {HORIZONTAL or VERTICAL}
     */
    public BannerLayoutManager(int orientation) {
        this(orientation, false);
    }

    public BannerLayoutManager(int orientation, boolean reverseLayout) {
        setEnableBringCenterToFront(true);
        setMaxVisibleItemCount(3);
        setOrientation(orientation);
        setReverseLayout(reverseLayout);
        setItemPrefetchEnabled(false);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mPendingSavedState != null) {
            return new SavedState(mPendingSavedState);
        }
        SavedState savedState = new SavedState();
        savedState.position = mPendingScrollPosition;
        savedState.offset = mOffset;
        savedState.isReverseLayout = mShouldReverseLayout;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = new SavedState((SavedState) state);
            requestLayout();
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == OrientationHelper.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == OrientationHelper.VERTICAL;
    }

    /**
     * Sets the orientation of the layout. {@link BannerLayoutManager}
     * will do its best to keep scroll position.
     *
     * @param orientation {HORIZONTAL} or {VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != OrientationHelper.HORIZONTAL && orientation != OrientationHelper.VERTICAL) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        }
        assertNotInLayoutOrScroll(null);
        if (orientation == mOrientation) {
            return;
        }
        mOrientation = orientation;
        mOrientationHelper = null;
        mDistanceToBottom = INVALID_SIZE;
        removeAllViews();
    }

    /**
     * Set the max visible item count, {@link #DETERMINE_BY_MAX_AND_MIN} means it haven't been set now
     * And it will use {@link #maxRemoveOffset()} and {@link #minRemoveOffset()} to handle the range
     *
     * @param mMaxVisibleItemCount Max visible item count
     */
    public void setMaxVisibleItemCount(int mMaxVisibleItemCount) {
        assertNotInLayoutOrScroll(null);
        if (this.mMaxVisibleItemCount == mMaxVisibleItemCount) return;
        this.mMaxVisibleItemCount = mMaxVisibleItemCount;
        removeAllViews();
    }

    private void resolveShouldLayoutReverse() {
        if (mOrientation == OrientationHelper.HORIZONTAL && getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
            mReverseLayout = !mReverseLayout;
        }
    }

    /**
     * Used to reverse item traversal and layout order.
     * This behaves similar to the layout change for RTL views. When set to true, first item is
     * laid out at the end of the UI, second item is laid out before it etc.
     * <p>
     * For horizontal layouts, it depends on the layout direction.
     * from LTR.
     */
    public void setReverseLayout(boolean reverseLayout) {
        assertNotInLayoutOrScroll(null);
        if (reverseLayout == mReverseLayout) {
            return;
        }
        mReverseLayout = reverseLayout;
        removeAllViews();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final int offsetPosition = getOffsetToPosition(position);
        if (mOrientation == OrientationHelper.VERTICAL) {
            recyclerView.smoothScrollBy(0, offsetPosition, null);
        } else {
            recyclerView.smoothScrollBy(offsetPosition, 0, null);
        }
    }
    @Override
    public void scrollToPosition(int position) {
        if (!loopMode && (position < 0 || position >= getItemCount())) return;
        mPendingScrollPosition = position;
        mOffset = mShouldReverseLayout ? position * -mInterval : position * mInterval;
        requestLayout();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            mOffset = 0;
            return;
        }

        ensureLayoutState();
        resolveShouldLayoutReverse();

        //make sure properties are correct while measure more than once
        View scrap = recycler.getViewForPosition(0);
        measureChildWithMargins(scrap, 0, 0);
        mDecoratedMeasurement = mOrientationHelper.getDecoratedMeasurement(scrap);
        mDecoratedMeasurementInOther = mOrientationHelper.getDecoratedMeasurementInOther(scrap);
        mSpaceMain = (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2;
        if (mDistanceToBottom == INVALID_SIZE) {
            mSpaceInOther = (getTotalSpaceInOther() - mDecoratedMeasurementInOther) / 2;
        } else {
            mSpaceInOther =getTotalSpaceInOther() - mDecoratedMeasurementInOther - mDistanceToBottom;
        }

        mInterval = setInterval();

        mLeftItems = (int) Math.abs(minRemoveOffset() / mInterval) + 1;
        mRightItems = (int) Math.abs(maxRemoveOffset() / mInterval) + 1;

        if (mPendingSavedState != null) {
            mShouldReverseLayout = mPendingSavedState.isReverseLayout;
            mPendingScrollPosition = mPendingSavedState.position;
            mOffset = mPendingSavedState.offset;
        }

        if (mPendingScrollPosition != NO_POSITION) {
            mOffset = mShouldReverseLayout ?
                    mPendingScrollPosition * -mInterval : mPendingScrollPosition * mInterval;
        }

        detachAndScrapAttachedViews(recycler);
        layoutItems(recycler);
    }
    public int getTotalSpaceInOther() {
        if (mOrientation == OrientationHelper.HORIZONTAL) {
            return getHeight() - getPaddingTop()
                    - getPaddingBottom();
        } else {
            return getWidth() - getPaddingLeft()
                    - getPaddingRight();
        }
    }
    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingSavedState = null;
        mPendingScrollPosition = NO_POSITION;
    }

    @Override
    public boolean onAddFocusables(@NonNull RecyclerView recyclerView, @NonNull ArrayList<View> views, int direction, int focusableMode) {
        final int currentPosition = getCurrentPosition();
        final View currentView = findViewByPosition(currentPosition);
        if (currentView == null) return true;
        if (recyclerView.hasFocus()) {
            final int movement = getMovement(direction);
            if (movement != DIRECTION_NO_WHERE) {
                final int targetPosition = movement == DIRECTION_BACKWARD ?
                        currentPosition - 1 : currentPosition + 1;
                recyclerView.smoothScrollToPosition(targetPosition);
            }
        } else {
            currentView.addFocusables(views, direction, focusableMode);
        }
        return true;
    }

    @Override
    public View onFocusSearchFailed(@NonNull View focused, int focusDirection, @NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state) {
        return null;
    }

    private int getMovement(int direction) {
        if (mOrientation == OrientationHelper.VERTICAL) {
            if (direction == View.FOCUS_UP) {
                return mShouldReverseLayout ? DIRECTION_FORWARD : DIRECTION_BACKWARD;
            } else if (direction == View.FOCUS_DOWN) {
                return mShouldReverseLayout ? DIRECTION_BACKWARD : DIRECTION_FORWARD;
            } else {
                return DIRECTION_NO_WHERE;
            }
        } else {
            if (direction == View.FOCUS_LEFT) {
                return mShouldReverseLayout ? DIRECTION_FORWARD : DIRECTION_BACKWARD;
            } else if (direction == View.FOCUS_RIGHT) {
                return mShouldReverseLayout ? DIRECTION_BACKWARD : DIRECTION_FORWARD;
            } else {
                return DIRECTION_NO_WHERE;
            }
        }
    }

    void ensureLayoutState() {
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
        }
    }

    private float getProperty(int position) {
        return mShouldReverseLayout ? position * -mInterval : position * mInterval;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        mOffset = 0;
    }


    @Override
    public int computeHorizontalScrollOffset(@NonNull RecyclerView.State state) {
        return computeScrollOffset();
    }

    @Override
    public int computeVerticalScrollOffset(@NonNull RecyclerView.State state) {
        return computeScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent(@NonNull RecyclerView.State state) {
        return computeScrollExtent();
    }

    @Override
    public int computeVerticalScrollExtent(@NonNull RecyclerView.State state) {
        return computeScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRange(@NonNull RecyclerView.State state) {
        return computeScrollRange();
    }

    @Override
    public int computeVerticalScrollRange(@NonNull RecyclerView.State state) {
        return computeScrollRange();
    }

    private int computeScrollOffset() {
        if (getChildCount() == 0) {
            return 0;
        }

        final float realOffset = getOffsetOfRightAdapterPosition();
        return !mShouldReverseLayout ? (int) realOffset : (int) ((getItemCount() - 1) * mInterval + realOffset);
    }

    private int computeScrollExtent() {
        if (getChildCount() == 0) {
            return 0;
        }
        return (int) mInterval;
    }

    private int computeScrollRange() {
        if (getChildCount() == 0) {
            return 0;
        }
        return (int) (getItemCount() * mInterval);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return (mOrientation == OrientationHelper.VERTICAL) ? 0 : scrollBy(dx, recycler);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return (mOrientation == OrientationHelper.HORIZONTAL) ? 0 : scrollBy(dy, recycler);
    }

    private int scrollBy(int dy, RecyclerView.Recycler recycler) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        ensureLayoutState();
        int willScroll = dy;

        float realDx = dy / getDistanceRatio();
        if (Math.abs(realDx) < 0.00000001f) {
            return 0;
        }
        float targetOffset = mOffset + realDx;

        //handle the boundary
        if (!loopMode && targetOffset < getMinOffset()) {
            willScroll -= (targetOffset - getMinOffset()) * getDistanceRatio();
        } else if (!loopMode && targetOffset > getMaxOffset()) {
            willScroll = (int) ((getMaxOffset() - mOffset) * getDistanceRatio());
        }

        realDx = willScroll / getDistanceRatio();

        mOffset += realDx;

        //handle recycle
        layoutItems(recycler);

        return willScroll;
    }

    private void layoutItems(RecyclerView.Recycler recycler) {
        detachAndScrapAttachedViews(recycler);
        positionCache.clear();

        final int itemCount = getItemCount();
        if (itemCount == 0) return;

        // make sure that current position start from 0 to 1
        final int currentPos = mShouldReverseLayout ?
                -getCurrentPositionOffset() : getCurrentPositionOffset();
        int start = currentPos - mLeftItems;
        int end = currentPos + mRightItems;

        // handle max visible count
        if (useMaxVisibleCount()) {
            boolean isEven = mMaxVisibleItemCount % 2 == 0;
            int offset;
            if (isEven) {
                offset = mMaxVisibleItemCount / 2;
                start = currentPos - offset + 1;
            } else {
                offset = (mMaxVisibleItemCount - 1) / 2;
                start = currentPos - offset;
            }
            end = currentPos + offset + 1;
        }

        if (!loopMode) {
            if (start < 0) {
                start = 0;
                if (useMaxVisibleCount()) end = mMaxVisibleItemCount;
            }
            if (end > itemCount) end = itemCount;
        }

        float lastOrderWeight = Float.MIN_VALUE;
        for (int i = start; i < end; i++) {
            if (useMaxVisibleCount() || !removeCondition(getProperty(i) - mOffset)) {
                // start and end base on current position,
                // so we need to calculate the adapter position
                int adapterPosition = i;
                if (i >= itemCount) {
                    adapterPosition %= itemCount;
                } else if (i < 0) {
                    int delta = (-adapterPosition) % itemCount;
                    if (delta == 0) delta = itemCount;
                    adapterPosition = itemCount - delta;
                }
                final View scrap = recycler.getViewForPosition(adapterPosition);
                measureChildWithMargins(scrap, 0, 0);
                resetViewProperty(scrap);
                // we need i to calculate the real offset of current view
                final float targetOffset = getProperty(i) - mOffset;
                layoutScrap(scrap, targetOffset);
                final float orderWeight = mEnableBringCenterToFront ?
                        setViewElevation() : adapterPosition;
                if (orderWeight > lastOrderWeight) {
                    addView(scrap);
                } else {
                    addView(scrap, 0);
                }
                if (i == currentPos) currentFocusView = scrap;
                lastOrderWeight = orderWeight;
                positionCache.put(i, scrap);
            }
        }

        currentFocusView.requestFocus();
    }

    private boolean useMaxVisibleCount() {
        return mMaxVisibleItemCount != DETERMINE_BY_MAX_AND_MIN;
    }

    private boolean removeCondition(float targetOffset) {
        return targetOffset > maxRemoveOffset() || targetOffset < minRemoveOffset();
    }

    private void resetViewProperty(View v) {
        v.setRotation(0);
        v.setRotationY(0);
        v.setRotationX(0);
        v.setScaleX(1f);
        v.setScaleY(1f);
        v.setAlpha(1f);
    }

    /* package */ float getMaxOffset() {
        return !mShouldReverseLayout ? (getItemCount() - 1) * mInterval : 0;
    }

    /* package */ float getMinOffset() {
        return !mShouldReverseLayout ? 0 : -(getItemCount() - 1) * mInterval;
    }

    private void layoutScrap(View scrap, float targetOffset) {

        final int left = calItemLeft(targetOffset);
        final int top = calItemTop(targetOffset);
        if (mOrientation == OrientationHelper.VERTICAL) {
            layoutDecorated(scrap, mSpaceInOther + left, mSpaceMain + top,
                    mSpaceInOther + left + mDecoratedMeasurementInOther, mSpaceMain + top + mDecoratedMeasurement);
        } else {
            layoutDecorated(scrap, mSpaceMain + left, mSpaceInOther + top,
                    mSpaceMain + left + mDecoratedMeasurement, mSpaceInOther + top + mDecoratedMeasurementInOther);
        }
        setItemViewProperty(scrap, targetOffset);
    }

    protected int calItemLeft(float targetOffset) {
        return mOrientation == OrientationHelper.VERTICAL ? 0 : (int) targetOffset;
    }

    protected int calItemTop(float targetOffset) {
        return mOrientation == OrientationHelper.VERTICAL ? (int) targetOffset : 0;
    }

    /**
     * when the target offset reach this,
     * the view will be removed and recycled in {@link #layoutItems(RecyclerView.Recycler)}
     */
    protected float maxRemoveOffset() {
        return mOrientationHelper.getTotalSpace() - mSpaceMain;
    }

    /**
     * when the target offset reach this,
     * the view will be removed and recycled in {@link #layoutItems(RecyclerView.Recycler)}
     */
    protected float minRemoveOffset() {
        return -mDecoratedMeasurement - mOrientationHelper.getStartAfterPadding() - mSpaceMain;
    }


    public int getCurrentPosition() {
        if (getItemCount() == 0) return 0;

        int position = getCurrentPositionOffset();
        if (!loopMode) return Math.abs(position);

        position = !mShouldReverseLayout ?
                //take care of position = getItemCount()
                (position >= 0 ?
                        position % getItemCount() :
                        getItemCount() + position % getItemCount()) :
                (position > 0 ?
                        getItemCount() - position % getItemCount() :
                        -position % getItemCount());
        return position == getItemCount() ? 0 : position;
    }

    @Override
    public View findViewByPosition(int position) {
        final int itemCount = getItemCount();
        if (itemCount == 0) return null;
        for (int i = 0; i < positionCache.size(); i++) {
            final int key = positionCache.keyAt(i);
            if (key >= 0) {
                if (position == key % itemCount) return positionCache.valueAt(i);
            } else {
                int delta = key % itemCount;
                if (delta == 0) delta = -itemCount;
                if (itemCount + delta == position) return positionCache.valueAt(i);
            }
        }
        return null;
    }

    private int getCurrentPositionOffset() {
        return Math.round(mOffset / mInterval);
    }

    /**
     * Sometimes we need to get the right offset of matching adapter position
     * cause when {@link #loopMode} is set true, there will be no limitation of {@link #mOffset}
     */
    private float getOffsetOfRightAdapterPosition() {
        if (mShouldReverseLayout)
            return loopMode ?
                    (mOffset <= 0 ?
                            (mOffset % (mInterval * getItemCount())) :
                            (getItemCount() * -mInterval + mOffset % (mInterval * getItemCount()))) :
                    mOffset;
        else
            return loopMode ?
                    (mOffset >= 0 ?
                            (mOffset % (mInterval * getItemCount())) :
                            (getItemCount() * mInterval + mOffset % (mInterval * getItemCount()))) :
                    mOffset;
    }

    public int getOffsetToPosition(int position) {
        if (loopMode)
            return (int) (((getCurrentPositionOffset() +
                    (!mShouldReverseLayout ? position - getCurrentPosition() : getCurrentPosition() - position)) *
                    mInterval - mOffset) * getDistanceRatio());
        return (int) ((position *
                (!mShouldReverseLayout ? mInterval : -mInterval) - mOffset) * getDistanceRatio());
    }

    public void setEnableBringCenterToFront(boolean bringCenterToTop) {
        assertNotInLayoutOrScroll(null);
        if (mEnableBringCenterToFront == bringCenterToTop) {
            return;
        }
        this.mEnableBringCenterToFront = bringCenterToTop;
        requestLayout();
    }

    private static class SavedState implements Parcelable {
        int position;
        float offset;
        boolean isReverseLayout;

        SavedState() {

        }

        SavedState(Parcel in) {
            position = in.readInt();
            offset = in.readFloat();
            isReverseLayout = in.readInt() == 1;
        }

        public SavedState(SavedState other) {
            position = other.position;
            offset = other.offset;
            isReverseLayout = other.isReverseLayout;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(position);
            dest.writeFloat(offset);
            dest.writeInt(isReverseLayout ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setLoopMode(boolean loopMode){
        this.loopMode = loopMode;
    }
}