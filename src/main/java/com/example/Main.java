package com.example;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
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
                                // Issue a book
                                System.out.println("- Book Issue Details -");
                                System.out.print("Enter book ID: ");
                                String book_id = sc.nextLine();
                                System.out.print("Enter student ID: ");
                                String sid = sc.nextLine();

                                // Check if a book with this book_id actually exists
                                Document doc = (Document) booksCol.find(eq("book_id", book_id)).first();
                                // If such a book_id does not exist print error
                                if (doc == null) {
                                    System.out.println(
                                            "Book ID not found!\nPlease try again with a valid book ID.\n");
                                    break;
                                }
                                if (doc.get("current_status").toString().toLowerCase().equals("issued")) {
                                    System.out.println("This book is currently not available in the library!\n");
                                    break;
                                }
                                // Check if a student with this sid actually exists
                                doc = (Document) studentsCol.find(eq("sid", sid)).first();
                                // If such a sid does not exist print error
                                if (doc == null) {
                                    System.out.println(
                                            "Student ID not found!\nPlease try again with a valid student ID.\n");
                                    break;
                                }
                                try {
                                    ObjectId histID = new ObjectId();
                                    InsertOneResult result = historyCol.insertOne(new Document()
                                            .append("_id", histID)
                                            .append("book_id", book_id)
                                            .append("sid", sid)
                                            .append("status", "pending return"));

                                    Document query = new Document().append("_id", histID);
                                    Date doi = new Date();
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(doi);
                                    cal.add(Calendar.DAY_OF_MONTH, 10);
                                    Date dod = cal.getTime();
                                    Bson updates = Updates.combine(
                                            Updates.set("doi", doi),
                                            Updates.set("dod", dod),
                                            // Updates.set("dod",
                                            // LocalDateTime.now(ZoneId.systemDefault()).plusDays(10)),
                                            Updates.set("dor", "      -"));
                                    historyCol.updateOne(query, updates);
                                    // Update the book status in books collection
                                    updates = Updates.combine(Updates.set("current_status", "issued"));
                                    booksCol.updateOne(query, updates);
                                    System.out.println("Book issued successfully!\n");
                                } catch (Exception e) {
                                    System.out.println("Book could not be issued due to an error!\n" + e
                                            + "\nPlease try again.\n");
                                }
                                break;
                            case 2:
                                // return a book
                                // read book ID from user
                                System.out.println("- Return Book -");
                                System.out.print("Enter book ID: ");
                                book_id = sc.nextLine();
                                // check if a doc with that book_id is in the collection history
                                doc = (Document) historyCol.find(eq("book_id", book_id)).sort(Sorts.descending("doi"))
                                        .first();
                                // If such a book_id does not exist print error
                                if (doc == null) {
                                    System.out.println(
                                            "Book ID not found in library issue history!\nPlease try again with a valid book ID.\n");
                                    break;
                                } else if (doc != null && !doc.get("status").toString().equals("returned")) {
                                    try {
                                        // Update dor and status in history collection
                                        Document query = new Document().append("_id", doc.get("_id"));
                                        Date dor = new Date();
                                        Bson updates = Updates.combine(
                                                Updates.set("dor", dor),
                                                Updates.set("status", "returned"));
                                        historyCol.updateOne(query, updates);
                                        // Update the book status in books collection
                                        updates = Updates
                                                .combine(Updates.set("current_status", "available in library"));
                                        booksCol.updateOne(query, updates);
                                        System.out.println("Book returned successfully!\n");
                                    } catch (Exception e) {
                                        System.out.println(
                                                "Could not return the book due to an error: " + e
                                                        + "\nPlease try again.");
                                    }
                                }
                                break;
                            case 3:
                                // Book Issue History Menu
                                int hm_choice = 0;
                                do {
                                    System.out.println("- Book Issue History -");
                                    System.out.println("1. Books pending return");
                                    System.out.println("2. Full history");
                                    System.out.println("3. Back to main menu");
                                    System.out.print("Enter a choice: ");
                                    hm_choice = sc.nextInt();
                                    sc.nextLine();
                                    System.out.println();
                                    switch (hm_choice) {
                                        case 1:
                                            // view details of issued books pending return
                                            int i = 1;
                                            // Check if history collection is empty, if empty print error message else
                                            // print pending return docs
                                            if (historyCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no book issue details in the database to be displayed!\n");
                                            } else {
                                                // retrieve pending return documents from history collection
                                                MongoCursor<Document> cursor = historyCol
                                                        .find(eq("status", "pending return"))
                                                        .sort(Sorts.descending("doi")).iterator();
                                                // print the retrieved documents in a table format
                                                System.out.println("- Books Pending Return -");
                                                System.out.println();
                                                System.out.format("%-6s%-12s%-16s%-35s%-35s%-35s%-20s\n", "", "Book ID",
                                                        "Student ID",
                                                        "Date of Issue",
                                                        "Due Date",
                                                        "Date of Return",
                                                        "Status");
                                                System.out.println();
                                                try {
                                                    while (cursor.hasNext()) {
                                                        doc = cursor.next();
                                                        System.out.format("%-6s%-12s%-16s%-35s%-35s%-35s%-20s\n",
                                                                String.valueOf(i) + ".",
                                                                doc.get("book_id").toString().toUpperCase(),
                                                                doc.get("sid").toString().toUpperCase(),
                                                                doc.get("doi").toString().toUpperCase(),
                                                                doc.get("dod").toString().toUpperCase(),
                                                                doc.get("dor").toString().toUpperCase(),
                                                                doc.get("status").toString().toUpperCase());
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
                                            }
                                            break;
                                        case 2:
                                            // view full book issue history
                                            i = 1;
                                            // Check if history collection is empty, if empty print error message else
                                            // print all docs
                                            if (historyCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no book issue details in the database to be displayed!\n");
                                            } else {
                                                // retrieve all documents from history collection
                                                MongoCursor<Document> cursor = historyCol.find()
                                                        .sort(Sorts.descending("doi")).iterator();
                                                // print the retrieved documents in a table format
                                                System.out.println("- Book Issue History Table -");
                                                System.out.println();
                                                System.out.format("%-6s%-12s%-16s%-35s%-35s%-35s%-20s\n", "", "Book ID",
                                                        "Student ID",
                                                        "Date of Issue",
                                                        "Due Date",
                                                        "Date of Return",
                                                        "Status");
                                                System.out.println();
                                                try {
                                                    while (cursor.hasNext()) {
                                                        doc = cursor.next();
                                                        System.out.format("%-6s%-12s%-16s%-35s%-35s%-35s%-20s\n",
                                                                String.valueOf(i) + ".",
                                                                doc.get("book_id").toString().toUpperCase(),
                                                                doc.get("sid").toString().toUpperCase(),
                                                                doc.get("doi").toString().toUpperCase(),
                                                                doc.get("dod").toString().toUpperCase(),
                                                                doc.get("dor").toString().toUpperCase(),
                                                                doc.get("status").toString().toUpperCase());
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
                                            }
                                            break;
                                        case 3:
                                            break;
                                        default:
                                            System.out.println("Please enter a valid choice!\n");
                                            break;
                                    }
                                } while (hm_choice != 3);
                                break;
                            case 4:
                                // Display books database menu
                                int bookdb_choice = 0;
                                // books database menu loops till user chooses option to go back to main menu
                                do {
                                    // books database menu
                                    System.out.println("- Books Database Menu -");
                                    System.out.println("1. View all book details");
                                    System.out.println("2. Add book");
                                    System.out.println("3. Remove book");
                                    System.out.println("4. Search book");
                                    System.out.println("5. Back to main menu");
                                    // Enter choice
                                    System.out.print("Enter your choice: ");
                                    bookdb_choice = sc.nextInt();
                                    sc.nextLine();
                                    System.out.println();

                                    switch (bookdb_choice) {
                                        case 1:
                                            // display all book details
                                            int i = 1;
                                            // Check if books collection is empty, if empty print error message else
                                            // print all docs
                                            if (booksCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no book details in the database to be displayed!\nPlease try again after adding a new book.\n");
                                            } else {
                                                // retrieve all documents from books collection
                                                MongoCursor<Document> cursor = booksCol.find()
                                                        .sort(Sorts.ascending("book_name")).iterator();
                                                // print the retrieved documents in a table format
                                                System.out.println("- Book Details Table -");
                                                System.out.println();
                                                System.out.format("%-6s%-40s%-16s%-20s%-11s%-11s%-20s\n", "", "Name",
                                                        "BID",
                                                        "Author",
                                                        "Edition",
                                                        "Year",
                                                        "Status");
                                                System.out.println();
                                                try {
                                                    while (cursor.hasNext()) {
                                                        doc = cursor.next();
                                                        System.out.format("%-6s%-40s%-16s%-20s%-11s%-11s%-20s\n",
                                                                String.valueOf(i) + ".",
                                                                doc.get("book_name").toString().toUpperCase(),
                                                                doc.get("book_id").toString().toUpperCase(),
                                                                doc.get("author").toString().toUpperCase(),
                                                                doc.get("edition").toString().toUpperCase(),
                                                                doc.get("pub_year"),
                                                                doc.get("current_status").toString().toUpperCase());
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
                                            }
                                            break;
                                        case 2:
                                            // Add a new book
                                            // Enter the new book details
                                            System.out.println("- New Book Details -");
                                            System.out.print("Enter book name: ");
                                            String name = sc.nextLine().toLowerCase();
                                            System.out.print("Enter book ID: ");
                                            String bid = sc.nextLine().toLowerCase();
                                            System.out.print("Enter author name: ");
                                            String author = sc.nextLine().toLowerCase();
                                            System.out.print("Enter edition: ");
                                            String edition = sc.nextLine();
                                            System.out.print("Enter year of publication: ");
                                            String year = sc.nextLine();

                                            // book_id has to be unique so check if a doc with the entered bid already
                                            // exists
                                            doc = (Document) booksCol.find(eq("book_id", bid)).first();
                                            // if such a doc exists print error else insert the new doc
                                            if (doc != null) {
                                                System.out.println(
                                                        "This book ID already exists. Book ID must be unique!\nPlease try again with a unique book ID.\n");
                                                break;
                                            } else {
                                                // Insert new document with the entered info into the book collection
                                                try {
                                                    InsertOneResult result = booksCol.insertOne(new Document()
                                                            .append("_id", new ObjectId())
                                                            .append("book_name", name)
                                                            .append("book_id", bid)
                                                            .append("author", author)
                                                            .append("edition", edition)
                                                            .append("pub_year", year)
                                                            .append("current_status", "available in library"));
                                                    System.out.println("New book added successfully!\n");
                                                } catch (MongoException e) {
                                                    System.err
                                                            .println("Unable to add new book due to an error: " + e);
                                                    System.out.println();
                                                }
                                            }
                                            break;
                                        case 3:
                                            // Remove a book from the books collection
                                            // Enter the book_id of the book to be removed
                                            System.out.println("- Remove a book -");
                                            System.out.print("Enter the book ID of the book to be removed: ");
                                            bid = sc.nextLine().toLowerCase();

                                            // Check if a book with this book_id actually exists
                                            doc = (Document) booksCol.find(eq("book_id", bid)).first();
                                            // If such a book_id does not exist print error else delete the doc
                                            if (doc == null) {
                                                System.out.println(
                                                        "Book ID not found!\nPlease try again with a valid book ID.\n");
                                                break;
                                            } else {
                                                // Check if book is currently issued
                                                BasicDBObject criteria = new BasicDBObject();
                                                criteria.append("book_id", bid);
                                                criteria.append("current_status", "issued");
                                                doc = (Document) booksCol.find(criteria).first();
                                                // if yes print error
                                                if (doc != null) {
                                                    System.out.println(
                                                            "This book is currently issued to a student!\nThe book cannot be removed until it has been returned.\n");
                                                    break;
                                                } else {
                                                    // delete the particular document from the books collection
                                                    Bson query = eq("book_id", bid);
                                                    try {
                                                        booksCol.deleteOne(query);
                                                        System.out
                                                                .println("Book removed successfully!\n");
                                                    } catch (MongoException e) {
                                                        System.err
                                                                .println("Unable to remove book due to an error: " + e);
                                                        System.out.println();
                                                    }
                                                }
                                            }
                                            break;
                                        case 4:
                                            // search book
                                            System.out.println("- Search Book -");
                                            System.out.print("Enter book ID: ");
                                            book_id = sc.nextLine();
                                            System.out.println();
                                            // Check if books collection is empty, if empty print error message else
                                            // print required doc
                                            if (booksCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no book details in the database to be displayed!\nPlease try again after adding a new book.\n");
                                            } else {
                                                // retrieve the matching document from books collection
                                                try {
                                                    doc = (Document) booksCol.find(eq("book_id", book_id))
                                                            .sort(Sorts.ascending("book_name")).first();
                                                    // check if a book with the entered book_id exists
                                                    if (doc == null) {
                                                        System.out.println(
                                                                "Book not found!\nPlease try again with a valid book ID.\n");
                                                        break;
                                                    } else {
                                                        // print the retrieved document in a table format
                                                        System.out.println("- Book Details -");
                                                        System.out.println();
                                                        System.out.format("%-40s%-16s%-20s%-11s%-11s%-20s\n",
                                                                "Name",
                                                                "BID",
                                                                "Author",
                                                                "Edition",
                                                                "Year",
                                                                "Status");
                                                        System.out.println();
                                                        System.out.format("%-40s%-16s%-20s%-11s%-11s%-20s\n",
                                                                doc.get("book_name").toString().toUpperCase(),
                                                                doc.get("book_id").toString().toUpperCase(),
                                                                doc.get("author").toString().toUpperCase(),
                                                                doc.get("edition").toString().toUpperCase(),
                                                                doc.get("pub_year"),
                                                                doc.get("current_status").toString().toUpperCase());
                                                        System.out.println();
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Could not retrieve the data due to an error: "
                                                            + e + "\nPlease try again.");
                                                }
                                            }
                                            break;
                                        case 5:
                                            break;
                                        default:
                                            System.out.println("Please enter a valid choice!\n");
                                            break;
                                    }
                                } while (bookdb_choice != 5);
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
                                    System.out.println("4. Search student");
                                    System.out.println("5. Back to main menu");
                                    // Enter choice
                                    System.out.print("Enter your choice: ");
                                    sdb_choice = sc.nextInt();
                                    sc.nextLine();
                                    System.out.println();

                                    switch (sdb_choice) {
                                        case 1:
                                            // display all student details
                                            int i = 1;
                                            // Check if students collection is empty, if empty print error message else
                                            // print all docs
                                            if (studentsCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no student details in the database to be displayed!\nPlease try again after adding a new student.\n");
                                            } else {
                                                // retrieve all documents from student collection
                                                MongoCursor<Document> cursor = studentsCol.find()
                                                        .sort(Sorts.ascending("name")).iterator();
                                                // print the retrieved documents in a table format
                                                System.out.println("- Student Details Table -");
                                                System.out.println();
                                                System.out.format("%-6s%-20s%-16s%-15s%-11s\n", "", "Name", "SID",
                                                        "Branch",
                                                        "Semester");
                                                System.out.println();
                                                try {
                                                    while (cursor.hasNext()) {
                                                        doc = cursor.next();
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
                                            }
                                            break;
                                        case 2:
                                            // Add a new student
                                            // Enter the new student details
                                            System.out.println("- New Student Details -");
                                            System.out.print("Enter student name: ");
                                            String name = sc.nextLine().toLowerCase();
                                            System.out.print("Enter student SID: ");
                                            sid = sc.nextLine().toLowerCase();
                                            System.out.print("Enter branch: ");
                                            String branch = sc.nextLine().toLowerCase();
                                            System.out.print("Enter semester: ");
                                            String sem = sc.nextLine();

                                            // SID has to be unique so check if a doc with the entered sid already
                                            // exists
                                            doc = (Document) studentsCol.find(eq("sid", sid)).first();
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
                                                // Check if student has unreturned book issues
                                                BasicDBObject criteria = new BasicDBObject();
                                                criteria.append("sid", sid);
                                                criteria.append("status", "pending return");
                                                doc = (Document) historyCol.find(criteria).first();
                                                // if yes print error
                                                if (doc != null) {
                                                    System.out.println(
                                                            "This student has book issues pending return!\nThe student cannot be removed until all the issued books have been returned.\n");
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
                                                                .println("Unable to remove student due to an error: "
                                                                        + e);
                                                        System.out.println();
                                                    }
                                                }

                                            }
                                            break;
                                        case 4:
                                            // search student
                                            System.out.println("- Search Student -");
                                            System.out.print("Enter student ID: ");
                                            sid = sc.nextLine();
                                            System.out.println();
                                            // Check if students collection is empty, if empty print error message else
                                            // print required doc
                                            if (studentsCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no student details in the database to be displayed!\nPlease try again after adding a new student.\n");
                                            } else {
                                                // retrieve the matching document from students collection
                                                try {
                                                    doc = (Document) studentsCol.find(eq("sid", sid))
                                                            .sort(Sorts.ascending("name")).first();
                                                    // check if a student with the entered sid exists
                                                    if (doc == null) {
                                                        System.out.println(
                                                                "Student not found!\nPlease try again with a valid student ID.\n");
                                                        break;
                                                    } else {
                                                        // print the retrieved document in a table format
                                                        System.out.println("- Student Details -");
                                                        System.out.println();
                                                        System.out.format("%-20s%-16s%-15s%-11s\n", "Name",
                                                                "SID",
                                                                "Branch",
                                                                "Semester");
                                                        System.out.println();
                                                        System.out.format("%-20s%-16s%-15s%-11s\n",
                                                                doc.get("name").toString().toUpperCase(),
                                                                doc.get("sid").toString().toUpperCase(),
                                                                doc.get("branch").toString().toUpperCase(),
                                                                doc.get("sem").toString().toUpperCase());
                                                        System.out.println();
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("Could not retrieve the data due to an error: "
                                                            + e + "\nPlease try again.");
                                                }
                                            }
                                            break;
                                        case 5:
                                            break;
                                        default:
                                            System.out.println("Please enter a valid choice!\n");
                                            break;
                                    }
                                } while (sdb_choice != 5);
                                break;
                            case 6:
                                // Display staff database menu
                                int stfdb_choice = 0;
                                // Staff database menu loops till user chooses option to go back to main menu
                                do {
                                    // Staff database menu
                                    System.out.println("- Staff Database Menu -");
                                    System.out.println("1. View all staff details");
                                    System.out.println("2. Add staff");
                                    System.out.println("3. Remove staff");
                                    System.out.println("4. Back to main menu");
                                    // Enter choice
                                    System.out.print("Enter your choice: ");
                                    stfdb_choice = sc.nextInt();
                                    sc.nextLine();
                                    System.out.println();

                                    switch (stfdb_choice) {
                                        case 1:
                                            // display all staff details
                                            int i = 1;
                                            // Check if staff collection is empty, if empty print error message else
                                            // print all docs
                                            if (staffCol.countDocuments() <= 0) {
                                                System.out.println(
                                                        "There are no staff details in the database to be displayed!\nPlease try again after adding a new staff.\n");
                                            } else {
                                                // retrieve all documents from staff collection
                                                MongoCursor<Document> cursor = staffCol.find()
                                                        .sort(Sorts.ascending("name")).iterator();
                                                // print the retrieved documents in a table format
                                                System.out.println("- Staff Details Table -");
                                                System.out.println();
                                                System.out.format("%-6s%-20s%-16s%-15s\n", "", "Name", "STFID",
                                                        "Department");
                                                System.out.println();
                                                try {
                                                    while (cursor.hasNext()) {
                                                        doc = cursor.next();
                                                        System.out.format("%-6s%-20s%-16s%-15s\n",
                                                                String.valueOf(i) + ".",
                                                                doc.get("name").toString().toUpperCase(),
                                                                doc.get("stfid").toString().toUpperCase(),
                                                                doc.get("dept").toString().toUpperCase());
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
                                            }
                                            break;
                                        case 2:
                                            // Add a new staff
                                            // Enter the new staff details
                                            System.out.println("- New Staff Details -");
                                            System.out.print("Enter staff name: ");
                                            String name = sc.nextLine().toLowerCase();
                                            System.out.print("Enter staff STFID: ");
                                            String stfid = sc.nextLine().toLowerCase();
                                            System.out.print("Enter department: ");
                                            String dept = sc.nextLine().toLowerCase();

                                            // STFID has to be unique so check if a doc with the entered stfid already
                                            // exists
                                            doc = (Document) staffCol.find(eq("stfid", stfid)).first();
                                            // if such a doc exists print error else insert the new doc
                                            if (doc != null) {
                                                System.out.println(
                                                        "This STFID already exists. Staff STFID must be unique!\nPlease try again with a unique STFID.\n");
                                                break;
                                            } else {
                                                // Insert new document with the entered info into the staff collection
                                                try {
                                                    InsertOneResult result = staffCol.insertOne(new Document()
                                                            .append("_id", new ObjectId())
                                                            .append("name", name)
                                                            .append("stfid", stfid)
                                                            .append("dept", dept));
                                                    System.out.println("New staff added successfully!\n");
                                                } catch (MongoException e) {
                                                    System.err
                                                            .println("Unable to add new staff due to an error: " + e);
                                                    System.out.println();
                                                }
                                            }
                                            break;
                                        case 3:
                                            // Remove a staff from the staff collection
                                            // Enter the stfid of the staff to be removed
                                            System.out.println("- Remove a staff -");
                                            System.out.print("Enter the STFID of the staff to be removed: ");
                                            stfid = sc.nextLine().toLowerCase();

                                            // Check if a staff with this stfid actually exists
                                            doc = (Document) staffCol.find(eq("stfid", stfid)).first();
                                            // If such a STFID does not exist print error else delete the doc
                                            if (doc == null) {
                                                System.out.println(
                                                        "STFID not found!\nPlease try again with a valid STFID.\n");
                                                break;
                                            } else {
                                                // delete the particular document from the staff collection
                                                Bson query = eq("stfid", stfid);
                                                try {
                                                    staffCol.deleteOne(query);
                                                    System.out
                                                            .println("Staff removed successfully!\n");
                                                } catch (MongoException e) {
                                                    System.err
                                                            .println("Unable to remove staff due to an error: " + e);
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
                                } while (stfdb_choice != 4);
                                break;
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
