package com.github.rx1226;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.github.rx1226.Unit.dp2px;

public class IndicatorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int indicatorMargin = dp2px(4);
    private Drawable selectDrawable;
    private Drawable unSelectDrawable;
    private int size;
    private int currentPosition = 0;
    private final int indicatorSize = 6;

    public void setPosition(int currentPosition) {
        this.currentPosition = currentPosition;
        notifyDataSetChanged();
    }
    public void setSize(int size) {this.size = size;}

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //選中的繪圖
        selectDrawable = new LayerDrawable(new Drawable[]{genGradientDrawable(Color.WHITE, indicatorSize)});
        //未選中的繪圖
        unSelectDrawable = new LayerDrawable(new Drawable[]{genGradientDrawable(Color.GRAY, indicatorSize)});

        ImageView bannerPoint = new ImageView(parent.getContext());
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin);
        bannerPoint.setLayoutParams(layoutParams);
        return new RecyclerView.ViewHolder(bannerPoint){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageView bannerPoint = (ImageView) holder.itemView;
        bannerPoint.setImageDrawable(currentPosition == position ? selectDrawable : unSelectDrawable);
    }

    @Override
    public int getItemCount() {
        return size;
    }

    private GradientDrawable genGradientDrawable(int Color, int indicatorSize){
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(Color);
        gradientDrawable.setSize(dp2px(indicatorSize), dp2px(indicatorSize));
        gradientDrawable.setCornerRadius(dp2px(indicatorSize));
        return gradientDrawable;
    }
}