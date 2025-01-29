package com.example.projectdm;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransportistaActivity extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private List<Demanda> demandas; // Lista de demandas dinámicas
    private GeoPoint nodoInicial; // Nodo inicial seleccionado
    private List<Polyline> rutasCalculadas = new ArrayList<>(); // Rutas trazadas en el mapa


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transportista);

        // Configurar OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Crear MapView
        mapView = new MapView(this);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(new GeoPoint(-2.146, -79.966));

        // Agregar MapView al contenedor
        FrameLayout mapContainer = findViewById(R.id.map_container);
        mapContainer.addView(mapView);

        // Agregar superposición de ubicación
        locationOverlay = new MyLocationNewOverlay(mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Generar demandas dinámicas
        generarDemandas();

        FloatingActionButton btnSetStartNode = findViewById(R.id.btnSetStartNode);
        btnSetStartNode.setOnClickListener(v -> {
            Toast.makeText(this, "Toque el mapa para establecer el nodo inicial.", Toast.LENGTH_SHORT).show();

            // Activar el listener del mapa para seleccionar el nodo inicial
            mapView.setOnTouchListener((view, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Obtener las coordenadas del toque
                    Projection projection = mapView.getProjection();
                    GeoPoint punto = (GeoPoint) projection.fromPixels((int) event.getX(), (int) event.getY());

                    // Establecer el nodo inicial
                    nodoInicial = punto;

                    // Agregar marcador del camión en el nodo inicial
                    agregarMarcadorCamion(nodoInicial);

                    // Mostrar mensaje al usuario
                    Toast.makeText(this, "Nodo inicial establecido en: " +
                            punto.getLatitude() + ", " + punto.getLongitude(), Toast.LENGTH_SHORT).show();

                    // Desactivar el listener para evitar que siga marcando nodos
                    mapView.setOnTouchListener(null);
                }
                return true;
            });
        });

        FloatingActionButton btnAssistant = findViewById(R.id.btnAssistant);
        btnAssistant.setOnClickListener(v -> mostrarSugerenciaDeAsistente());


        // Botón para seleccionar una demanda y calcular la ruta
        FloatingActionButton btnSelectDemand = findViewById(R.id.btnSelectDemand);
        btnSelectDemand.setOnClickListener(v -> {
            if (nodoInicial == null) {
                Toast.makeText(this, "Primero selecciona tu ubicación actual.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Construir la lista de nombres de demandas con la distancia desde el camión
            String[] nombresDemandas = new String[demandas.size()];
            for (int i = 0; i < demandas.size(); i++) {
                Demanda demanda = demandas.get(i);
                double distancia = calcularDistancia(nodoInicial, demanda.getUbicacion());
                nombresDemandas[i] = demanda.getDescripcion() + " - A " + String.format("%.1f", distancia) + " km";
            }

            // Mostrar las demandas en un diálogo
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Demandas disponibles");

            builder.setItems(nombresDemandas, (dialog, which) -> {
                Demanda demandaSeleccionada = demandas.get(which);

                // Calcular la ruta más corta al destino seleccionado
                calcularRutaCorta(demandaSeleccionada);
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

    }

    private void generarDemandas() {
        demandas = Arrays.asList(
                new Demanda("Entrega de Electrodoméstico", new GeoPoint(-2.155259, -79.892787), 200),
                new Demanda("Paquete Urgente", new GeoPoint(-2.229620, -79.898629), 300),
                new Demanda("Encomienda Pequeña", new GeoPoint(-2.162932, -79.931266), 150),
                new Demanda("Material Escolar", new GeoPoint(-2.127823, -79.907100), 100),
                new Demanda("Ropa", new GeoPoint(-2.141862, -79.865193), 180),
                new Demanda("Medicinas", new GeoPoint(-2.165419, -79.928423), 250),
                new Demanda("Repuestos de Autos", new GeoPoint(-2.118293, -79.907512), 350),
                new Demanda("Electrónica", new GeoPoint(-2.170823, -79.922312), 280),
                new Demanda("Frutas y Verduras", new GeoPoint(-2.134823, -79.916712), 120),
                new Demanda("Muebles de Oficina", new GeoPoint(-2.145823, -79.906312), 400)
        );

        for (Demanda demanda : demandas) {
            Marker marker = new Marker(mapView);
            marker.setPosition(demanda.getUbicacion());
            marker.setTitle(demanda.getDescripcion());
            marker.setIcon(resizeIcon(getResources().getDrawable(R.drawable.ic_demand, null), 60, 60));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }


    private void mostrarSugerenciaDeAsistente() {
        if (nodoInicial == null) {
            Toast.makeText(this, "Selecciona tu ubicación inicial primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Encuentra la mejor demanda basada en la relación beneficio/distancia
        Demanda mejorDemanda = null;
        double mejorRelacion = Double.MIN_VALUE;

        for (Demanda demanda : demandas) {
            double distancia = calcularDistancia(nodoInicial, demanda.getUbicacion());
            double relacion = demanda.getBeneficio() / distancia; // Relación beneficio/distancia

            if (relacion > mejorRelacion) {
                mejorRelacion = relacion;
                mejorDemanda = demanda;
            }
        }

        if (mejorDemanda != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Asistente: Mejor Opción");
            Demanda finalMejorDemanda = mejorDemanda;
            builder.setMessage("Demanda sugerida: " + mejorDemanda.getDescripcion() +
                            "\nBeneficio: $" + mejorDemanda.getBeneficio() +
                            "\nDistancia: " + String.format("%.2f", calcularDistancia(nodoInicial, mejorDemanda.getUbicacion())) + " km")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        calcularRutaCorta(finalMejorDemanda); // Calcular y mostrar la ruta al aceptar
                    })
                    .setNegativeButton("Rechazar", null)
                    .show();
        } else {
            Toast.makeText(this, "No se encontraron demandas disponibles.", Toast.LENGTH_SHORT).show();
        }
    }

    private double calcularDistancia(GeoPoint punto1, GeoPoint punto2) {
        double lat1 = Math.toRadians(punto1.getLatitude());
        double lon1 = Math.toRadians(punto1.getLongitude());
        double lat2 = Math.toRadians(punto2.getLatitude());
        double lon2 = Math.toRadians(punto2.getLongitude());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);

        return 2 * 6371 * Math.asin(Math.sqrt(a)); // Radio de la Tierra: 6371 km
    }


    private void trazarRutaAnimada(List<GeoPoint> puntos, int color) {
        Polyline polyline = new Polyline();
        polyline.getOutlinePaint().setColor(color);
        polyline.getOutlinePaint().setStrokeWidth(8f);

        List<GeoPoint> puntosAnimados = new ArrayList<>();
        Handler handler = new Handler(Looper.getMainLooper());
        int delay = 50;

        for (int i = 0; i < puntos.size(); i++) {
            int finalI = i;
            handler.postDelayed(() -> {
                puntosAnimados.add(puntos.get(finalI));
                polyline.setPoints(puntosAnimados);
                mapView.getOverlays().add(polyline);
                mapView.invalidate();
            }, delay * i);
        }
    }






    private void calcularRutaCorta(Demanda demanda) {
        if (nodoInicial == null) {
            Toast.makeText(this, "Selecciona un nodo inicial primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear la URL para la API de OSRM
        String url = "https://router.project-osrm.org/route/v1/driving/" +
                nodoInicial.getLongitude() + "," + nodoInicial.getLatitude() +
                ";" + demanda.getUbicacion().getLongitude() + "," + demanda.getUbicacion().getLatitude() +
                "?overview=full&geometries=geojson";

        // Realizar la solicitud y mostrar la ruta
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL obj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    mostrarRuta(result); // Mostrar la ruta
                }
            }
        }.execute();
    }

    private void mostrarRuta(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                String geometry = route.getString("geometry");
                List<GeoPoint> puntosRuta = GeoJsonParser.parseGeoJson(geometry);

                // Animar la ruta en lugar de mostrarla directamente
                trazarRutaAnimada(puntosRuta, Color.BLUE);

                Toast.makeText(this, "Ruta trazada en el mapa.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al mostrar la ruta.", Toast.LENGTH_SHORT).show();
        }
    }


    private Drawable resizeIcon(Drawable icon, int width, int height) {
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();

        // Asegurar que la imagen se ajuste correctamente en cualquier nivel de zoom
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return new BitmapDrawable(getResources(), resizedBitmap);
    }


    private void agregarMarcadorCamion(GeoPoint ubicacion) {
        // Elimina marcadores anteriores si ya se estableció uno antes
        for (Overlay overlay : new ArrayList<>(mapView.getOverlays())) {
            if (overlay instanceof Marker) {
                Marker marcador = (Marker) overlay;
                if ("Camion".equals(marcador.getTitle())) {
                    mapView.getOverlays().remove(marcador);
                }
            }
        }

        // Crear un nuevo marcador para el camión
        Marker marker = new Marker(mapView);
        marker.setPosition(ubicacion);
        marker.setTitle("Camion");
        marker.setIcon(resizeIcon(getResources().getDrawable(R.drawable.ic_truck, null), 80, 80));

        // **Aquí está la corrección clave para evitar desplazamiento**
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        // Agregar el marcador al mapa
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }



    // Clase interna para representar las demandas
    private static class Demanda {
        private final String descripcion;
        private final GeoPoint ubicacion;
        private final int beneficio;

        public Demanda(String descripcion, GeoPoint ubicacion, int beneficio) {
            this.descripcion = descripcion;
            this.ubicacion = ubicacion;
            this.beneficio = beneficio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public GeoPoint getUbicacion() {
            return ubicacion;
        }

        public int getBeneficio() {
            return beneficio;
        }


    }
}

