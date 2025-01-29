package com.example.projectdm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;


public class GameEngine extends Thread {

    private SurfaceHolder surfaceHolder;
    private boolean running = true;
    private boolean isPaused = false;
    private Bitmap mensajeroBitmap;
    private int mensajeroX; // Posición inicial en X
    private int mensajeroY; // Posición inicial en Y
    private int mapWidth;
    private int mapHeight;
    private CityGraph city;
    private CityGraph.Node initialNode;
    private CityGraph.Node endNode;
    private Context context;
    private List<CityGraph.Edge> shortestPath; // Ruta más corta
    private List<CityGraph.Edge> playerPath = new ArrayList<>(); // Camino del jugador



    public GameEngine(SurfaceHolder surfaceHolder, Bitmap mensajeroBitmap, int mapWidth, int mapHeight, Context context, int level) {
        this.surfaceHolder = surfaceHolder;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.context = context;

        // Redimensionar el bitmap del mensajero
        this.mensajeroBitmap = Bitmap.createScaledBitmap(mensajeroBitmap, 100, 100, false);

        initializeCity(level); // Configurar la ciudad según el nivel
        calculateShortestPath(); // Calcular la ruta más corta
    }


    @Override
    public void run() {
        while (running) {
            if (!isPaused) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        render(canvas); // Renderiza el juego

                        // Verificar si el mensajero está cerca del nodo final
                        if (isNearNode(mensajeroX, mensajeroY, endNode.x, endNode.y)) {
                            evaluatePlayerPath(); // Comparar las rutas y actualizar colores
                            running = false;      // Finaliza el juego
                            break;                // Salir del bucle
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Manejar excepciones
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas); // Liberar el canvas
                    }
                }
            }
        }
    }


    private boolean isNearNode(int x, int y, int nodeX, int nodeY) {
        int tolerance = 20; // Margen de error en píxeles
        return Math.abs(x - nodeX) <= tolerance && Math.abs(y - nodeY) <= tolerance;
    }




    public void pause() {
        isPaused = true;
    }

    public void resumeGame() {
        isPaused = false;
    }

    public void stopGame() {
        running = false;
    }



    private void initializeCity(int level) {
        city = new CityGraph();

        if (level == 1) {
            // Configuración del Nivel 1
            city.addNode(300, 90, "Casa 1");
            city.addNode(900, 100, "Casa 2");
            city.addNode(1270, 400, "Casa 3");
            city.addNode(1560, 170, "Casa 4");
            city.addNode(1600, 450, "Casa 5");
            city.addNode(1280, 700, "Casa 6");
            city.addNode(810, 820, "Casa 7");
            city.addNode(211, 710, "Casa 8");
            city.addNode(100, 350, "Casa 9");

            city.addEdge(city.nodes.get(0), city.nodes.get(1), 50);
            city.addEdge(city.nodes.get(1), city.nodes.get(2), 30);
            city.addEdge(city.nodes.get(2), city.nodes.get(3), 20);
            city.addEdge(city.nodes.get(3), city.nodes.get(4), 15);
            city.addEdge(city.nodes.get(2), city.nodes.get(5), 25);
            city.addEdge(city.nodes.get(5), city.nodes.get(6), 40);
            city.addEdge(city.nodes.get(6), city.nodes.get(7), 35);
            city.addEdge(city.nodes.get(7), city.nodes.get(8), 45);
            city.addEdge(city.nodes.get(8), city.nodes.get(0), 60);
            city.addEdge(city.nodes.get(1), city.nodes.get(6), 50);
            city.addEdge(city.nodes.get(4), city.nodes.get(5), 20);

            initialNode = city.nodes.get(0); // Nodo inicial
            endNode = city.nodes.get(4);     // Nodo final

        } else if (level == 2) {
            // Configuración del Nivel 2
            city.addNode(200, 100, "Nodo A");
            city.addNode(500, 200, "Nodo B");
            city.addNode(700, 500, "Nodo C");
            city.addNode(900, 300, "Nodo D");
            city.addNode(1100, 600, "Nodo E");

            city.addEdge(city.nodes.get(0), city.nodes.get(1), 40);
            city.addEdge(city.nodes.get(1), city.nodes.get(2), 35);
            city.addEdge(city.nodes.get(2), city.nodes.get(3), 25);
            city.addEdge(city.nodes.get(3), city.nodes.get(4), 30);
            city.addEdge(city.nodes.get(0), city.nodes.get(3), 50);

            initialNode = city.nodes.get(0); // Nodo inicial
            endNode = city.nodes.get(4);     // Nodo final
        }

        // Posicionar al personaje en el nodo inicial
        mensajeroX = initialNode.x;
        mensajeroY = initialNode.y;
    }





    private void render(Canvas canvas) {
        // Limpiar el canvas
        canvas.drawColor(Color.BLACK);

        Paint paint = new Paint();
        paint.setStrokeWidth(5);

        // Dibujar las aristas con sus colores actuales
        for (CityGraph.Edge edge : city.edges) {
            // Dibujar la línea de la arista
            paint.setColor(edge.color); // Usa el color actualizado (gris, verde o rojo)
            paint.setStrokeWidth(5);    // Ancho de las calles
            canvas.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y, paint);

            // Dibujar el peso de la arista
            paint.setColor(Color.WHITE); // Color del texto del peso
            paint.setTextSize(24);       // Tamaño del texto
            float midX = (edge.from.x + edge.to.x) / 2f; // Punto medio en X
            float midY = (edge.from.y + edge.to.y) / 2f; // Punto medio en Y
            canvas.drawText(String.valueOf(edge.cost), midX, midY, paint); // Mostrar el peso (costo)
        }

        // Dibujar nodos
        for (CityGraph.Node node : city.nodes) {
            if (node == endNode) {
                paint.setColor(Color.WHITE); // Nodo final se dibuja debajo del tesoro
                canvas.drawCircle(node.x, node.y, 20, paint); // Dibujar nodos como círculos
            } else {
                paint.setColor(Color.WHITE); // Nodos normales en blanco
                canvas.drawCircle(node.x, node.y, 20, paint); // Dibujar nodos como círculos
            }
        }

        // Dibujar el icono del tesoro
        if (endNode != null) {
            Bitmap treasureBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_treasure);
            Bitmap resizedTreasureBitmap = Bitmap.createScaledBitmap(treasureBitmap, 50, 50, false); // Ajusta el tamaño
            canvas.drawBitmap(resizedTreasureBitmap, endNode.x - resizedTreasureBitmap.getWidth() / 2, endNode.y - resizedTreasureBitmap.getHeight() / 2, null);
        }

        // Dibujar el icono del pirata
        if (endNode != null) {
            Bitmap pirateBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_pirate);
            Bitmap resizedPirateBitmap = Bitmap.createScaledBitmap(pirateBitmap, 90, 90, false); // Ajusta el tamaño
            // Posicionar el pirata más lejos del tesoro
            canvas.drawBitmap(resizedPirateBitmap, endNode.x + 150, endNode.y -45 , null); // Ajusta estas coordenadas según tu diseño
        }

        // Dibujar el mensajero
        if (mensajeroBitmap != null) {
            canvas.drawBitmap(mensajeroBitmap, mensajeroX - mensajeroBitmap.getWidth() / 2,
                    mensajeroY - mensajeroBitmap.getHeight() / 2, null);
        }
    }






    public void updateMensajeroPosition(int x, int y) {
        for (CityGraph.Edge edge : city.edges) {
            if (isNearEdge(x, y, edge.from.x, edge.from.y, edge.to.x, edge.to.y)) {
                // Si el nuevo punto está en una arista válida, actualizamos la posición
                this.mensajeroX = x;
                this.mensajeroY = y;

                // Monitorear el camino del jugador
                trackPlayerPath(edge.from, edge.to);
                return;
            }
        }

        // Si no está en ninguna arista, no hacemos nada (opcional: notificar)
        Log.d("GameEngine", "Movimiento restringido: fuera de las aristas.");
    }




    // Verificar si el jugador está cerca de una arista (mejorada)
    private boolean isNearEdge(int x, int y, int x1, int y1, int x2, int y2) {
        double distance = Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1) /
                Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));

        if (distance > 20) { // Si está fuera de la distancia aceptable
            return false;
        }

        // Verificar si el punto está dentro de los límites del segmento de línea
        double dotProduct = (x - x1) * (x2 - x1) + (y - y1) * (y2 - y1);
        if (dotProduct < 0) {
            return false; // Fuera del segmento
        }

        double squaredLength = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        if (dotProduct > squaredLength) {
            return false; // Fuera del segmento
        }

        return true; // Dentro del rango permitido
    }



    private void calculateShortestPath() {
        shortestPath = city.calculateShortestPath(initialNode, endNode);
    }

    // Monitorear el camino del jugador
    private void trackPlayerPath(CityGraph.Node fromNode, CityGraph.Node toNode) {
        CityGraph.Edge edge = city.findEdgeBetween(fromNode, toNode);
        if (edge != null && !playerPath.contains(edge)) {
            playerPath.add(edge);
        }
    }

    private void evaluatePlayerPath() {
        // Resetear los colores de todas las aristas
        for (CityGraph.Edge edge : city.edges) {
            edge.color = Color.GRAY; // Color predeterminado (calles no recorridas)
        }

        // Marcar la ruta más corta en verde
        for (CityGraph.Edge edge : shortestPath) {
            edge.color = Color.GREEN; // Ruta más corta
        }

        // Si el jugador tomó una ruta incorrecta, marcarla en rojo
        boolean isCorrectPath = true; // Asume que es correcto al inicio
        for (CityGraph.Edge edge : playerPath) {
            if (!shortestPath.contains(edge)) {
                edge.color = Color.RED; // Marcar como incorrecto
                isCorrectPath = false; // El camino no es el más corto
            }
        }

        // Mostrar mensaje al jugador después de evaluar el camino
        showEndGameMessage(isCorrectPath);

        // Redibujar el mapa con las rutas actualizadas
        redrawCanvas();
    }









    private void redrawCanvas() {
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                render(canvas); // Usa el método render para actualizar el dibujo
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }







    private void showEndGameMessage(boolean isCorrectPath) {
        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if (isCorrectPath) {
                builder.setTitle("¡Felicidades!")
                        .setMessage("Has tomado la ruta más corta.")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            // Mostrar la ruta más corta y avanzar al siguiente nivel
                            showShortestPathMessage();
                            ((Minigame2DActivity) context).startNextLevel();
                        })
                        .setNegativeButton("Salir", (dialog, which) -> {
                            ((Minigame2DActivity) context).finish();
                        })
                        .show();
            } else {
                builder.setTitle("Ruta incorrecta")
                        .setMessage("Has llegado al destino, pero no tomaste la ruta más corta.\nLa ruta correcta se marcará en verde.")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            // Mostrar la ruta más corta con un retraso de 10 segundos
                            showShortestPathMessage();
                            new Handler().postDelayed(() -> {
                                // Preguntar si quiere reintentar después de 10 segundos
                                showRetryDialog();
                            }, 10000);
                        })
                        .setNegativeButton("Salir", (dialog, which) -> {
                            ((Minigame2DActivity) context).finish();
                        })
                        .show();
            }
        });
    }

    /**
     * Mostrar cuadro de diálogo para reintentar el nivel
     */
    private void showRetryDialog() {
        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("¿Reintentar nivel?")
                    .setMessage("¿Quieres intentar nuevamente este nivel?")
                    .setPositiveButton("Reintentar", (dialog, which) -> {
                        ((Minigame2DActivity) context).restartCurrentLevel();
                    })
                    .setNegativeButton("Salir", (dialog, which) -> {
                        ((Minigame2DActivity) context).finish();
                    })
                    .show();
        });
    }

    /**
     * Mostrar mensaje de la ruta más corta
     */
    private void showShortestPathMessage() {
        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Ruta más corta")
                    .setMessage("La ruta más corta se ha marcado en verde en el mapa.")
                    .setPositiveButton("Aceptar", null)
                    .show();
        });

        // Redibujar el canvas para mostrar la ruta
        redrawCanvas();
    }













    public int getMensajeroX() {
        return mensajeroX;
    }

    public int getMensajeroY() {
        return mensajeroY;
    }

}


