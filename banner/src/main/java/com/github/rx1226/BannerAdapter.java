package com.github.rx1226;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.rx1226.banner.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<String> data;
    private ItemClickListener itemClickListener;

    public BannerAdapter() {}

    public BannerAdapter(List<String> data) {
        this.data = data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ImageHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ImageHolder holder = (ImageHolder) viewHolder;
        Glide.with(context).load(data.get(position)).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return (data != null) ? data.size() : 0;
    }

    private class ImageHolder extends RecyclerView.ViewHolder{
        public ImageView img;
        public ImageHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(view -> {
                if(itemClickListener != null) itemClickListener.onClick(view, getAdapterPosition());
            });
            img = itemView.findViewById(R.id.img);
        }
    }
}