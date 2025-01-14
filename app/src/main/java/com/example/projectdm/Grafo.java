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

    public List<List<String>> dijkstraConSegundoCamino(String origen, String destino) {
        // Camino más corto
        Map<String, Double> distancias = dijkstra(origen);
        List<String> caminoMasCorto = reconstruirCamino(origen, destino);

        // Almacenar el peso del camino más corto
        double pesoMasCorto = distancias.get(destino);

        // Intentar encontrar el segundo camino más corto
        List<String> segundoCamino = null;
        double pesoSegundoMasCorto = Double.MAX_VALUE;

        for (int i = 0; i < caminoMasCorto.size() - 1; i++) {
            // Eliminar temporalmente una arista del camino más corto
            String nodoA = caminoMasCorto.get(i);
            String nodoB = caminoMasCorto.get(i + 1);
            double pesoAristaOriginal = adyacencias.get(nodoA).get(nodoB);

            adyacencias.get(nodoA).remove(nodoB);
            adyacencias.get(nodoB).remove(nodoA);

            // Recalcular el camino más corto sin esa arista
            Map<String, Double> distanciasTemp = dijkstra(origen);
            double nuevoPeso = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                nuevoPeso = distanciasTemp.getOrDefault(destino, Double.MAX_VALUE);
            }

            if (nuevoPeso > pesoMasCorto && nuevoPeso < pesoSegundoMasCorto) {
                pesoSegundoMasCorto = nuevoPeso;
                segundoCamino = reconstruirCamino(origen, destino);
            }

            // Restaurar la arista
            adyacencias.get(nodoA).put(nodoB, pesoAristaOriginal);
            adyacencias.get(nodoB).put(nodoA, pesoAristaOriginal);
        }

        // Retornar ambos caminos
        List<List<String>> caminos = new ArrayList<>();
        caminos.add(caminoMasCorto);
        if (segundoCamino != null) {
            caminos.add(segundoCamino);
        }

        return caminos;
    }
}
