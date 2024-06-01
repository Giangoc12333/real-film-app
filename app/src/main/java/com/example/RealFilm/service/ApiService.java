package com.example.RealFilm.service;

import com.example.RealFilm.BuildConfig;
import com.example.RealFilm.MyApplication;
import com.example.RealFilm.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    public static final String BASE_URL = "http://127.0.0.1:3000/api/v1/";
    private static Retrofit retrofit;

    public static <S> S createService(Class<S> serviceClass) {
        String token = MyApplication.getToken();

        Interceptor interceptor = chain -> {
            Request originalRequest = chain.request();
            Request.Builder modifiedRequest = originalRequest.newBuilder();
            modifiedRequest.addHeader("Authorization", "Bearer " + token);
            return chain.proceed(modifiedRequest.build());
        };

        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(90, TimeUnit.SECONDS)
                .connectTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .cache(null);

//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BASIC); // Giảm mức độ log xuống BASIC hoặc NONE
//            httpClient.addInterceptor(logging);
//
//        }

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit.create(serviceClass);
    }
}