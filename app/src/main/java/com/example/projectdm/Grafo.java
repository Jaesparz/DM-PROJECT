package com.example.projectdm;

import android.os.Build;

import org.osmdroid.util.GeoPoint;


import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Grafo {
    private Map<String, Map<String, Double>> adyacencias = new HashMap<>();
    private Map<String, String> predecesores = new HashMap<>();

    // Agregar una conexión entre nodos
    public void agregarArista(String origen, String destino, double peso) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            adyacencias.putIfAbsent(origen, new HashMap<>());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            adyacencias.putIfAbsent(destino, new HashMap<>());
        }
        adyacencias.get(origen).put(destino, peso);
        adyacencias.get(destino).put(origen, peso); // Si la conexión es bidireccional
    }

    // Obtener los nodos
    public Set<String> obtenerNodos() {
        return adyacencias.keySet();
    }

    // Obtener los vecinos de un nodo
    public Map<String, Double> obtenerVecinos(String nodo) {
        Map<String, Double> xd = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            xd  = adyacencias.getOrDefault(nodo, Collections.emptyMap());
        }
        return xd;
    }

    // Algoritmo de Dijkstra con predecesores
    public Map<String, Double> dijkstra(String origen) {
        Map<String, Double> distancias = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pq = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
        }

        distancias.put(origen, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(origen, 0.0));

        while (!pq.isEmpty()) {
            Map.Entry<String, Double> nodoActual = pq.poll();
            String nodo = nodoActual.getKey();
            double distancia = nodoActual.getValue();

            for (Map.Entry<String, Double> vecino : obtenerVecinos(nodo).entrySet()) {
                double nuevaDistancia = distancia + vecino.getValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (nuevaDistancia < distancias.getOrDefault(vecino.getKey(), Double.MAX_VALUE)) {
                        distancias.put(vecino.getKey(), nuevaDistancia);
                        predecesores.put(vecino.getKey(), nodo); // Guardar el predecesor
                        pq.add(new AbstractMap.SimpleEntry<>(vecino.getKey(), nuevaDistancia));
                    }
                }
            }
        }

        return distancias;
    }

    // Método para reconstruir el camino más corto
    public List<String> reconstruirCamino(String origen, String destino) {
        List<String> camino = new ArrayList<>();
        String nodoActual = destino;

        while (nodoActual != null && !nodoActual.equals(origen)) {
            camino.add(nodoActual);
            nodoActual = predecesores.get(nodoActual);
        }

        if (nodoActual != null) {
            camino.add(origen); // Añadir el origen al camino
        }

        Collections.reverse(camino); // Invertir el camino
        return camino;
    }
}