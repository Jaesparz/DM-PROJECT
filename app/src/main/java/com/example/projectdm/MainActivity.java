package com.example.projectdm;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.Projection;
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
    private boolean enVistaDeGrafo = false; // Estado para alternar vistas
    private List<LugarTuristico> lugares; // Lugares existentes
    private List<Polyline> aristasGrafo = new ArrayList<>();
    private LugarTuristico nodoPersonalizado; // Ahora almacenará un LugarTuristico


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establecer el diseño de la actividad
        setContentView(R.layout.activity_main);

        // Configurar OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // Configurar el botón flotante
        FloatingActionButton btnToggleGraphView = findViewById(R.id.btnToggleGraphView);
        btnToggleGraphView.setOnClickListener(v -> alternarVistaDeGrafo());


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

        // Inicializar los lugares
        lugares = generarLugares();

        // Lugares Turisticos (vista normal)

        agregarMarcadores();

        // Generar las aristas entre los nodos
        generarAristas();

        // Muestra las obstrucciones en el mapa
        agregarObstrucciones();


        //Bienvenida con polito

        mostrarBienvenidaConPolito();


        FloatingActionButton btnSetCustomStart = findViewById(R.id.btnNodoPersonalizado);
        btnSetCustomStart.setOnClickListener(v -> mostrarDialogoNodoPersonalizado());


        FloatingActionButton btnRecomendaciones = findViewById(R.id.btnRecomendarLugar);
        btnRecomendaciones.setOnClickListener(v -> mostrarRecomendacionesPolito());


        // 🔹 Deshabilitar la actualización automática de la posición de los nodos
        mapView.setTilesScaledToDpi(false);
        mapView.setFlingEnabled(false);



    }


    private List<Obstruccion> obstrucciones = Arrays.asList(
            new Obstruccion("TRAFICO FUERTE", new GeoPoint(-2.155259, -79.892787), "Tráfico", "Embotellamiento de Trafico"),
            new Obstruccion("CHOQUE GRAVE", new GeoPoint(-2.147711, -79.964547), "Obstrucción", "Choque frontal")
    );

    private void agregarObstrucciones() {
        for (Obstruccion obstruccion : obstrucciones) {
            Marker marker = new Marker(mapView);
            marker.setPosition(obstruccion.getUbicacion());
            marker.setTitle(obstruccion.getNombreCalle());
            marker.setIcon(resizeIcon(getResources().getDrawable(R.drawable.ic_obstruction, null), 60, 60)); // Ícono personalizado
            marker.setOnMarkerClickListener((m, map) -> {
                mostrarDetalleObstruccion(obstruccion); // Mostrar detalles al seleccionar
                return true; // Evitar el comportamiento predeterminado
            });
            mapView.getOverlays().add(marker); // Agregar marcador al mapa
        }

        mapView.invalidate(); // Refrescar el mapa
    }


    private void mostrarDetalleObstruccion(Obstruccion obstruccion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalle de la Obstrucción")
                .setMessage("Calle: " + obstruccion.getNombreCalle() +
                        "\nTipo: " + obstruccion.getTipo() +
                        "\nDescripción: " + obstruccion.getDescripcion())
                .setPositiveButton("Entendido", null)
                .show();
    }


    private void verificarObstruccionesEnRuta(List<GeoPoint> ruta) {
        boolean hayObstruccion = false;
        StringBuilder mensaje = new StringBuilder("La ruta pasa por las siguientes obstrucciones:\n");

        for (GeoPoint punto : ruta) {
            for (Obstruccion obstruccion : obstrucciones) {
                if (punto.equals(obstruccion.getUbicacion())) {
                    hayObstruccion = true;
                    mensaje.append("- ").append(obstruccion.getNombreCalle())
                            .append(": ").append(obstruccion.getDescripcion()).append("\n");
                }
            }
        }

        if (hayObstruccion) {
            // Mostrar notificación y buscar nueva ruta
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Obstrucciones en la Ruta")
                    .setMessage(mensaje.toString())
                    .setPositiveButton("Buscar Ruta Alternativa", (dialog, which) -> {
                        buscarRutaAlternativa();
                    })
                    .setNegativeButton("Aceptar", null)
                    .show();
        }
    }


    private void buscarRutaAlternativa() {
        Toast.makeText(this, "Buscando ruta alternativa...", Toast.LENGTH_SHORT).show();
        // Implementa la lógica para buscar una nueva ruta
    }








    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destruir el MapView para liberar recursos
        if (mapView != null) {
            mapView.onDetach();
        }
    }




    // Generar las aristas del grafo
    private void generarAristas() {
        aristasGrafo.clear(); // Limpiar aristas existentes

        // Crear aristas entre nodos consecutivos
        for (int i = 0; i < lugares.size() - 1; i++) {
            LugarTuristico inicio = lugares.get(i);
            LugarTuristico fin = lugares.get(i + 1);

            // Crear una polilínea para la arista
            Polyline polyline = new Polyline();
            polyline.setPoints(Arrays.asList(inicio.getUbicacion(), fin.getUbicacion()));
            polyline.getOutlinePaint().setColor(Color.GRAY); // Color de la arista
            polyline.getOutlinePaint().setStrokeWidth(5f); // Grosor de la línea

            aristasGrafo.add(polyline);
        }
    }

    // Alternar entre la vista normal y la vista de grafo
    private void alternarVistaDeGrafo() {
        enVistaDeGrafo = !enVistaDeGrafo;
        mapView.getOverlays().clear(); // Limpiar las superposiciones actuales

        if (enVistaDeGrafo) {
            mostrarGrafo(); // Mostrar nodos y aristas
        } else {
            agregarMarcadores(); // Volver a la vista normal
        }

        mapView.invalidate(); // Redibujar el mapa
    }

    // Mostrar nodos y aristas en la vista de grafo
    // Mostrar nodos en la vista de grafo
    private void mostrarGrafo() {
        // Dibujar nodos
        for (LugarTuristico lugar : lugares) {
            Marker marker = new Marker(mapView);
            marker.setPosition(lugar.getUbicacion());
            marker.setIcon(resizeIcon(getResources().getDrawable(R.drawable.ic_node, null), 50, 50)); // Tamaño ajustado
            marker.setTitle(lugar.getNombre());
            mapView.getOverlays().add(marker);
        }

        // Dibujar aristas
        for (Polyline arista : aristasGrafo) {
            mapView.getOverlays().add(arista);
        }
    }

    // Método para redimensionar el ícono
    private Drawable resizeIcon(Drawable icon, int width, int height) {
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return new BitmapDrawable(getResources(), resizedBitmap);
    }

    private List<LugarTuristico> generarLugares() {
        return Arrays.asList(


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
    }
    private void agregarMarcadores() {
        for (LugarTuristico lugar : lugares) {
            Marker marker = new Marker(mapView);
            marker.setPosition(lugar.getUbicacion());
            marker.setTitle(lugar.getNombre());

            // Usamos un icono personalizado
            marker.setIcon(resizeIcon(getResources().getDrawable(R.drawable.ic_custom_marker, null), 60, 60));


            marker.setOnMarkerClickListener((m, map) -> {
                mapView.getController().setCenter(m.getPosition()); // Bloquear en la posición real
                mostrarOpciones(lugar);
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();  // Redibujar el mapa
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

                // Mostrar la ruta más corta de forma animada
                trazarRutaAnimada(camino1, Color.BLUE);

                // Verificar si la ruta pasa por obstrucciones
                verificarObstruccionesEnRuta(camino1);



                // Calcular el tiempo estimado
                double tiempoRuta1 = calcularTiempo(distanciaKm1);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ruta más corta")
                        .setMessage("Distancia: " + String.format("%.2f", distanciaKm1) + " km\n" +
                                "Tiempo estimado: " + (int) tiempoRuta1 + " minutos.")
                        .setPositiveButton("Aceptar", null)
                        .show();

                // Mostrar ruta alternativa después de 10 segundos
                if (routes.length() > 1) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mostrarRutaAlternativa(jsonResponse, distanciaKm1, tiempoRuta1);
                    }, 10000);
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
                JSONObject route2 = routes.getJSONObject(1);
                String geometry2 = route2.getString("geometry");
                List<GeoPoint> camino2 = GeoJsonParser.parseGeoJson(geometry2);

                double distanciaKm2 = route2.getDouble("distance") / 1000;
                double tiempoRuta2 = calcularTiempo(distanciaKm2);
                double diferenciaTiempo = tiempoRuta2 - tiempoRuta1;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Ruta alternativa")
                        .setMessage("Distancia: " + String.format("%.2f", distanciaKm2) + " km\n" +
                                "Tiempo estimado: " + (int) tiempoRuta2 + " minutos.\n" +
                                "Esta ruta tomará aproximadamente " + (int) diferenciaTiempo + " minutos más.")
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            trazarRutaAnimada(camino2, Color.RED); // Animar la ruta alternativa
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

    private void trazarRutaAnimada(List<GeoPoint> puntos, int color) {
        // Crear una polilínea para la ruta
        Polyline polyline = new Polyline();
        polyline.getOutlinePaint().setColor(color); // Color del camino
        polyline.getOutlinePaint().setStrokeWidth(10f); // Ancho de la línea

        // Lista para los puntos que se van añadiendo progresivamente
        List<GeoPoint> puntosAnimados = new ArrayList<>();

        // Handler para animar
        Handler handler = new Handler(Looper.getMainLooper());

        // Velocidad de la animación (en milisegundos)
        int delay = 50;

        // Animar punto a punto
        for (int i = 0; i < puntos.size(); i++) {
            int finalI = i;
            handler.postDelayed(() -> {
                puntosAnimados.add(puntos.get(finalI)); // Añadir el siguiente punto
                polyline.setPoints(puntosAnimados); // Actualizar la polilínea
                mapView.getOverlays().add(polyline); // Añadir la polilínea al mapa
                mapView.invalidate(); // Redibujar el mapa
            }, delay * i); // Incrementar el tiempo de retraso para cada punto
        }
    }




    //POLITO


    private void mostrarBienvenidaConPolito() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_polito, null);

        ImageView imageViewPolito = dialogView.findViewById(R.id.imageViewPolito);
        TextView textViewMensaje = dialogView.findViewById(R.id.textViewMensaje);
        Button buttonAceptar = dialogView.findViewById(R.id.buttonAceptar);

        imageViewPolito.setImageResource(R.drawable.polito);
        textViewMensaje.setText("Hola mi querido politénico, espero tengas un buen día y estés AP.");

        AlertDialog dialog = builder.setView(dialogView).create();

        buttonAceptar.setOnClickListener(v -> {
            textViewMensaje.setText("Este mapa nos servirá para encontrar el camino más corto entre los lugares marcados en el mapa. "
                    + "Al seleccionar un lugar podrás establecerlo como punto de partida o destino.");
            buttonAceptar.setText("Siguiente");

            buttonAceptar.setOnClickListener(v2 -> {
                textViewMensaje.setText("Cuando selecciones una partida y un destino, se ejecutará un algoritmo para llevarte por el camino más corto.");
                buttonAceptar.setText("Siguiente");

                buttonAceptar.setOnClickListener(v3 -> {
                    textViewMensaje.setText("Estaré aquí por si me necesitas.");
                    buttonAceptar.setText("OK");

                    buttonAceptar.setOnClickListener(v4 -> dialog.dismiss()); // Finaliza el flujo de mensajes
                });
            });
        });

        dialog.show();
    }




    //NODO INCIAL DEL USUARIO

    private void mostrarDialogoNodoPersonalizado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Crear una vista personalizada para incluir la imagen de Polito
        View customView = getLayoutInflater().inflate(R.layout.dialog_polito, null);
        ImageView imageView = customView.findViewById(R.id.imageViewPolito);
        imageView.setImageResource(R.drawable.polito_guinando); // Imagen de Polito preguntando

        TextView textView = customView.findViewById(R.id.textViewMensaje);
        textView.setText("¿Deseas un lugar inicial personalizado?");

        builder.setView(customView)
                .setPositiveButton("Sí", (dialog, which) -> activarSeleccionPersonalizada())
                .setNegativeButton("No", null)
                .show();
    }

    private void agregarMarcadorNodoPersonalizado(GeoPoint punto) {
        Marker marker = new Marker(mapView);
        marker.setPosition(punto);
        marker.setTitle("Punto Personalizado");

        // ✅ ICONO DIFERENTE para el nodo personalizado
        marker.setIcon(resizeIcon(getResources().getDrawable(R.drawable.ic_backpack, null), 70, 70));

        // 🔹 Bloquea el movimiento del nodo personalizado tras zoom o ajustes
        marker.setOnMarkerClickListener((m, map) -> {
            mapView.getController().setCenter(m.getPosition()); // Mantiene la posición exacta
            return true;
        });

        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }




    private void activarSeleccionPersonalizada() {
        Toast.makeText(this, "Toca el mapa para elegir el punto de inicio.", Toast.LENGTH_SHORT).show();

        mapView.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Projection projection = mapView.getProjection();
                GeoPoint punto = (GeoPoint) projection.fromPixels((int) event.getX(), (int) event.getY());

                // Crear un LugarTuristico con icono diferente
                nodoPersonalizado = new LugarTuristico("Punto Personalizado", punto, "Ubicación personalizada", "file");

                // ✅ Marcarlo automáticamente como punto de partida
                puntoPartida = nodoPersonalizado;

                // Agregar marcador con nuevo icono
                agregarMarcadorNodoPersonalizado(punto);

                mapView.setOnTouchListener(null);
                Toast.makeText(this, "Punto de partida personalizado seleccionado.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }


    // RECOMENDACIONES

    private void mostrarRecomendacionesPolito() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configurar imagen de Polito más grande
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.polito_recomend);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(30, 30, 30, 30);

        builder.setTitle("Polito te recomienda")
                .setView(imageView)
                .setMessage("Te recomiendo visitar estos lugares en ESPOL.")
                .setPositiveButton("Ver Opciones", (dialog, which) -> mostrarListaRecomendaciones())
                .show();
    }


    private void mostrarListaRecomendaciones() {
        if (puntoPartida == null && nodoPersonalizado == null) {
            Toast.makeText(this, "Selecciona un punto de partida primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        LugarTuristico puntoInicio = nodoPersonalizado != null ? nodoPersonalizado : puntoPartida;

        String[] lugaresRecomendados = {
                "Comedor FADCOM", "Sweet & Coffe", "LA VACA", "Parque AJÁ",
                "FRESH FOOD", "LAGO ESPOL (VISTA)", "COMEDOR CARPA ROJA", "Frutanga","Piscina"
        };

        List<String> listaLugares = new ArrayList<>(); // Usamos una lista dinámica

        for (String nombreLugar : lugaresRecomendados) {
            LugarTuristico destino = obtenerLugarPorNombre(nombreLugar);
            if (destino != null) { // 🔥 EVITAMOS EL ERROR DE NULL
                double distanciaKm = calcularDistancia(puntoInicio.getUbicacion(), destino.getUbicacion());
                listaLugares.add(nombreLugar + " (" + String.format("%.2f", distanciaKm) + " km)");
            }
        }

        if (listaLugares.isEmpty()) {
            Toast.makeText(this, "No se encontraron lugares recomendados.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] lugaresConDistancia = listaLugares.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Configurar imagen de Polito en la lista
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.polito_guinando);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setPadding(30, 30, 30, 30);

        builder.setTitle("Polito - Lugares Recomendados")
                .setView(imageView)
                .setItems(lugaresConDistancia, (dialog, which) -> seleccionarLugarDestino(lugaresRecomendados[which]))
                .show();
    }


    private void seleccionarLugarDestino(String lugarSeleccionado) {
        @SuppressLint({"NewApi", "LocalSuppress"})
        LugarTuristico destino = lugares.stream()
                .filter(lugar -> lugar.getNombre().equals(lugarSeleccionado))
                .findFirst()
                .orElse(null);

        if (destino != null) {
            if (nodoPersonalizado != null) {
                puntoPartida = nodoPersonalizado; // 🔹 Si hay un nodo personalizado, se usa como punto de partida
            } else if (puntoPartida == null) {
                Toast.makeText(this, "Selecciona un punto de partida primero.", Toast.LENGTH_SHORT).show();
                return;
            }

            puntoDestino = destino; // 🔹 Se asigna el destino seleccionado
            obtenerRuta(puntoPartida, puntoDestino); // 🔹 Se usa `obtenerRuta` con `LugarTuristico`, no con `GeoPoint`
        }
    }
    @SuppressLint("NewApi")
    private LugarTuristico obtenerLugarPorNombre(String nombre) {
        return lugares.stream()
                .filter(lugar -> lugar.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);
    }
    private double calcularDistancia(GeoPoint punto1, GeoPoint punto2) {
        double lat1 = punto1.getLatitude();
        double lon1 = punto1.getLongitude();
        double lat2 = punto2.getLatitude();
        double lon2 = punto2.getLongitude();

        double radioTierra = 6371.0; // Radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return radioTierra * c;
    }





}





















