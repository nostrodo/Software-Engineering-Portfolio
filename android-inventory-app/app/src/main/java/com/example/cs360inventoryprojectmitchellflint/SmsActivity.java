package com.example.cs360inventoryprojectmitchellflint;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SmsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // link to UI
        Button enable = findViewById(R.id.EnableSMSButton);
        Button reject = findViewById(R.id.RejectSMSButton);

        // prompts user to enable SMS, then continues to next screen
        enable.setOnClickListener(v -> {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 1);

            startActivity(new Intent(this, InventoryActivity.class));
        });

        // just continues to next screen if rejected
        reject.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
    }
}