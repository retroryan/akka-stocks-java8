package actors;

import akka.actor.Extension;
import com.typesafe.config.Config;

public class SettingsImpl implements Extension {

    SettingsImpl(Config config) {
        this.hostname = config.getString("stock.hostname");
        this.port = config.getString("stock.port");
    }


  public final String hostname;
  public final String port;
}
