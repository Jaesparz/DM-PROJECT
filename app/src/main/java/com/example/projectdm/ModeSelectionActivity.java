package com.example.projectdm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        Button btnViajero = findViewById(R.id.btnViajero);
        Button btnTransportista = findViewById(R.id.btnTransportista);
        Button btnMinijuego = findViewById(R.id.btnMinijuego);
        Button btnInfo = findViewById(R.id.btnInfo);


        //Boton viajero

        btnViajero.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, MainActivity.class);
            startActivity(intent);
        });


        //Boton transportista

//        btnTransportista.setOnClickListener(v -> {
//            Intent intent = new Intent(ModeSelectionActivity.this, TransportistaActivity.class);
//            startActivity(intent);
//        });


        //Minijuego

        btnMinijuego.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, Minigame2DActivity.class);
            startActivity(intent);
        });




        // Botón para ver información

        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ModeSelectionActivity.this, InfoAlgorithm.class);
            startActivity(intent);
        });


        // Botón para regresar al menú principal
        Button btnBackToMain = findViewById(R.id.btnBackToMain);
        btnBackToMain.setOnClickListener(v -> {
            // Regresar al menú principal
            Intent intent = new Intent(ModeSelectionActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish(); // Opcional: finaliza esta actividad para no dejarla en la pila
        });
    }
}
