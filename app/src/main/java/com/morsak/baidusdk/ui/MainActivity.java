package com.morsak.baidusdk.ui;

import android.app.ProgressDialog;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.morsak.baidusdk.R;
import com.morsak.baidusdk.data.WeatherDataBean;
import com.morsak.baidusdk.entity.Constans;
import com.morsak.baidusdk.impl.WeatherImpl;

import java.util.Calendar;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity{
    // Content View Elements

    private LinearLayout mLlRefresh;
    private TextView mTvNowTime;
    private TextView mTvCity;
    private TextView mTvUpdateTime;
    private TextView mTvCode;
    private TextView mTvWeather;
    private TextView mTvWind;
    private TextView mTvWindCode;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private ProgressDialog dialog;
    private String city = "唐山市";
    private WeatherImpl impl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    //初始化View
    private void initView(){
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);
        initLocation();
        initDialog();
        initRetrofit();
        dialog.show();
        //开启定位
        mLocationClient.start();
        Log.i(Constans.TAG,"开始定位");

        // End Of Content View Elements
            mLlRefresh = (LinearLayout) findViewById(R.id.llRefresh);
            mTvNowTime = (TextView) findViewById(R.id.tvNowTime);
            mTvCity = (TextView) findViewById(R.id.tvCity);
            mTvUpdateTime = (TextView) findViewById(R.id.tvUpdateTime);
            mTvCode = (TextView) findViewById(R.id.tvCode);
            mTvWeather = (TextView) findViewById(R.id.tvWeather);
            mTvWind = (TextView) findViewById(R.id.tvWind);
            mTvWindCode = (TextView) findViewById(R.id.tvWindCode);
    }
    private void initDialog()
    {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Waiting");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
    }
    //初始化Retrofit
    private void initRetrofit(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apis.juhe.cn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        impl= retrofit.create(WeatherImpl.class);

    }
    //请求Weather
    private void requestWeather(String city)
    {
        Call<WeatherDataBean> weatherBeanCall = impl.getWeather(city,Constans.WEATHER_KEY);
        weatherBeanCall.enqueue(new Callback<WeatherDataBean>() {
            @Override
            public void onResponse(Call<WeatherDataBean> call, Response<WeatherDataBean> response) {
                //Log.i(Constans.TAG,response.body().toString());
                WeatherDataBean bean = response.body();
                Calendar cal;
                cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                String hour,minute,second;
                if (cal.get(Calendar.AM_PM) == 0)
                    hour = String.valueOf(cal.get(Calendar.HOUR));
                else
                    hour = String.valueOf(cal.get(Calendar.HOUR)+12);
                minute = String.valueOf(cal.get(Calendar.MINUTE));
                second = String.valueOf(cal.get(Calendar.SECOND));
                mTvCity.setText(bean.getResult().getCity());
                mTvUpdateTime.setText(hour+":"+minute+":"+second);
                mTvCode.setText(bean.getResult().getRealtime().getTemperature());
                mTvWeather.setText(bean.getResult().getRealtime().getInfo());
                mTvWind.setText("风向|"+bean.getResult().getRealtime().getDirect());
                mTvWindCode.setText("风力|"+bean.getResult().getRealtime().getPower());
            }

            @Override
            public void onFailure(Call<WeatherDataBean> call, Throwable t) {
                Log.i(Constans.TAG,"发生未知错误");
            }
        });
    }
    //初始化定位
    private void initLocation()
    {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(0);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setEnableSimulateGps(false);
        option.setIsNeedAddress(true);//如果没有则不会显示定位位置
        mLocationClient.setLocOption(option);
    }


    // 定位回调
    public class MyLocationListener extends BDAbstractLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
            dialog.dismiss();
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
            //String addr = location.getAddrStr();    //获取详细地址信息
            //String country = location.getCountry();    //获取国家
            //String province = location.getProvince();    //获取省份
            //String city = location.getCity();    //获取城市
            //String district = location.getDistrict();    //获取区县
            //String street = location.getStreet();    //获取街道信息
            switch (location.getLocType())
            {
                case BDLocation.TypeGpsLocation:
                case BDLocation.TypeNetWorkLocation:
                case BDLocation.TypeOffLineLocation:
                    city = location.getCity();
                    Log.i(Constans.TAG,"定位成功:"+location.getCity());
                    break;
                case BDLocation.TypeServerError:
                case BDLocation.TypeNetWorkException:
                case BDLocation.TypeCriteriaException:
                    Log.i(Constans.TAG,"定位失败");
                    break;
            }
            requestWeather(city.toString().substring(0,city.toString().length()-1));
        }
    }
}
