package com.devluis.blog.service;

import com.devluis.blog.config.AppProperties;
import com.devluis.blog.datasource.MongoDataSource;
import com.devluis.blog.utils.AppConstant;
import com.google.protobuf.Empty;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@Slf4j
public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    public BlogServiceImpl(MongoDataSource mongoDataSource) {
        MongoClient mongoClient = mongoDataSource.getMongoClient();
        String databaseProperty = AppProperties.getProperty("db.mongo.database");
        MongoDatabase database = mongoClient.getDatabase(databaseProperty);
        mongoCollection = database.getCollection("db.mongo.blog.collection");
        log.info("Initialized BlogServiceImpl with MongoDB collection: {}", "db.mongo.blog.collection");
    }

    @Override
    public void createBlog(Blog request, StreamObserver<BlogId> responseObserver) {
        log.info("Received createBlog request: {}", request);
        Document doc = new Document(AppConstant.AUTHOR_FIELD, request.getAuthor())
                .append(AppConstant.TITLE_FIELD, request.getTitle())
                .append(AppConstant.CONTENT_FIELD, request.getContent());
        InsertOneResult result;
        try {
            result = mongoCollection.insertOne(doc);
            log.info("Inserted document: {}", doc);
        } catch (MongoException e) {
            log.error("Failed to insert document: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(e.getLocalizedMessage())
                    .asRuntimeException());
            return;
        }
        if (!result.wasAcknowledged() || result.getInsertedId() == null) {
            log.error("Insert operation was not acknowledged or no ID returned");
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Blog couldn't be created")
                    .asRuntimeException());
            return;
        }

        String id = result.getInsertedId().asObjectId().getValue().toString();
        log.info("Blog created with ID: {}", id);

        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(BlogId request, StreamObserver<Blog> responseObserver) {
        log.info("Received readBlog request for ID: {}", request.getId());
        if (request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The Blog ID cannot be empty")
                    .asRuntimeException());
            return;
        }
        String id = request.getId();
        Document result = mongoCollection.find(eq(AppConstant.ID_FIELD, new ObjectId(id))).first();
        if (result == null) {
            log.error("Blog not found for ID: {}", id);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog was not found")
                    .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }

        log.info("Found blog: {}", result);

        responseObserver.onNext(Blog.newBuilder()
                .setId(id)
                .setAuthor(result.getString(AppConstant.AUTHOR_FIELD))
                .setTitle(result.getString(AppConstant.TITLE_FIELD))
                .setContent(result.getString(AppConstant.CONTENT_FIELD))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(Blog request, StreamObserver<Empty> responseObserver) {
        log.info("Received updateBlog request: {}", request);
        if (request.getId().isEmpty()) {
            log.error("Blog ID is empty");
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The Blog ID cannot be empty")
                    .asRuntimeException());
            return;
        }
        String id = request.getId();
        Document result = mongoCollection.findOneAndUpdate(eq(AppConstant.ID_FIELD, new ObjectId(id)),
                combine(
                        set(AppConstant.AUTHOR_FIELD, request.getAuthor()),
                        set(AppConstant.TITLE_FIELD, request.getTitle()),
                        set(AppConstant.CONTENT_FIELD, request.getContent())
                )
        );
        if (result == null) {
            log.error("Blog not found for ID: {}", id);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog was not found")
                    .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }

        log.info("Updated blog: {}", result);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(BlogId request, StreamObserver<Empty> responseObserver) {
        log.info("Received deleteBlog request for ID: {}", request.getId());
        if (request.getId().isEmpty()) {
            log.error("Blog ID is empty");
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The Blog ID cannot be empty")
                    .asRuntimeException());
            return;
        }

        String id = request.getId();
        DeleteResult result;
        try {
            result = mongoCollection.deleteOne(eq(AppConstant.ID_FIELD, new ObjectId(id)));
            log.info("Deleted blog count: {}", result.getDeletedCount());
        } catch (MongoException e) {
            log.error("Failed to delete blog with ID: {}", id, e);
            responseObserver.onError(Status
                    .INTERNAL
                    .withDescription("The blog couldn't be deleted")
                    .asRuntimeException());
            return;
        }
        if (!result.wasAcknowledged()) {
            log.error("Delete operation was not acknowledged for ID: {}", id);
            responseObserver.onError(Status
                    .INTERNAL
                    .withDescription("The blog couldn't be deleted")
                    .asRuntimeException());
            return;
        }
        if (result.getDeletedCount() == 0) {
            log.error("Blog not found for ID: {}", id);
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog was not found")
                    .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(Empty request, StreamObserver<Blog> responseObserver) {
        log.info("Received listBlogs request");
        for (Document document : this.mongoCollection.find()) {
            log.info("Listing blog: {}", document);
            responseObserver.onNext(Blog.newBuilder()
                    .setId(document.getObjectId(AppConstant.ID_FIELD).toString())
                    .setTitle(document.getString(AppConstant.TITLE_FIELD))
                    .setAuthor(document.getString(AppConstant.AUTHOR_FIELD))
                    .setContent(document.getString(AppConstant.CONTENT_FIELD))
                    .build());
        }
        responseObserver.onCompleted();
    }
}
