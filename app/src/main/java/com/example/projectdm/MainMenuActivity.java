package com.example.projectdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        Button btnComenzar = findViewById(R.id.btnMenu);
        btnComenzar.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ModeSelectionActivity.class);
            startActivity(intent);
        });
    }
}
