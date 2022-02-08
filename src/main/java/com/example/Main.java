package com.example;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import java.util.Scanner;

import org.bson.Document;
import com.example.Utilities;

public class Main {
    public static void main(String[] args) {

        // Variable declarations
        ConnectionString connectionString = new ConnectionString(
                "mongodb+srv://root:toor@cluster0.d6s9y.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = null;
        MongoDatabase db = null;
        MongoCollection loginCol = null;
        MongoCollection booksCol = null;
        MongoCollection studentsCol = null;
        MongoCollection staffCol = null;
        MongoCollection historyCol = null;
        Scanner sc = new Scanner(System.in);
        int choice;
        int login_status = 0;
        String login_uname;
        String login_pass;

        // Connect to MongoDB Atlas Cluster
        try {

            mongoClient = MongoClients.create(settings);
            // Connect to the 'library_management_system_project' database
            db = mongoClient.getDatabase("library_management_system_project");
        } catch (Exception e) {
            System.out.println("Failed to connect to the database!\nError: " + e);
        }

        // MongoCollection restaurantsCol = db.getCollection("restaurants");
        // Document doc = (Document) restaurantsCol.find(eq("name", "Varun's
        // Pizzeria")).first();
        // System.out.println(doc.get("name"));
        // Document doc2 = (Document) doc.get("address");
        // System.out.println(doc2.get("zipcode"));

        // Get references to the required collections
        try {
            loginCol = db.getCollection("login");
            booksCol = db.getCollection("books");
            studentsCol = db.getCollection("students");
            staffCol = db.getCollection("staff");
            historyCol = db.getCollection("history");
        } catch (Exception e) {
            System.out.println("Failed to get the required collections!\nError: " + e);
        }

        while (true) {

            System.out.println("- Library Management System -");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();
            System.out.println();
            switch (choice) {
                case 1:
                    // Login Prompt
                    do {
                        System.out.println("- Library Management System Login -");
                        System.out.print("Username: ");
                        login_uname = sc.nextLine();
                        System.out.print("Password: ");
                        login_pass = sc.nextLine();
                        Document doc = (Document) loginCol.find(eq("uname", login_uname)).first();
                        if (doc != null) {
                            if (doc.get("pass").toString().equals(login_pass)) {
                                System.out.println("Login successful! Logged in as " + login_uname + ".");
                                System.out.println();
                                login_status = 1;
                            } else {
                                System.out.println("Incorrect username or password!\nPlease try again.\n");
                            }
                        } else {
                            System.out.println("Incorrect username or password!\nPlease try again.\n");
                        }
                    } while (login_status == 0);
                    
                    break;
                case 2:
                    System.out.println("Library Management System exited.");
                    System.exit(0);
                default:
                    System.out.println("Please enter a valid choice!\n");
                    break;
            }

        }
    }
}
