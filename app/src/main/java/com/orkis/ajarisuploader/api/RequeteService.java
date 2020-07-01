package com.orkis.ajarisuploader.api;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Alex on 17/01/2017.
 * This interface is used by Retrofit
 */

public interface RequeteService {
    @Multipart
    @POST("upImportDoc.do")
    Call<ResponseBody> uploadSingleFile(
            @Header("Cookie") String sessionid,
            @Header("User-Agent") String userAgent,
            @Part MultipartBody.Part file,
            @Part("jsessionid") RequestBody jsessionid,
            @Part("ptoken") RequestBody ptoken,
            @Part("ajaupmo") RequestBody ajaupmo,
            @Part("ContributionComment") RequestBody ContributionComment,
            @Part("Document_numbasedoc") RequestBody Document_numbasedoc,
            @Part("contribution") RequestBody contribution);
}