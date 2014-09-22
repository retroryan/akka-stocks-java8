package actors;

import akka.actor.Extension;
import com.typesafe.config.Config;

public class SettingsImpl implements Extension {

    SettingsImpl(Config config) {
        this.SomeSetting = config.getString("some.setting");
    }


  public final String SomeSetting;
}
