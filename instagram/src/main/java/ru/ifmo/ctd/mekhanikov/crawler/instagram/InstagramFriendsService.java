package ru.ifmo.ctd.mekhanikov.crawler.instagram;

import com.squareup.okhttp.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;
import ru.ifmo.ctd.mekhanikov.crawler.FriendsService;
import ru.ifmo.ctd.mekhanikov.crawler.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Target("instagram")
public class InstagramFriendsService implements FriendsService {
    private static final Logger LOG = LoggerFactory.getLogger(InstagramFriendsService.class);

    private static final String INSTAGRAM_BASE_URL = "https://api.instagram.com/";
    private static final String ACCESS_TOKEN_PROPERTY = "instagram.access_token";
    private static final String REMAINING_HEADER = "X-Ratelimit-Remaining";

    private final String accessToken;
    private final InstagramService service;
    private int requestsLeft = -1;

    public InstagramFriendsService() {
        this.accessToken = System.getProperty(ACCESS_TOKEN_PROPERTY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(INSTAGRAM_BASE_URL)
                .build();
        this.service = retrofit.create(InstagramService.class);
        LOG.info("Instagram service initialized");
    }

    @Override
    public List<Long> getFriends(long userId) throws IOException {
        LOG.info("Getting friends list of user " + userId);
        List<Long> friends = new ArrayList<>();
        String cursor = "";
        while (cursor != null) {
            LOG.info(String.format("Sending request. UserId=%d, cursor=%s", userId, cursor));
            Call<ResponseBody> call = service.getFollows(userId, cursor, accessToken);
            Response<ResponseBody> response = call.execute();
            LOG.info("Response received");
            requestsLeft = Integer.parseInt(response.headers().get(REMAINING_HEADER));
            if (response.isSuccess()) {
                String responseString = response.body().string();
                parseFollowsList(responseString, friends);
                cursor = getNextCursor(responseString);
            } else {
                throw new IllegalStateException(response.message());
            }
        }
        return friends;
    }

    private void parseFollowsList(String response, List<Long> follows) {
        JSONObject json = new JSONObject(response);
        JSONArray items = json.getJSONArray("data");
        for (Object item : items) {
            JSONObject jsonFriend = (JSONObject) item;
            follows.add(jsonFriend.getLong("id"));
        }
    }

    private String getNextCursor(String response) {
        JSONObject json = new JSONObject(response);
        JSONObject pagination = json.getJSONObject("pagination");
        if (pagination.has("next_cursor")) {
            return (String) pagination.get("next_cursor");
        } else {
            return null;
        }
    }

    @Override
    public int getRequestsLeft() {
        return requestsLeft;
    }

    @Override
    public int getSecondsUntilReset() {
        return 3600;
    }
}
