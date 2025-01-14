package com.example.projectdm;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

                //MALLS

                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787),"El mall mas grande","file"),
                new LugarTuristico("Mall del Sur", new GeoPoint(-2.229620, -79.898629),"Mall Peligroso","file"),
                new LugarTuristico("Riocentro Ceibos", new GeoPoint(-2.162932, -79.931266),"El mall mas hermoso","file"),
                new LugarTuristico("Riocentro Norte", new GeoPoint(-2.127823, -79.907100),"Mall bonito","file"),
                new LugarTuristico("Riocentro Entre Rios", new GeoPoint(-2.141862, -79.865193),"El mall aniñado","file"),
                new LugarTuristico("San Marino Shopping", new GeoPoint(-2.169124, -79.898621),"Q vuelva supercines","file"),
                new LugarTuristico("City Mall", new GeoPoint(-2.141375, -79.909117),"Mall zzzz","file"),
                new LugarTuristico("Policentro", new GeoPoint(-2.170923, -79.900732),"Solo por el supermaxi voy a este","file"),
                new LugarTuristico("Mall del Fortin", new GeoPoint(-2.109470, -79.948379),"dicen que por ahi matan","file"),
                new LugarTuristico("C.C Albán Borja", new GeoPoint(-2.168714, -79.916538),"Mall Abandonado","file"),

                //ESPOL

                new LugarTuristico("Rectorado", new GeoPoint(-2.147711, -79.964547),"Rectorado de la ESPOL","file"),
                new LugarTuristico("Biblioteca", new GeoPoint(-2.147588, -79.966033),"Donde Pides Un Libro Y lo llevas a pasear a tu casa","file"),
                new LugarTuristico("FCSH", new GeoPoint(-2.148194, -79.968581),"La facultad de las bonitas (asi me cuentan)","file"),
                new LugarTuristico("CELEX", new GeoPoint(-2.148548, -79.967819),"Aqui se ve los ingles","file"),
                new LugarTuristico("Canchas Fcsh", new GeoPoint(-2.147290, -79.968769),"Fulbito en fcsh","file"),
                new LugarTuristico("FIEC Nueva", new GeoPoint(-2.144698, -79.967873),"La facultad de los machos","file"),
                new LugarTuristico("GYM PROFESORES", new GeoPoint(-2.143722, -79.968023),"Gym de los profesores","file"),
                new LugarTuristico("Canchas de Futbol", new GeoPoint(-2.142994, -79.967600),"Sale Fulbito?","https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRVn8lvtioKScTFUMFczjzrj_PeW-gJ9VqN8w&s"),
                new LugarTuristico("Coliseo Nuevo", new GeoPoint(-2.142318, -79.967165),"Sale volley?","file"),
                new LugarTuristico("UBP", new GeoPoint(-2.143063, -79.967135),"Lugar en donde te pueden ayudar con problemas medicos al instante","file"),
                new LugarTuristico("Lab De Mecanica", new GeoPoint(-2.143259, -79.965789),"Lab De Pruebas de Materiales","file"),
                new LugarTuristico("Coliseo Viejo", new GeoPoint(-2.145048, -79.964182),"Sale pingpong?","file"),
                new LugarTuristico("FADCOM", new GeoPoint(-2.144151, -79.962358),"La Facultad Que no Sufre","file"),
                new LugarTuristico("Comedor FADCOM", new GeoPoint(-2.143610, -79.961355),"Aqui se come rico RECOMENDADO","file"),
                new LugarTuristico("Edificio de Posgrados", new GeoPoint(-2.143646, -79.966317),"Pa que saques tu posgrado","file"),
                new LugarTuristico("Parque AJÁ", new GeoPoint(-2.144082, -79.966961),"Parque interesante con investigaciones y curiosidades","file"),
                new LugarTuristico("LABS FIEC", new GeoPoint(-2.145198, -79.967294),"Labs De Computacion","file"),
                new LugarTuristico("Frutanga", new GeoPoint(-2.144891, -79.966221),"El famoso frutangas para evitar la gastritis","file"),
                new LugarTuristico("FIEC Vieja", new GeoPoint(-2.145433, -79.966087),"fiec vieja, parte de varias materias del basico","file"),
                new LugarTuristico("Sweet & Coffe", new GeoPoint(-2.146098, -79.966789),"Para tomarse un cafecito (carisimo)","file"),
                new LugarTuristico("FCNM 9M", new GeoPoint(-2.146822, -79.966800),"El famoso Basico donde todos algunas vez sufrimos","file"),
                new LugarTuristico("LA VACA", new GeoPoint(-2.147503, -79.967347),"La famosa Vaca, aqui tambien se come rico","file"),
                new LugarTuristico("LABS FISICA-QUIMICA", new GeoPoint(-2.146800, -79.967535),"Labs Donde se experimentan las practicas de fisica y quimica","file"),
                new LugarTuristico("BANCO DEL PACIFICO", new GeoPoint(-2.146425, -79.965100),"Para que saques tu billuzo","file"),
                new LugarTuristico("FRESH FOOD", new GeoPoint(-2.146446, -79.964676),"Otra opcion para comer, aunque queda un poco lejitos","file"),
                new LugarTuristico("FCNM (MARITIMA)", new GeoPoint(-2.147052, -79.962895),"Conocida como la facultad fantasma....","file"),
                new LugarTuristico("LAGO ESPOL (VISTA)", new GeoPoint(-2.145555, -79.962949),"Para trear a tu pelada","file"),
                new LugarTuristico("FICT", new GeoPoint(-2.145590, -79.965397),"La facultad de los ingenieros civiles","file"),
                new LugarTuristico("FEPOL", new GeoPoint(-2.145996, -79.966331),"nose que hacen pero, FEPOL","file"),
                new LugarTuristico("COMEDOR CARPA ROJA", new GeoPoint(-2.145612, -79.965124),"Para comerse un buen bollo con arroz","file"),
                new LugarTuristico("ESTACION DE BUSES", new GeoPoint(-2.145113, -79.965344),"Para tomar la ruta hacia tu hogar","file"),
                new LugarTuristico("FIMCP", new GeoPoint(-2.144009, -79.965910),"La Facultad de Ingenieria Mecanica Y Ciencias De la Produccion","file"),
                new LugarTuristico("Piscina", new GeoPoint(-2.152351, -79.958432),"Para que te quites el calorón de estas fechas","file"),
                new LugarTuristico("Gimnasio Estudiantes", new GeoPoint(-2.153374, -79.959311),"Para que te pongas bien pepudo para esa pelada","file"),
                new LugarTuristico("Residencia Estudiantes", new GeoPoint(-2.153727, -79.956855),"Residencia de Estudiantes Politecnicos (en contenedores)","file"),
                new LugarTuristico("FCV", new GeoPoint(-2.151785, -79.956865),"La facultad aislada, pero con peladas guapas","file"),
                new LugarTuristico("Garita", new GeoPoint(-2.152559, -79.953389),"Ingreso A LA ESPOL","file"),
                new LugarTuristico("ADMISIONES", new GeoPoint(-2.150284, -79.949473),"Donde alguna vez pasamos un infierno total","file")






                //PA TURISTEAR

//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787)),
//                new LugarTuristico("Mall del Sol", new GeoPoint(-2.155259, -79.892787))


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

        // Crear una vista personalizada para mostrar la imagen y descripción
        View customView = getLayoutInflater().inflate(R.layout.dialog_lugar_turistico, null);
        ImageView imageView = customView.findViewById(R.id.imageViewLugar);
        TextView descripcionText = customView.findViewById(R.id.descripcionLugar);

        // Establecer la imagen y descripción del lugar
        Picasso.get().load(lugar.getImagenUrl()).into(imageView);  // Usamos Picasso para cargar la imagen desde la URL
        descripcionText.setText(lugar.getDescripcion());  // Establecemos la descripción

        builder.setView(customView)
                .setTitle(lugar.getNombre())
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
                        // Mostrar cuadro de diálogo para seleccionar transporte antes de calcular la ruta
                        seleccionarTransporte(() -> obtenerRuta(puntoPartida, puntoDestino));
                    }
                })
                .show();
    }




    private void obtenerRuta(LugarTuristico puntoPartida, LugarTuristico puntoDestino) {
        String url = "https://router.project-osrm.org/route/v1/driving/" +
                puntoPartida.getUbicacion().getLongitude() + "," + puntoPartida.getUbicacion().getLatitude() +
                ";" + puntoDestino.getUbicacion().getLongitude() + "," + puntoDestino.getUbicacion().getLatitude() +
                "?overview=full&geometries=geojson&alternatives=true&steps=true&annotations=true";


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
                    mostrarRutaMasCorta(result); // Mostrar directamente la ruta más corta
                }
            }
        }.execute();
    }

    private void mostrarRutaMasCorta(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {
                // Obtener la primera ruta (camino más corto)
                JSONObject route1 = routes.getJSONObject(0);
                String geometry1 = route1.getString("geometry");
                List<GeoPoint> camino1 = GeoJsonParser.parseGeoJson(geometry1);

                double distanciaKm1 = route1.getDouble("distance") / 1000;

                // Mostrar la ruta más corta en el mapa
                trazarRutaEnMapa(camino1, Color.BLUE);

                // Calcular el tiempo estimado para la ruta más corta
                double tiempoRuta1 = calcularTiempo(distanciaKm1);

                // Mostrar tiempo estimado en un cuadro de diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ruta más corta")
                        .setMessage("Distancia: " + String.format("%.2f", distanciaKm1) + " km\n" +
                                "Tiempo estimado: " + (int) tiempoRuta1 + " minutos.")
                        .setPositiveButton("Aceptar", null)
                        .show();

                // Comprobar si hay una segunda ruta alternativa
                if (routes.length() > 1) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mostrarRutaAlternativa(jsonResponse, distanciaKm1, tiempoRuta1);
                    }, 10000); // Esperar 10 segundos antes de mostrar la opción de ruta alternativa
                } else {
                    Log.d("RUTA", "No se encontró una segunda ruta alternativa.");
                }
            } else {
                Log.d("RUTA", "No se encontraron rutas en la respuesta.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mostrarRutaAlternativa(String jsonResponse, double distanciaKm1, double tiempoRuta1) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 1) { // Comprobar si hay una ruta alternativa disponible
                // Obtener la segunda ruta (ruta alternativa)
                JSONObject route2 = routes.getJSONObject(1);
                String geometry2 = route2.getString("geometry");
                List<GeoPoint> camino2 = GeoJsonParser.parseGeoJson(geometry2);

                double distanciaKm2 = route2.getDouble("distance") / 1000;

                // Calcular tiempo estimado para la ruta alternativa
                double tiempoRuta2 = calcularTiempo(distanciaKm2);

                // Calcular diferencia de tiempo entre las rutas
                double diferenciaTiempo = tiempoRuta2 - tiempoRuta1;

                // Mostrar un diálogo con los detalles de la ruta alternativa
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ruta alternativa")
                        .setMessage("Distancia: " + String.format("%.2f", distanciaKm2) + " km\n" +
                                "Tiempo estimado: " + (int) tiempoRuta2 + " minutos.\n" +
                                "Esta ruta tomará aproximadamente " + (int) diferenciaTiempo + " minutos más.")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            trazarRutaEnMapa(camino2, Color.RED); // Mostrar la ruta alternativa en rojo
                            Toast.makeText(this, "Ruta alternativa marcada en rojo.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                Log.d("RUTA", "No hay rutas alternativas disponibles.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void trazarRutaEnMapa(List<GeoPoint> camino, int color) {
        Polyline polyline = new Polyline();
        polyline.setPoints(camino);
        polyline.getOutlinePaint().setColor(color); // Establecer el color de la ruta
        mapView.getOverlays().add(polyline);
        mapView.invalidate(); // Redibujar el mapa
    }



    private String modoTransporte; // Variable para guardar el modo seleccionado

    private void seleccionarTransporte(Runnable onSeleccionCompleta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Infla el diseño personalizado
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transporte, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Configurar clics en las imágenes
        dialogView.findViewById(R.id.optionWalking).setOnClickListener(v -> {
            modoTransporte = "walking";
            dialog.dismiss();
            onSeleccionCompleta.run(); // Ejecutar el callback cuando se selecciona
        });

        dialogView.findViewById(R.id.optionBus).setOnClickListener(v -> {
            modoTransporte = "bus";
            dialog.dismiss();
            onSeleccionCompleta.run();
        });

        dialogView.findViewById(R.id.optionCar).setOnClickListener(v -> {
            modoTransporte = "car";
            dialog.dismiss();
            onSeleccionCompleta.run();
        });

        dialog.show();
    }

    private double calcularTiempo(double distanciaKm) {
        double velocidadKmH;

        // Asignar velocidad según el modo de transporte seleccionado
        switch (modoTransporte) {
            case "walking":
                velocidadKmH = 5.0; // Velocidad a pie en km/h
                break;
            case "bus":
                velocidadKmH = 40.0; // Velocidad en bus en km/h
                break;
            case "car":
                velocidadKmH = 60.0; // Velocidad en carro en km/h
                break;
            default:
                velocidadKmH = 0; // Si no se seleccionó un transporte
        }

        // Calcular tiempo en horas y convertir a minutos
        return (distanciaKm / velocidadKmH) * 60; // Tiempo en minutos
    }



}


