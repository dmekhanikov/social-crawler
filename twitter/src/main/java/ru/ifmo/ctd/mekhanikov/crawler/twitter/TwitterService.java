package ru.ifmo.ctd.mekhanikov.crawler.twitter;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TwitterService {

    @GET("/{userName}/following/users")
    Call<UsersResponse> listFriends(@Path("userName") String userName,
                                    @Query("max_position") long maxPosition);
}
