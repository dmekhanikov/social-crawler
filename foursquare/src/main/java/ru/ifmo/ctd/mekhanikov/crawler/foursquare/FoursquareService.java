package ru.ifmo.ctd.mekhanikov.crawler.foursquare;

import com.squareup.okhttp.ResponseBody;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface FoursquareService {

    @GET("/v2/users/{userId}/friends?v=20160102")
    Call<ResponseBody> listFriends(@Path("userId") long userId,
                                   @Query("client_id") String clientId,
                                   @Query("client_secret") String clientSecret);
}
