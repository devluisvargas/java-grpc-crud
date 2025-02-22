package com.devluis.blog;

import com.devluis.blog.config.AppProperties;
import com.devluis.blog.config.GrpcServer;
import com.devluis.blog.datasource.MongoDataSource;
import com.devluis.blog.service.BlogServiceImpl;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws InterruptedException, IOException {
        AppProperties.getInstance();
        MongoDataSource mongoDataSource = MongoDataSource.getInstance();
        BlogServiceImpl blogService = new BlogServiceImpl(mongoDataSource);
        GrpcServer.initServer(blogService);
    }
}
