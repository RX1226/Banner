<h1 align="center">Banner</h1>

<p align="center">
  <a target="_blank" href="https://www.paypal.me/RX1226" title="Donate using PayPal"><img src="https://img.shields.io/badge/paypal-donate-yellow.svg" /></a>
</p>


A banner for Android.

It can download image form Internert and append data

*Inspired by [ren93](https://github.com/ren93)/**[RecyclerBanner](https://github.com/ren93/RecyclerBanner)****

*Inspired by [ZhangHao555](https://github.com/ZhangHao555)/**[BannerRecyclerView](https://github.com/ZhangHao555/BannerRecyclerView)**

## How to use

1. Add the JitPack repository to your build file:
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
2. Add the dependency:
```
    dependencies {
        implementation 'com.github.RX1226:Banner:1.0.0'
    }
```

3. Add permission in AndroidManifest.xml
```
    <uses-permission android:name="android.permission.INTERNET"/>
```
## Usage
Process flow
**note**
a. instance object and init, add listener

```
public class MainActivity extends AppCompatActivity {
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
}
```
b. start and stop banner
```
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
```
## License
	Copyright 2020 RX1226
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	   http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
