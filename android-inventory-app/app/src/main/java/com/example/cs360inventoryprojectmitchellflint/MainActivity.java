package com.example.cs360inventoryprojectmitchellflint;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // class variables
    DatabaseHelper db;
    EditText user, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize database
        db = new DatabaseHelper(this);

        // link to UI
        user = findViewById(R.id.usernameText);
        pass = findViewById(R.id.passwordText);
        Button login = findViewById(R.id.buttonLogin);
        Button create = findViewById(R.id.buttonCreateAccount);

        // create user button
        create.setOnClickListener(v -> {

            // null imput validation
            if (user.getText().toString().isEmpty() || pass.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // show prompt if username already exists, else creat account
            if (db.userExists(user.getText().toString())) {
                Toast.makeText(this, "User exists", Toast.LENGTH_SHORT).show();
            } else {
                db.insertUser(user.getText().toString(), pass.getText().toString());
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
            }
        });

        // login button
        login.setOnClickListener(v -> {
            // check user, if exists = move to next activity.
            if (db.checkUser(user.getText().toString(), pass.getText().toString())) {
                startActivity(new Intent(this, SmsActivity.class));
            } else {
                // input validation
                Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show();
            }
        });
    }
}