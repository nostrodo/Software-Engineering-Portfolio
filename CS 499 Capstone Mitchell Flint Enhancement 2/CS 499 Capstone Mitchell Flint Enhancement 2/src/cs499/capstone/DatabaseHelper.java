package cs499.capstone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    // class variables
    private static final String DATABASE_NAME = "app.db";
    private Connection db;

    // constructor
    public DatabaseHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            onCreate(db);
        } catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
    }

    public void onCreate(Connection db) {

        // when DB is created, make a table for users and items
        try {
            Statement statement = db.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS items (item_id INTEGER PRIMARY KEY AUTOINCREMENT, item_name TEXT, quantity INTEGER,"
            		+ " location TEXT)");

            statement.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    // user table functions

    // create user
    public boolean insertUser(String username, String password) {
        try {
            PreparedStatement values = db.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            values.setString(1, username);
            values.setString(2, password);
            return values.executeUpdate() != -1;
        } catch (SQLException e) { return false; }
    }

    // check that user data matches when logging in
    public boolean checkUser(String username, String password) {
        try {
            PreparedStatement c = db.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            c.setString(1, username);
            c.setString(2, password);
            ResultSet result = c.executeQuery();
            boolean exists = result.next();
            result.close();
            c.close();
            return exists;
        } catch (SQLException e) { return false; }
    }

    // check for existing UN only
    public boolean userExists(String username) {
        try {
            PreparedStatement c = db.prepareStatement("SELECT * FROM users WHERE username=?");
            c.setString(1, username);
            ResultSet result = c.executeQuery();
            boolean exists = result.next();
            result.close();
            c.close();
            return exists;
        } catch (SQLException e) { return false; }
    }


    // item table functions

    // create item
    public boolean insertItem(String name, int quantity, String location) {
        try {
            PreparedStatement v = db.prepareStatement("INSERT INTO items (item_name, quantity, location) VALUES (?, ?, ?)");
            v.setString(1, name);
            v.setInt(2, quantity);
            v.setString(3, location);
            return v.executeUpdate() != -1;
        } catch (SQLException e) { return false; }
    }

    // loads entire list of items
    public ResultSet getAllItems() {
        try {
            Statement c = db.createStatement();
            return c.executeQuery("SELECT * FROM items");
        } catch (SQLException e) { return null; }
    }

    // updating data for selected item
    public boolean updateItem(int id, String name, int quantity, String location) {
        try {
            PreparedStatement v = db.prepareStatement("UPDATE items SET item_name=?, quantity=?, location=? WHERE item_id=?");
            v.setString(1, name);
            v.setInt(2, quantity);
            v.setString(3, location);
            v.setInt(4, id);
            return v.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // deletes the selected item
    public boolean deleteItem(int id) {
        try {
            PreparedStatement statement = db.prepareStatement("DELETE FROM items WHERE item_id=?");
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // clears the current claim after submission
    public boolean clearItems() {
        try {
            Statement statement = db.createStatement();
            statement.executeUpdate("DELETE FROM items");
            statement.close();
            return true;
        } catch (SQLException e) { return false; }
    }
}
