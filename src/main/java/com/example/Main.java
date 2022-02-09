package com.example;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import java.util.Scanner;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

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
            System.exit(1);
        }

        // Get references to the required collections
        try {
            loginCol = db.getCollection("login");
            booksCol = db.getCollection("books");
            studentsCol = db.getCollection("students");
            staffCol = db.getCollection("staff");
            historyCol = db.getCollection("history");
        } catch (Exception e) {
            System.out.println("Failed to get the required collections!\nError: " + e);
            System.exit(1);
        }

        // Main Loop, comes back here after user logs out
        while (true) {
            // First Menu
            System.out.println("- Library Management System -");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            // Enter choice
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();
            System.out.println();
            switch (choice) {
                case 1:
                    // Login Prompt loops while login_status = 0
                    do {
                        System.out.println("- Library Management System Login -");
                        // Enter username
                        System.out.print("Username: ");
                        login_uname = sc.nextLine();
                        // Enter password
                        System.out.print("Password: ");
                        login_pass = sc.nextLine();
                        // retrieve document from login collection with uname = username entered by the
                        // user
                        Document doc = (Document) loginCol.find(eq("uname", login_uname)).first();
                        // if such a document exists that document will be returned else null
                        if (doc != null) {
                            // doc exists now check if the password entered by the user equals the pass
                            // value for that doc in login collection
                            if (doc.get("pass").toString().equals(login_pass)) {
                                // pass matches login successful
                                System.out.println("Login successful! Logged in as " + login_uname + ".");
                                System.out.println();
                                // set login status as 1 to break out of login prompt loop
                                login_status = 1;
                            } else {
                                // pass does not match
                                System.out.println("Incorrect username or password!\nPlease try again.\n");
                            }
                        } else {
                            // no doc with uname = username entered by user exists
                            System.out.println("Incorrect username or password!\nPlease try again.\n");
                        }
                    } while (login_status == 0);

                    // Main menu, loops until user is logged in
                    while (login_status == 1) {
                        System.out.println("- Library Management System Menu-");
                        System.out.println("1. Issue Book");
                        System.out.println("2. Return Book");
                        System.out.println("3. Book Issue History");
                        System.out.println("4. Books Database");
                        System.out.println("5. Student Database");
                        System.out.println("6. Staff Database");
                        System.out.println("7. Logout");
                        // Enter choice
                        System.out.print("Enter your choice: ");
                        choice = sc.nextInt();
                        sc.nextLine();
                        System.out.println();
                        switch (choice) {
                            case 1:
                                // TODO issue book
                                break;
                            case 2:
                                // TODO return book
                                break;
                            case 3:
                                // TODO history menu
                                break;
                            case 4:
                                // todo books db menu
                                break;
                            case 5:
                                // Display student database menu
                                int sdb_choice = 0;
                                // Student database menu loops till user chooses option to go back to main menu
                                do {
                                    // Student database menu
                                    System.out.println("- Student Database Menu -");
                                    System.out.println("1. View all student details");
                                    System.out.println("2. Add student");
                                    System.out.println("3. Remove student");
                                    System.out.println("4. Back to main menu");
                                    // Enter choice
                                    System.out.print("Enter your choice: ");
                                    sdb_choice = sc.nextInt();
                                    sc.nextLine();
                                    System.out.println();

                                    switch (sdb_choice) {
                                        case 1:
                                            // display all student details
                                            int i = 1;
                                            // retrieve all documents from student collection
                                            MongoCursor<Document> cursor = studentsCol.find()
                                                    .sort(Sorts.ascending("name")).iterator();
                                            // print the retrieved documents in a table format
                                            System.out.println("- Student Details Table -");
                                            System.out.println();
                                            System.out.format("%-6s%-20s%-16s%-15s%-11s\n", "", "Name", "SID", "Branch",
                                                    "Semester");
                                            System.out.println();
                                            try {
                                                while (cursor.hasNext()) {
                                                    Document doc = cursor.next();
                                                    System.out.format("%-6s%-20s%-16s%-15s%-11s\n",
                                                            String.valueOf(i) + ".",
                                                            doc.get("name").toString().toUpperCase(),
                                                            doc.get("sid").toString().toUpperCase(),
                                                            doc.get("branch").toString().toUpperCase(),
                                                            doc.get("sem").toString().toUpperCase());
                                                    i++;
                                                }
                                                System.out.println();
                                            } catch (Exception e) {
                                                System.out.println("Error: " + e
                                                        + "\nCould not retrieve data from the database!\nPlease try again.\n");
                                                System.out.println();
                                            } finally {
                                                // close the cursor
                                                cursor.close();
                                            }
                                            break;
                                        case 2:
                                            // Add a new student
                                            // Enter the new student details
                                            System.out.println("- New Student Details -");
                                            System.out.print("Enter student name: ");
                                            String name = sc.nextLine();
                                            System.out.print("Enter student SID: ");
                                            String sid = sc.nextLine();
                                            System.out.print("Enter branch: ");
                                            String branch = sc.nextLine();
                                            System.out.print("Enter semester: ");
                                            String sem = sc.nextLine();

                                            // SID has to be unique so check if a doc with the entered sid already
                                            // exists
                                            Document doc = (Document) studentsCol.find(eq("sid", sid)).first();
                                            // if such a doc exists print error else insert the new doc
                                            if (doc != null) {
                                                System.out.println(
                                                        "This SID already exists. Student SID must be unique!\nPlease try again with a unique SID.\n");
                                                break;
                                            } else {
                                                // Insert new document with the entered info into the student collection
                                                try {
                                                    InsertOneResult result = studentsCol.insertOne(new Document()
                                                            .append("_id", new ObjectId())
                                                            .append("name", name)
                                                            .append("sid", sid)
                                                            .append("branch", branch)
                                                            .append("sem", sem));
                                                    System.out.println("New student added successfully!\n");
                                                } catch (MongoException e) {
                                                    System.err
                                                            .println("Unable to add new student due to an error: " + e);
                                                    System.out.println();
                                                }
                                            }
                                            break;
                                        case 3:
                                            // Remove a student from the students collection
                                            // Enter the sid of the student to be removed
                                            System.out.println("- Remove a student -");
                                            System.out.print("Enter the SID of the student to be removed: ");
                                            sid = sc.nextLine().toLowerCase();

                                            // Check if a student with this sid actually exists
                                            doc = (Document) studentsCol.find(eq("sid", sid)).first();
                                            // If such a SID does not exist print error else delete the doc
                                            if (doc == null) {
                                                System.out.println(
                                                        "SID not found!\nPlease try again with a valid SID.\n");
                                                break;
                                            } else {
                                                // delete the particular document from the students collection
                                                Bson query = eq("sid", sid);
                                                try {
                                                    studentsCol.deleteOne(query);
                                                    System.out
                                                            .println("Student removed successfully!\n");
                                                } catch (MongoException e) {
                                                    System.err
                                                            .println("Unable to remove student due to an error: " + e);
                                                    System.out.println();
                                                }
                                            }
                                            break;
                                        case 4:
                                            break;
                                        default:
                                            System.out.println("Please enter a valid choice!\n");
                                            break;
                                    }
                                } while (sdb_choice != 4);

                                break;
                            case 6:
                                // todo staff db menu
                            case 7:
                                // logout
                                // set login_status = 0 to break out of the main menu loop and go back to first
                                // menu
                                login_status = 0;
                                System.out.println("Logged out successfully!\n");
                                break;
                            default:
                                System.out.println("Please enter a valid choice!\n");
                                break;
                        }
                    }
                    break;
                case 2:
                    // exit out of the library management system program
                    System.out.println("Library Management System exited.");
                    System.exit(0);
                default:
                    System.out.println("Please enter a valid choice!\n");
                    break;
            }

        }
    }
}
