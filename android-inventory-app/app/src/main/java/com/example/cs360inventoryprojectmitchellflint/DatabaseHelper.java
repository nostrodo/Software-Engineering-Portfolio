package com.example.cs360inventoryprojectmitchellflint;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // class variables
    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    // constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // when DB is created, make a table for users and items

        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT)");

        db.execSQL("CREATE TABLE items (item_id INTEGER PRIMARY KEY AUTOINCREMENT, item_name TEXT, quantity INTEGER, location TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


    // user table functions

    // create user
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        return db.insert("users", null, values) != -1;
    }

    // check that user data matches when logging in
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?",
                new String[]{username, password});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    // check for existing UN only
    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE username=?",
                new String[]{username});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }


    // Item table functions

    // create item
    public boolean insertItem(String name, int quantity, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("item_name", name);
        v.put("quantity", quantity);
        v.put("location", location);
        return db.insert("items", null, v) != -1;
    }

    // loads entire list of items
    public Cursor getAllItems() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM items", null);
    }

    // Updating data for selected item
    public boolean updateItem(int id, String name, int quantity, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("item_name", name);
        v.put("quantity", quantity);
        v.put("location", location);
        return db.update("items", v, "item_id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // deletes the selected item
    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("items", "item_id=?", new String[]{String.valueOf(id)}) > 0;
    }
}