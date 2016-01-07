package ru.ifmo.ctd.mekhanikov.crawler.instagram;

import com.squareup.okhttp.ResponseBody;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface InstagramService {
    @GET("/v1/users/{userId}/follows")
    Call<ResponseBody> getFollows(@Path("userId") long userId,
                                  @Query("cursor") String cursor,
                                  @Query("access_token") String accessToken);
}
