package com.rx1226.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rx1226.Banner;
import com.github.rx1226.ClickListener;
import com.github.rx1226.ScrollListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private final static String TAG = "MainActivity";
    private Banner banner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> list = new ArrayList<>(Arrays.asList(
                "https://raw.githubusercontent.com/RX1226/Banner/master/image/img1.png",
                "https://raw.githubusercontent.com/RX1226/Banner/master/image/img2.png",
                "https://raw.githubusercontent.com/RX1226/Banner/master/image/img3.png"));

        banner = findViewById(R.id.banner);
        banner.setDate(list);
        banner.setScrollTime(2000);
        banner.setScrollListener(new ScrollListener() {
            @Override
            public void onScrollStateChanged(int currentPosition, @NonNull RecyclerView recyclerView) {
                Log.d("TAG", "currentPosition = " + currentPosition);
            }
        });
        banner.setOnClickListener(new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d("TAG", "onClick = " + position);
            }
        });
        banner.appendDate(new ArrayList<>(Arrays.asList(
                "https://raw.githubusercontent.com/RX1226/Banner/master/image/img4.png",
                "https://raw.githubusercontent.com/RX1226/Banner/master/image/img5.png")));
    }

    @Override
    protected void onResume() {
        super.onResume();
        banner.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        banner.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
