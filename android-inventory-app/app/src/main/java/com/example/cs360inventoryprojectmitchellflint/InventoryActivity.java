package com.example.cs360inventoryprojectmitchellflint;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;


public class InventoryActivity extends AppCompatActivity {

    // class variables
    DatabaseHelper db;
    TableLayout table;
    ArrayList<Integer> ids = new ArrayList<>();
    int selectedIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // initialize database
        db = new DatabaseHelper(this);

        // references for the inventory table, and 2 buttons
        table = findViewById(R.id.inventoryTable);
        Button addButton = findViewById(R.id.ButtonAddItem);
        Button editButton = findViewById(R.id.ButtonEditItem);

        // listener for the add item button
        addButton.setOnClickListener(v -> addItem());

        // listener for the edit item button; only works when an item is selected
        editButton.setOnClickListener(v -> {
            if (selectedIndex == -1 || selectedIndex >= ids.size()) {
                Toast.makeText(this, "Select an item first", Toast.LENGTH_SHORT).show();
            } else {
                editItem(selectedIndex);
            }
        });

        // handles loading the inventory table with items in the databse
        load();
    }

    // loading helper function
    private void load() {
        // resets the screen data, and clears potentially stale selection
        ids.clear();
        selectedIndex = -1;

        // i would like to keep the placeholder rows for when editing in the activity view, however
        // they do need removed on start up. this also helps with updating the table after edits
        table.removeAllViews();

        // gets all item in the database as a cursor
        Cursor c = db.getAllItems();
        int index = 0;

        // loops through each item
        while (c.moveToNext()) {
            // gets the values from each item
            int itemId = c.getInt(0);
            String name = c.getString(1);
            String qty = c.getString(2);
            String loc = c.getString(3);
            ids.add(itemId);

            // creates new table and row for the item, set up like the placeholders
            TableRow row = new TableRow(this);
            TextView nameView = new TextView(this);
            TextView qtyView = new TextView(this);
            TextView locView = new TextView(this);
            Button deleteBtn = new Button(this);

            nameView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            qtyView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            locView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            deleteBtn.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

            // sets the item's values
            nameView.setText(name);
            qtyView.setText(qty);
            locView.setText(loc);
            deleteBtn.setText("Delete");

            // adds a listener to the row, to make the item selectable
            int currentIndex = index;
            nameView.setOnClickListener(v -> selectRow(row, currentIndex));
            deleteBtn.setOnClickListener(v -> delete(currentIndex));

            // finally, adds the row to the UI table
            row.addView(nameView);
            row.addView(qtyView);
            row.addView(locView);
            row.addView(deleteBtn);

            table.addView(row);

            index++;
        }

        c.close();
    }

    // row selection function
    private void selectRow(TableRow selectedRow, int index) {
        selectedIndex = index;

        // removes previous slection, and sets color to transparent
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            row.setBackgroundColor(Color.TRANSPARENT);
        }

        // sets selected row to a light gray
        selectedRow.setBackgroundColor(Color.parseColor("#D3D3D3"));
    }

    // add item function
    private void addItem() {
        // popup window used for adding item details

        // starts by creating temporary input fields
        EditText n = new EditText(this);
        n.setHint("Item name");

        EditText q = new EditText(this);
        q.setHint("Quantity");
        q.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText l = new EditText(this);
        l.setHint("Location");

        // stores these fields in a linear layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(n);
        layout.addView(q);
        layout.addView(l);

        // dialog window
        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(layout)

                // saves data when done, and trims off excess white space
                .setPositiveButton("Save", (d, w) -> {
                    String name = n.getText().toString().trim();
                    String qty = q.getText().toString().trim();
                    String loc = l.getText().toString().trim();

                    // some light null input validation for all 3 fields
                    if (name.isEmpty() || qty.isEmpty() || loc.isEmpty()) {
                        Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // adds the new item to the database, then reloads the item list
                    db.insertItem(name, Integer.parseInt(qty), loc);
                    load();
                })

                // cancels everything if selected
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editItem(int index) {

        // check if a valid item is selected
        if (index >= ids.size()) {
            return;
        }

        // checks the database and gets item details
        Cursor c = db.getAllItems();
        int i = 0;

        String currentName = "";
        String currentQty = "";
        String currentLoc = "";

        // items in view are in same order as in the database, so this loops through until current selections
        while (c.moveToNext()) {
            if (i == index) {
                currentName = c.getString(1);
                currentQty = c.getString(2);
                currentLoc = c.getString(3);
                break;
            }
            i++;
        }

        // cleanup
        c.close();

        // sets up the dialog boxe, fields are prefilled with current item's details
        EditText n = new EditText(this);
        n.setHint("Item name");
        n.setText(currentName);

        EditText q = new EditText(this);
        q.setHint("Quantity");
        q.setInputType(InputType.TYPE_CLASS_NUMBER);
        q.setText(currentQty);

        EditText l = new EditText(this);
        l.setHint("Location");
        l.setText(currentLoc);

        //dialog layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(n);
        layout.addView(q);
        layout.addView(l);

        // dialog window
        new AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(layout)

                // update button updates the item in the database with the new info
                .setPositiveButton("Update", (d, w) -> {
                    String name = n.getText().toString().trim();
                    String qty = q.getText().toString().trim();
                    String loc = l.getText().toString().trim();

                    // more null input validation
                    if (name.isEmpty() || qty.isEmpty() || loc.isEmpty()) {
                        Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //updates the DB, and reloads the view
                    db.updateItem(ids.get(index), name, Integer.parseInt(qty), loc);
                    load();
                })

                // cancels out of edit
                .setNegativeButton("Cancel", null)
                .show();
    }

    // deletes the item
    private void delete(int index) {
        if (index < ids.size()) {
            db.deleteItem(ids.get(index));
            load();
        }
    }
}