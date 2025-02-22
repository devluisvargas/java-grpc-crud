package com.devluis.blog.datasource;

import com.devluis.blog.config.AppProperties;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;

public class MongoDataSource {

    @Getter
    private final MongoClient mongoClient;
    private static  MongoDataSource mongoDataSource;

    private MongoDataSource(){
        String url = AppProperties.getProperty("db.mongo.url");
        this.mongoClient = MongoClients.create(url);
    }
    public static synchronized MongoDataSource getInstance(){
        if(mongoDataSource==null){
            mongoDataSource = new MongoDataSource();
        }
        return mongoDataSource;
    }
}
