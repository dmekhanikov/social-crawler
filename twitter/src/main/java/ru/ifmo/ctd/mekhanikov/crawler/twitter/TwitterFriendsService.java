package ru.ifmo.ctd.mekhanikov.crawler.twitter;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.Call;
import retrofit.JacksonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import ru.ifmo.ctd.mekhanikov.crawler.FriendsService;
import ru.ifmo.ctd.mekhanikov.crawler.NamesService;
import ru.ifmo.ctd.mekhanikov.crawler.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Target("twitter")
public class TwitterFriendsService implements FriendsService {

    private static final Logger LOG = LoggerFactory.getLogger(TwitterFriendsService.class);
    private static final String TWITTER_BASE_URL = "https://twitter.com/";
    private static final String AUTH_TOKEN_PROPERTY = "twitter.auth_token";

    private TwitterService twitterService;
    private String authToken;
    private NamesService namesService;
    private Random random;

    public TwitterFriendsService(NamesService namesService) {
        this.namesService = namesService;
        this.authToken = System.getProperty(AUTH_TOKEN_PROPERTY);
        this.random = new Random(System.currentTimeMillis());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TWITTER_BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.twitterService = retrofit.create(TwitterService.class);
        LOG.info("Twitter service initialized");
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(chain -> {
            Request request = chain.request();
            Request newRequest = request.newBuilder()
                    .addHeader("Cookie", "auth_token=" + authToken)
                    .build();
            return chain.proceed(newRequest);
        });
        return client;
    }

    @Override
    public List<Long> getFriends(long userId) throws Exception {
        LOG.info("Getting friends list of user " + userId);
        String userName = namesService.getName(userId);
        LOG.info("Screen name for " + userId + ": " + userName);
        List<Long> friends = new ArrayList<>();
        boolean hasMoreItems = true;
        long maxPosition = -1;
        while (hasMoreItems) {
            LOG.info(String.format("Sending request: {userName: %s, maxPosition: %d}", userName, maxPosition));
            Call<UsersResponse> call = twitterService.listFriends(userName, maxPosition);
            Response<UsersResponse> response = call.execute();
            LOG.info("Response received");
            if (response.isSuccess()) {
                UsersResponse responseBody = response.body();
                List<Long> receivedUsers = parseUsers(responseBody.getItemsHtml());
                assert(receivedUsers.size() == responseBody.getNewLatentCount());
                LOG.info(receivedUsers.size() + " users found");
                friends.addAll(receivedUsers);
                hasMoreItems = responseBody.isHasMoreItems();
                maxPosition = responseBody.getMinPosition();
            } else {
                if (!response.message().equals("Not Found")) {
                    Thread.sleep(30000 + random.nextInt(90000));
                }
                throw new IllegalStateException(response.message());
            }
            Thread.sleep(random.nextInt(1000));
        }
        return friends;
    }

    private List<Long> parseUsers(String html) {
        String PREFIX = "data-user-id=\"";
        List<Long> users = new ArrayList<>();
        int pos = 0;
        while (true) {
            pos = html.indexOf(PREFIX, pos);
            if (pos == -1) {
                return users;
            }
            pos = html.indexOf(PREFIX, pos + 1);
            int start = pos + PREFIX.length();
            int end = html.indexOf('"', start);
            String idString = html.substring(start, end);
            long id = Long.parseLong(idString);
            users.add(id);
            pos = end;
        }
    }

    @Override
    public int getRequestsLeft() {
        return 1;
    }

    @Override
    public int getSecondsUntilReset() {
        return 0;
    }
}
