package com.devluis.blog.config;

import java.io.IOException;
import java.util.Properties;

public class AppProperties {
    private static Properties properties;
    private static AppProperties appProperties;
    private AppProperties() {
        properties = new Properties();
        java.net.URL url = ClassLoader.getSystemResource("application.properties");
        try {
            properties.load(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static synchronized void getInstance(){
        if(appProperties == null){
            appProperties = new AppProperties();
        }
    }

    public static String getProperty(String property){
        return properties.getProperty(property);
    }
}
