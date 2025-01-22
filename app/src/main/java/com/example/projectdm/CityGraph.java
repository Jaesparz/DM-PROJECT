package com.example.projectdm;

import android.annotation.SuppressLint;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class CityGraph {
    public static class Edge {
        public Node from;
        public Node to;
        public int cost;
        public int color = Color.WHITE; // Color inicial

        public Edge(Node from, Node to, int cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }

    public static class Node {
        public int x, y;
        public String name;
        public List<Edge> edges = new ArrayList<>();

        public Node(int x, int y, String name) {
            this.x = x;
            this.y = y;
            this.name = name;
        }
    }

    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    // Métodos para agregar nodos y aristas
    public void addNode(int x, int y, String name) {
        nodes.add(new Node(x, y, name));
    }

    public void addEdge(Node from, Node to, int cost) {
        Edge edge = new Edge(from, to, cost);
        edges.add(edge);
        from.edges.add(edge); // Relación bidireccional
        to.edges.add(edge);
    }


    public List<CityGraph.Edge> calculateShortestPath(CityGraph.Node start, CityGraph.Node end) {
        // Distancias mínimas a cada nodo
        Map<CityGraph.Node, Integer> distances = new HashMap<>();
        // Nodo anterior para reconstruir el camino
        Map<CityGraph.Node, CityGraph.Node> previousNodes = new HashMap<>();
        // Cola de prioridad para procesar los nodos
        @SuppressLint({"NewApi", "LocalSuppress"}) PriorityQueue<CityGraph.Node> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        // Inicializar distancias y la cola
        for (CityGraph.Node node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
            previousNodes.put(node, null);
        }
        distances.put(start, 0);
        queue.add(start);

        // Algoritmo de Dijkstra
        while (!queue.isEmpty()) {
            CityGraph.Node current = queue.poll();

            // Si llegamos al nodo final, terminamos
            if (current == end) break;

            for (CityGraph.Edge edge : current.edges) {
                CityGraph.Node neighbor = edge.to;
                int newDist = distances.get(current) + edge.cost;

                // Actualizamos la distancia si encontramos un camino más corto
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Reconstruir el camino más corto
        List<CityGraph.Edge> shortestPath = new ArrayList<>();
        CityGraph.Node currentNode = end;

        while (previousNodes.get(currentNode) != null) {
            CityGraph.Node prevNode = previousNodes.get(currentNode);
            CityGraph.Edge edge = findEdgeBetween(prevNode, currentNode);
            shortestPath.add(0, edge); // Insertar al inicio para mantener el orden
            currentNode = prevNode;
        }

        return shortestPath;
    }

    CityGraph.Edge findEdgeBetween(CityGraph.Node from, CityGraph.Node to) {
        for (CityGraph.Edge edge : edges) {
            if (edge.from == from && edge.to == to) {
                return edge;
            }
        }
        return null;
    }




}
