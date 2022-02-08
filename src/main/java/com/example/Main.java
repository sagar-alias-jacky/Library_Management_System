package com.example;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;

public class Main {
    public static void main(String[] args) {

        ConnectionString connectionString = new ConnectionString(
                "mongodb+srv://root:toor@cluster0.d6s9y.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = null;
        MongoDatabase db = null;

        // Connect to MongoDB Atlas Cluster
        try {

            mongoClient = MongoClients.create(settings);
            // Connect to the 'library_management_system_project' database
            db = mongoClient.getDatabase("restaurants");
        } catch (Exception e) {
            System.out.println("Failed to connect to the database!\nError: " + e);
        }

        MongoCollection restaurantsCol = db.getCollection("restaurants");
        Document doc = (Document) restaurantsCol.find(eq("name", "Varun's Pizzeria")).first();
        System.out.println(doc.get("name"));
        Document doc2 = (Document) doc.get("address");
        System.out.println(doc2.get("zipcode"));

    }
}
