package com.example.projectdm;

import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Dijkstra {
    public static List<String> calcularCaminoMasCorto(Grafo grafo, String inicio, String destino) {
        Map<String, Double> distancias = new HashMap<>();
        Map<String, String> previos = new HashMap<>();
        PriorityQueue<String> cola = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        }

        for (String nodo : grafo.obtenerNodos()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }
        distancias.put(inicio, 0.0);
        cola.add(inicio);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            if (actual.equals(destino)) break;

            for (Map.Entry<String, Double> vecino : grafo.obtenerVecinos(actual).entrySet()) {
                String nodoVecino = vecino.getKey();
                double nuevaDistancia = distancias.get(actual) + vecino.getValue();

                if (nuevaDistancia < distancias.get(nodoVecino)) {
                    distancias.put(nodoVecino, nuevaDistancia);
                    previos.put(nodoVecino, actual);
                    cola.add(nodoVecino);
                }
            }
        }

        List<String> camino = new ArrayList<>();
        for (String at = destino; at != null; at = previos.get(at)) {
            camino.add(at);
        }
        Collections.reverse(camino);
        return camino;
    }
}

