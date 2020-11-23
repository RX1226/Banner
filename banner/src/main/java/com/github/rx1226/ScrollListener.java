package com.github.rx1226;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface ScrollListener {
    void onScrollStateChanged(int currentPosition, @NonNull RecyclerView recyclerView);
}