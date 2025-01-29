package com.example.projectdm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Minigame2DActivity extends AppCompatActivity {

    private SurfaceView gameSurface;
    private GameEngine gameEngine;
    private int mapWidth;
    private int mapHeight;
    private int currentLevel = 1; // Comienza en el nivel 1

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

            // Inicializar el primer nivel del juego
            startLevel(currentLevel);
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
        findViewById(R.id.btnUp).setOnClickListener(v -> moveMensajero(0, -25));
        findViewById(R.id.btnDown).setOnClickListener(v -> moveMensajero(0, 25));
        findViewById(R.id.btnLeft).setOnClickListener(v -> moveMensajero(-25, 0));
        findViewById(R.id.btnRight).setOnClickListener(v -> moveMensajero(25, 0));
    }

    private void moveMensajero(int dx, int dy) {
        int newX = gameEngine.getMensajeroX() + dx;
        int newY = gameEngine.getMensajeroY() + dy;

        // Verificar que el mensajero no salga del mapa
        if (newX >= 0 && newX + 50 <= mapWidth && newY >= 0 && newY + 50 <= mapHeight) {
            gameEngine.updateMensajeroPosition(newX, newY);
        }
    }

    /**
     * Inicia un nivel específico
     */
    private void startLevel(int level) {
        SurfaceHolder holder = gameSurface.getHolder();
        Bitmap mensajeroBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nino);

        // Si ya hay un motor de juego en ejecución, deténlo
        if (gameEngine != null) {
            gameEngine.stopGame();
        }

        // Mostrar mensaje de inicio del nivel
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Inicio del Nivel " + level)
                    .setMessage("¡Prepárate para completar el Nivel " + level + "!")
                    .setPositiveButton("Comenzar", (dialog, which) -> {
                        // Inicializar el nuevo nivel
                        gameEngine = new GameEngine(holder, mensajeroBitmap, mapWidth, mapHeight, this, level);
                        gameEngine.start(); // Iniciar el motor del juego
                    })
                    .show();
        });
    }

    /**
     * Método llamado desde GameEngine para avanzar al siguiente nivel
     */
    public void startNextLevel() {
        currentLevel++;
        if (currentLevel > 2) {
            // Si no hay más niveles, muestra un mensaje final
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("¡Felicidades!")
                        .setMessage("¡Has completado todos los niveles!")
                        .setPositiveButton("Salir", (dialog, which) -> finish())
                        .show();
            });
        } else {
            // Iniciar el siguiente nivel
            startLevel(currentLevel);
        }
    }

    /**
     * Método llamado desde GameEngine para repetir el nivel si se toma un camino incorrecto
     */
    public void restartCurrentLevel() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Ruta incorrecta, EL PIRATA ROBARÁ EL BOTIN")
                    .setMessage("No tomaste la ruta más corta. ¿Quieres intentar nuevamente?")
                    .setPositiveButton("Reintentar", (dialog, which) -> startLevel(currentLevel))
                    .setNegativeButton("Salir", (dialog, which) -> finish())
                    .show();
        });
    }
}


