package com.reader.alldocviewer.pdfeditor.launcher;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.reader.alldocviewer.pdfeditor.R;
import com.reader.alldocviewer.pdfeditor.launcher.adapter.NewsAdapter;
import com.reader.alldocviewer.pdfeditor.launcher.common.APIClient;
import com.reader.alldocviewer.pdfeditor.launcher.common.APIInterface;
import com.reader.alldocviewer.pdfeditor.launcher.common.Utils;
import com.reader.alldocviewer.pdfeditor.launcher.model.NewsModel;
import com.reader.alldocviewer.pdfeditor.launcher.model.NewsResponseModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsActivity extends AppCompatActivity {
    private AppCompatEditText etSearch;
    private AppCompatImageView ivSearch;
    private ProgressBar pbDataLoad;
    private RecyclerView rvNews;

    private final ArrayList<NewsModel> arrayListNewsModel = new ArrayList<>();
    private String stringSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        initViews();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        ivSearch = findViewById(R.id.ivSearch);
        pbDataLoad = findViewById(R.id.pbDataLoad);
        rvNews = findViewById(R.id.rvNews);

        clickEvents();
    }

    private void clickEvents() {
        Utils.trackScreen(this, "NEWS_SCREEN_SHOW");
        getNews();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                stringSearch = charSequence.toString();
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        ivSearch.setOnClickListener(view -> {
            if (!stringSearch.isEmpty()) {
                String url = "https://www.google.com/search?q=" + Uri.encode(stringSearch);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.android.chrome");
                startActivity(intent);
                etSearch.setText("");
                etSearch.clearFocus();
            } else {
                Toast.makeText(NewsActivity.this, "Please Enter Text !", Toast.LENGTH_SHORT).show();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NewsActivity.this.finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_right_news);
            }
        });
    }

    private void getNews() {
        pbDataLoad.setVisibility(VISIBLE);

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<NewsResponseModel> call = apiInterface.getLatestNews("TBX2Bm48azyFOI8l53wfhOItaZlKSnHXb286agRi79Y0B-al", "en");
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponseModel> call, @NonNull Response<NewsResponseModel> response) {
                if (response.isSuccessful() && response.body().getNews() != null) {
                    arrayListNewsModel.clear();
                    Log.e("ghgjgjgjjgjgh", "onResponse() returned: " +response.body().getNews() );
                    arrayListNewsModel.addAll(response.body().getNews());

//                    if (AdPlacement.shouldShowNewsNativeAd1() && arrayListNewsModel.size() > 1) {
//                        NewsModel newsModel1 = new NewsModel();
//                        newsModel1.setAdShow(true);
//                        newsModel1.setAdSlot(1);
//                        arrayListNewsModel.add(1, newsModel1);
//                    }
//
//                    if (AdPlacement.shouldShowNewsNativeAd2() && arrayListNewsModel.size() > 5) {
//                        NewsModel newsModel5 = new NewsModel();
//                        newsModel5.setAdShow(true);
//                        newsModel5.setAdSlot(2);
//                        arrayListNewsModel.add(5, newsModel5);
//                    }

                    setNewsAdapter();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponseModel> call, @NonNull Throwable t) {
            }
        });
    }

    private void setNewsAdapter() {
        pbDataLoad.setVisibility(GONE);

        NewsAdapter newsAdapter = new NewsAdapter(getApplicationContext(), arrayListNewsModel);
        rvNews.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvNews.setAdapter(newsAdapter);
    }
}