package com.example.projectdm;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.OverlayItem;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private LugarTuristico puntoPartida;
    private LugarTuristico puntoDestino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el diseño de la actividad
        setContentView(R.layout.activity_main);

        // Configurar OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Crear dinámicamente el MapView
        mapView = new MapView(this);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(-2.146, -79.966));

        // Agregar el MapView al contenedor
        FrameLayout mapContainer = findViewById(R.id.map_container);
        mapContainer.addView(mapView);

        // Agregar superposición de ubicación
        locationOverlay = new MyLocationNewOverlay(mapView);
        locationOverlay.enableMyLocation();
        mapView.getOverlays().add(locationOverlay);

        // Agregar una brújula
        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Agregar barra de escala
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);

        // Lugares Turisticos

        agregarMarcadores();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destruir el MapView para liberar recursos
        if (mapView != null) {
            mapView.onDetach();
        }
    }
    private void agregarMarcadores() {
        List<LugarTuristico> lugares = Arrays.asList(
                new LugarTuristico("Malecón 2000", new GeoPoint(-2.1891, -79.8806)),
                new LugarTuristico("Parque Seminario", new GeoPoint(-2.1897, -79.8833)),
                new LugarTuristico("Cerro Santa Ana", new GeoPoint(-2.1902, -79.8774)),
                new LugarTuristico("Barrio Las Peñas", new GeoPoint(-2.1878, -79.8756))
        );

        for (LugarTuristico lugar : lugares) {
            Marker marker = new Marker(mapView);
            marker.setPosition(lugar.getUbicacion());
            marker.setTitle(lugar.getNombre());
            marker.setOnMarkerClickListener((m, map) -> {
                mostrarOpciones(lugar);  // Llamar al método para seleccionar partida o destino
                return true;  // Evitar el comportamiento predeterminado
            });
            mapView.getOverlays().add(marker);  // Agregar marcador al mapa
        }

        mapView.invalidate();  // Refrescar el mapa
    }

    private void mostrarOpciones(LugarTuristico lugar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar punto")
                .setMessage("¿Este es el punto de partida o de destino?")
                .setPositiveButton("Partida", (dialog, which) -> {
                    puntoPartida = lugar;
                    Toast.makeText(MainActivity.this, "Punto de partida seleccionado: " + lugar.getNombre(), Toast.LENGTH_SHORT).show();
                    if (puntoDestino != null) {
                        obtenerRuta(puntoPartida, puntoDestino);  // Llamar cuando ambos puntos estén seleccionados
                    }
                })
                .setNegativeButton("Destino", (dialog, which) -> {
                    puntoDestino = lugar;
                    Toast.makeText(MainActivity.this, "Punto de destino seleccionado: " + lugar.getNombre(), Toast.LENGTH_SHORT).show();
                    if (puntoPartida != null) {
                        obtenerRuta(puntoPartida, puntoDestino);  // Llamar cuando ambos puntos estén seleccionados
                    }
                })
                .show();
    }

    private void obtenerRuta(LugarTuristico puntoPartida, LugarTuristico puntoDestino) {
        String url = "https://router.project-osrm.org/route/v1/driving/" +
                puntoPartida.getUbicacion().getLongitude() + "," + puntoPartida.getUbicacion().getLatitude() +
                ";" + puntoDestino.getUbicacion().getLongitude() + "," + puntoDestino.getUbicacion().getLatitude() +
                "?overview=full&geometries=geojson";

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

                    return response.toString(); // La respuesta en formato JSON
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    mostrarRutaEnMapa(result);  // Mostrar la ruta en el mapa
                }
            }
        }.execute();
    }

    private void mostrarRutaEnMapa(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                String geometry = route.getString("geometry");

                Polyline polyline = new Polyline();
                polyline.setPoints(GeoJsonParser.parseGeoJson(geometry));
                mapView.getOverlays().add(polyline);

                mapView.invalidate();  // Redibujar el mapa
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


