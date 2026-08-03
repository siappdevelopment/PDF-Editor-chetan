package com.reader.alldocviewer.pdfeditor.launcher.common;


import com.reader.alldocviewer.pdfeditor.launcher.model.NewsResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIInterface {
    @GET("v1/latest-news")
    Call<NewsResponseModel> getLatestNews(@Query("apiKey") String apiKey, @Query("language") String language);
}