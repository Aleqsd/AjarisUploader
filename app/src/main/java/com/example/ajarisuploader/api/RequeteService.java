package com.example.ajarisuploader.api;


import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Alex on 17/01/2017.
 * This interface is used by Retrofit
 */

public interface RequeteService {
    @Multipart
    @POST("upImportDoc.do")
    Call<ResponseBody> uploadSingleFile(
            @Header("Cookie") String sessionid,
            @Part MultipartBody.Part file,
            @Part("jsessionid") RequestBody jsessionid,
            @Part("ptoken") RequestBody ptoken,
            @Part("ajaupmo") RequestBody ajaupmo,
            @Part("ContributionComment") RequestBody ContributionComment,
            @Part("Document_numbasedoc") RequestBody Document_numbasedoc,
            @Part("contribution") RequestBody contribution);
}