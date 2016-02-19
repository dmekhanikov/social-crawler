import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ctd.mekhanikov.crawler.FriendsService;
import ru.ifmo.ctd.mekhanikov.crawler.NamesService;
import ru.ifmo.ctd.mekhanikov.crawler.Target;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

@Target("facebook")
public class FacebookFriendsService implements FriendsService {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookFriendsService.class);
    private static final String FACEBOOK_LOGIN_PROPERTY = "facebook.login";
    private static final String FACEBOOK_PASSWORD_PROPERTY = "facebook.password";

    private WebClient webClient;
    private NamesService namesService;
    private Random random;

    public FacebookFriendsService(NamesService namesService) throws IOException {
        this.webClient = new WebClient();
        this.namesService = namesService;
        webClient.setJavaScriptEngine(new JavaScriptEngine(webClient));
        java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(Level.OFF);
        login();
        this.random = new Random(System.currentTimeMillis());
        LOG.info("Facebook service initialized");
    }

    private void login() throws IOException {
        String email = System.getProperty(FACEBOOK_LOGIN_PROPERTY);
        String password = System.getProperty(FACEBOOK_PASSWORD_PROPERTY);
        if (email == null || password == null) {
            throw new IllegalStateException("Could not find Facebook email or password properties");
        }
        LOG.info("Authenticating in Facebook");
        HtmlPage loginPage = webClient.getPage("http://m.facebook.com");
        HtmlForm form = loginPage.getForms().get(0);

        HtmlTextInput emailField = (HtmlTextInput) form.getInputsByName("email").get(0);
        HtmlPasswordInput passwordField = (HtmlPasswordInput) form.getInputsByName("pass").get(0);
        HtmlSubmitInput submitButton = (HtmlSubmitInput) form.getInputsByName("login").get(0);
        emailField.setValueAttribute(email);
        passwordField.setValueAttribute(password);
        HtmlPage homePage = submitButton.click();
        if (!homePage.getTitleText().equals("Facebook")) {
            throw new IllegalStateException("Facebook authorization failure");
        }
        LOG.info("Successfully authenticated");
    }

    @Override
    public List<Long> getFriends(long userId) throws Exception {
        LOG.info("Getting friends of user " + userId);
        List<Long> friends = new ArrayList<>();
        try {
            HtmlPage friendsPage = webClient.getPage("https://m.facebook.com/profile.php?v=friends&all=1&id=" + userId);
            LOG.info("Loaded friends list page");
            HtmlAnchor loadMoreFriendsLink = null;
            do {
                if (loadMoreFriendsLink != null) {
                    friendsPage = loadMoreFriendsLink.click();
                }
                List<Long> parsedFriends = getFriendsIds(friendsPage);
                LOG.info(parsedFriends.size()  + " users found");
                friends.addAll(parsedFriends);
                loadMoreFriendsLink = getLoadMoreFriendsLink(friendsPage);
                Thread.sleep(random.nextInt(5000));
            } while (loadMoreFriendsLink != null);
        } catch (Exception e) {
            LOG.error("Failed to get friends of user " + userId, e);
            Thread.sleep(random.nextInt(90000) + 30000);
        }
        return friends;
    }

    @Override
    public int getRequestsLeft() {
        return 1;
}

    @Override
    public int getSecondsUntilReset() {
        return 0;
    }

    private HtmlAnchor getLoadMoreFriendsLink(HtmlPage friendsPage) {
        DomElement moreFriendsDiv = friendsPage.getElementById("m_more_friends");
        if (moreFriendsDiv != null) {
            return (HtmlAnchor) moreFriendsDiv.getFirstElementChild();
        } else {
            return null;
        }
    }

    private List<Long> getFriendsIds(HtmlPage friendsPage) throws IOException {
        List<Long> friendsNames = new ArrayList<>();
        List<?> friendsLinks = friendsPage.getByXPath("//a[@class='ce']");
        if (friendsLinks.isEmpty()) {
            friendsLinks = friendsPage.getByXPath("//a[@class='bk']");
        }
        String ID_PREFIX = "/profile.php?id=";
        for (Object link : friendsLinks) {
            String href = ((HtmlAnchor) link).getHrefAttribute();
            try {
                Long id;
                if (href.startsWith(ID_PREFIX)) {
                    String idString = href.substring(ID_PREFIX.length(), href.indexOf('&'));
                    id = Long.parseLong(idString);
                } else {
                    String name = href.substring(1, href.indexOf('?'));
                    id = namesService.getId(name);
                    if (id == null) {
                        continue;
                    }
                }
                friendsNames.add(id);
            } catch (Exception e) {
                LOG.error("Failed to get friend's id", e);
            }
        }
        return friendsNames;
    }
}
