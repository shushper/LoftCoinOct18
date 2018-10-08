package com.loftschool.loftcoinoct18.data.api;

import com.loftschool.loftcoinoct18.data.api.model.RateResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("ticker")
    Call<RateResponse> ticker(@Query("structure") String structure, @Query("convert") String convert);
}