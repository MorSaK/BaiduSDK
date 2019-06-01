package com.morsak.baidusdk.impl;

import com.morsak.baidusdk.data.WeatherDataBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherImpl {
    //http://apis.juhe.cn/
    // simpleWeather/query?
    // city=%E8%8B%8F%E5%B7%9E&key=c7347a30b3a6e34dceeed69ef167977d
    @GET("simpleWeather/query?")
    Call<WeatherDataBean> getWeather(@Query("city")String city,@Query("key")String key);
}
