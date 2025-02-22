package com.devluis.blog.config;

import com.devluis.blog.datasource.MongoDataSource;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class GrpcServer {

    public static void initServer(BindableService serviceDefinition) throws InterruptedException, IOException {
        String port = AppProperties.getProperty("grpc.server.port");
        Server server = ServerBuilder
                .forPort(Integer.parseInt(port))
                .addService(serviceDefinition)
                .build();

        server.start();
        log.info("Server Started");
        log.info("Listening in port: {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Receiver Shutdown request");
            MongoDataSource mongoDataSource = MongoDataSource.getInstance();
            mongoDataSource.getMongoClient().close();
            server.shutdown();
            log.info("Server stopped");
        }));

        server.awaitTermination();
    }
}
