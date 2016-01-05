package ru.ifmo.ctd.mekhanikov.crawler.foursquare;

import com.squareup.okhttp.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import ru.ifmo.ctd.mekhanikov.crawler.FriendsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FoursquareFriendsService implements FriendsService {

    private static final Logger LOG = LoggerFactory.getLogger(FoursquareFriendsService.class);

    private static final String CLIENT_ID_PROPERTY = "foursquare.client_id";
    private static final String CLIENT_SECRET_PROPERTY = "foursquare.client_secret";
    private static final String FOURSQUARE_BASE_URL = "https://api.foursquare.com/v2";

    private final String clientId;
    private final String clientSecret;
    private final FoursquareService service;

    public FoursquareFriendsService() {
        this.clientId = System.getProperty(CLIENT_ID_PROPERTY);
        this.clientSecret = System.getProperty(CLIENT_SECRET_PROPERTY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FOURSQUARE_BASE_URL)
                .build();
        this.service = retrofit.create(FoursquareService.class);
        LOG.info("Foursquare service initialized");
    }

    public List<Long> getFriends(long userId) throws IOException {
        LOG.info("Getting friends list of user " + userId);
        Call<ResponseBody> call = service.listFriends(userId, clientId, clientSecret);
        Response<ResponseBody> response = call.execute();
        LOG.info("Response received");
        if (response.isSuccess()) {
            List<Long> friends = new ArrayList<>();
            JSONObject json = new JSONObject(response.body().string());
            JSONArray items = json.getJSONObject("response")
                    .getJSONObject("friends")
                    .getJSONArray("items");
            for (Object item : items) {
                JSONObject jsonFriend = (JSONObject) item;
                friends.add(jsonFriend.getLong("id"));
            }
            return friends;
        } else {
            throw new IllegalStateException(response.message());
        }
    }
}
