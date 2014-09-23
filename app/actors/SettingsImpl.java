package actors;

import akka.actor.Extension;
import com.typesafe.config.Config;

public class SettingsImpl implements Extension {

    SettingsImpl(Config config) {
        this.SENTIMENT_URL = config.getString("sentiment.url");
        this.TWEET_URL = config.getString("tweet.url");
    }

    public final String SENTIMENT_URL;
    public final String TWEET_URL;
}
