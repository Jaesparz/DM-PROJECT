package com.example.projectdm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Minigame2DActivity extends AppCompatActivity {

    private SurfaceView gameSurface;
    private GameEngine gameEngine;
    private int mapWidth;
    private int mapHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame_2d);

        gameSurface = findViewById(R.id.gameSurface);
        SurfaceHolder holder = gameSurface.getHolder();

        // Inicializar el mapa después de conocer el tamaño del SurfaceView
        gameSurface.post(() -> {
            mapWidth = gameSurface.getWidth();
            mapHeight = gameSurface.getHeight();

            // Inicializar el sprite del mensajero
            Bitmap mensajeroBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nino);

            // Inicializar el motor del juego
            gameEngine = new GameEngine(holder, mensajeroBitmap, mapWidth, mapHeight, this);
            gameEngine.start(); // Iniciar el motor del juego
        });

        setupControls();


        gameSurface.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                Log.d("TouchCoordinates", "X: " + x + ", Y: " + y);
            }
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameEngine != null) {
            gameEngine.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameEngine != null) {
            gameEngine.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
    }

    private void setupControls() {
        findViewById(R.id.btnUp).setOnClickListener(v -> moveMensajero(0, -10));
        findViewById(R.id.btnDown).setOnClickListener(v -> moveMensajero(0, 10));
        findViewById(R.id.btnLeft).setOnClickListener(v -> moveMensajero(-10, 0));
        findViewById(R.id.btnRight).setOnClickListener(v -> moveMensajero(10, 0));
    }

    private void moveMensajero(int dx, int dy) {
        int newX = gameEngine.getMensajeroX() + dx;
        int newY = gameEngine.getMensajeroY() + dy;

        // Verificar que el mensajero no salga del mapa
        if (newX >= 0 && newX + 50 <= mapWidth && newY >= 0 && newY + 50 <= mapHeight) {
            gameEngine.updateMensajeroPosition(newX, newY);
        }
    }
}

