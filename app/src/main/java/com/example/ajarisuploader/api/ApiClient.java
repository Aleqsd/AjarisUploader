package com.example.ajarisuploader.api;

import retrofit2.Retrofit;

public class ApiClient {
    public static final String BASE_URL = "https://file.io";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .build();
        }
        return retrofit;
    }
}