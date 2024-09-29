package Reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsReader implements Reader{
    private final Properties properties;

    public SettingsReader(String settingsPath) throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(settingsPath));
    }

    @Override
    public int getPort (){
        return Integer.parseInt(properties.getProperty("port"));
    }

    @Override
    public String getHost(){
        return properties.getProperty("host");
    }
}
