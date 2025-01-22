package com.example.projectdm;

import android.content.Context;
import android.graphics.Bitmap;
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



    public GameEngine(SurfaceHolder surfaceHolder, Bitmap mensajeroBitmap, int mapWidth, int mapHeight,Context context) {
        this.surfaceHolder = surfaceHolder;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.context = context;

        // Redimensionar el bitmap del mensajero
        this.mensajeroBitmap = Bitmap.createScaledBitmap(mensajeroBitmap, 100, 100, false);

        initializeCity();
        calculateShortestPath();
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
                        surfaceHolder.unlockCanvasAndPost(canvas); // Asegurarse de liberar el canvas
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



    private void initializeCity() {
        city = new CityGraph();



        // Aumenta la escala de las posiciones de los nodos
        int scale = 3;
        city.addNode(100 * scale, 100 * scale, "Casa 1");
        city.addNode(300 * scale, 100 * scale, "Casa 2");
        city.addNode(100 * scale, 300 * scale, "Casa 3");
        city.addNode(300 * scale, 300 * scale, "Casa 4");

        // Agregar aristas
        city.addEdge(city.nodes.get(0), city.nodes.get(1), 10);
        city.addEdge(city.nodes.get(0), city.nodes.get(2), 20);
        city.addEdge(city.nodes.get(1), city.nodes.get(3), 15);
        city.addEdge(city.nodes.get(2), city.nodes.get(3), 25);

        // Posicionar al personaje en el primer nodo
        initialNode = city.nodes.get(0);
        mensajeroX = initialNode.x;
        mensajeroY = initialNode.y;

         endNode = city.nodes.get(city.nodes.size() - 1);

    }




    private void render(Canvas canvas) {
        // Limpiar el canvas
        canvas.drawColor(Color.BLACK);

        Paint paint = new Paint();
        paint.setStrokeWidth(5);

        // Dibujar aristas (calles)
        for (CityGraph.Edge edge : city.edges) {
            paint.setColor(edge.color); // Color de la arista (blanco, verde, rojo, etc.)
            paint.setStrokeWidth(3);    // Ancho de las calles
            canvas.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y, paint);
        }

        // Dibujar nodos (casas o puntos de interés)
        for (CityGraph.Node node : city.nodes) {
            if (node == endNode) {
                paint.setColor(Color.GREEN);  // Nodo final en color verde
            } else {
                paint.setColor(Color.WHITE);  // Color de los nodos normales
            }
            canvas.drawCircle(node.x, node.y, 20, paint);  // Dibujar nodo como un círculo


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
        if (edge != null) {
            playerPath.add(edge);
        }
    }
    private boolean isRenderingPath = false;
    private void evaluatePlayerPath() {
        // Resetea los colores de las aristas
        for (CityGraph.Edge edge : city.edges) {
            edge.color = Color.WHITE; // Predeterminado
        }

        // Marca las aristas del camino del jugador como incorrectas
        for (CityGraph.Edge edge : playerPath) {
            edge.color = Color.RED; // Color de error
        }

        // Marca las aristas del camino más corto como correctas
        for (CityGraph.Edge edge : shortestPath) {
            edge.color = Color.GREEN; // Color correcto

            // Si el jugador recorrió la arista correcta, ajusta el color
            if (playerPath.contains(edge)) {
                edge.color = Color.GREEN;
            }
        }

        // Mostrar mensaje al jugador
        boolean isCorrectPath = playerPath.equals(shortestPath);
        showEndGameMessage(isCorrectPath);

        // Redibujar la pantalla
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                render(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                        .setPositiveButton("Aceptar", null)
                        .show();
            } else {
                builder.setTitle("Ruta incorrecta")
                        .setMessage("Has llegado al destino, pero no tomaste la ruta más corta.")
                        .setPositiveButton("Aceptar", null)
                        .show();
            }
        });
    }









    public int getMensajeroX() {
        return mensajeroX;
    }

    public int getMensajeroY() {
        return mensajeroY;
    }

}


