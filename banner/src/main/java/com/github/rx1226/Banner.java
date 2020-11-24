package com.github.rx1226;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rx1226.banner.R;

import java.util.List;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
import static com.github.rx1226.Unit.dp2px;

public class Banner extends FrameLayout {
    private RecyclerView recyclerView;
    private BannerLayoutManager layoutManager;
    private BannerAdapter bannerAdapter;
    private IndicatorAdapter indicatorAdapter;
    private RecyclerView indicatorContainer;
    private List<String> data;
    private boolean isShowIndicator; //顯示指標
    private int interval; //刷新時間區間
    private Handler handler;
    private ScrollListener scrollListener;
    private int currentPosition;
    private boolean isTouch;

    public Banner(@NonNull Context context) {
        this(context, null);
    }

    public Banner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context, attrs);
    }

    private void initLayout(@NonNull Context context, @Nullable AttributeSet attrs){
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Banner);
        isShowIndicator = attributes.getBoolean(R.styleable.Banner_showIndicator, true);
        int indicatorGravity = attributes.getInt(R.styleable.Banner_indicatorGravity, Gravity.BOTTOM | Gravity.CENTER);
        int indicatorOrientation = attributes.getInt(R.styleable.Banner_indicatorOrientation, 0);
        //是否自動撥放
        boolean isAutoPlaying = attributes.getBoolean(R.styleable.Banner_autoPlay, true);
//        boolean loopMode = attributes.getBoolean(R.styleable.Banner_loopMode, true);
        interval = attributes.getInt(R.styleable.Banner_interval, 3000);
        int orientation = attributes.getInt(R.styleable.Banner_orientation, 0);
//        int scrollTime = attributes.getInt(R.styleable.Banner_scrollTime, 500);
        attributes.recycle();

        recyclerView = new RecyclerView(context);
        LayoutParams vpLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(recyclerView, vpLayoutParams);
        layoutManager = new BannerLayoutManager(orientation);
//        layoutManager.setLoopMode(loopMode);
//        layoutManager.setSmoothScrollTime(scrollTime);
        bannerAdapter = new BannerAdapter();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(bannerAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == SCROLL_STATE_TOUCH_SCROLL){
                    currentPosition = layoutManager.getCurrentPosition();
                    indicatorAdapter.setPosition(currentPosition);
                    isTouch = true;
                }else {
                    isTouch = false;
                }
                if(newState == SCROLL_STATE_IDLE){
                    currentPosition = layoutManager.getCurrentPosition();
                    indicatorAdapter.setPosition(currentPosition);
                    if(scrollListener != null) scrollListener.onScrollStateChanged(currentPosition, recyclerView);
                }
            }
        });
        new BannerPageSnapHelper().attachToRecyclerView(recyclerView);

        //指示器
        indicatorContainer = new RecyclerView(context);
        LinearLayoutManager indicatorLayoutManager = new LinearLayoutManager(context, indicatorOrientation, false);
        indicatorContainer.setLayoutManager(indicatorLayoutManager);
        indicatorAdapter = new IndicatorAdapter();
        indicatorContainer.setAdapter(indicatorAdapter);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = indicatorGravity;
        int margin = dp2px(12);
        params.setMargins(margin, margin, margin, margin);
        addView(indicatorContainer, params);
        if (isShowIndicator) {
            indicatorContainer.setVisibility(VISIBLE);
        }else {
            indicatorContainer.setVisibility(GONE);
        }
        if(isAutoPlaying){
            start();
        }
    }

    public void setScrollListener(ScrollListener scrollListener){
        this.scrollListener = scrollListener;
    }

    public void setOnClickListener(ClickListener clickListener){
        bannerAdapter.setClickListener(clickListener);
    }

    public void showIndicator(boolean isShowIndicator) {
        this.isShowIndicator = isShowIndicator;
        indicatorContainer.setVisibility(isShowIndicator ? VISIBLE : GONE);
    }

//    public void setScrollTime(int scrollTime){
//        layoutManager.setSmoothScrollTime(scrollTime);
//    }

//    public void setLoopMode(boolean loopMode){
//        layoutManager.setLoopMode(loopMode);
//    }

    public void setDate(List<String> data){
        if(data != null) {
            this.data = data;
            bannerAdapter.setData(data);
            indicatorAdapter.setSize(data.size());
        }
    }

    public void appendDate(List<String> data){
        this.data.addAll(data);
        bannerAdapter.setData(this.data);
        indicatorAdapter.setSize(this.data.size());
    }

    private final Runnable autoRun = new Runnable() {
        @Override
        public void run() {
            if(!isTouch) {
                currentPosition = layoutManager.getCurrentPosition() + 1;
                if (currentPosition == data.size()) currentPosition = 0;
                recyclerView.smoothScrollToPosition(currentPosition);
                indicatorAdapter.setPosition(currentPosition);
            }
            handler.postDelayed(this, interval);
        }
    };
    public void start(){
        if(handler == null) {
            handler = new Handler();
            handler.postDelayed(autoRun, interval);
        }
    }
    public void stop(){
        if(handler != null){
            handler.removeCallbacks(autoRun);
        }
    }
}