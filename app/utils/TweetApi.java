package utils;

import play.libs.ws.WSRequestHolder;
import play.libs.ws.ning.NingWSClient;

public class TweetApi {

    public static NingWSClient wsClient = WSUtils.getWSClient();

    public static WSRequestHolder callTweetWS(String url) {
        return wsClient.url(url);
    }
}
