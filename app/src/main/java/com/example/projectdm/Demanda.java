package com.example.projectdm;

import org.osmdroid.util.GeoPoint;

public class Demanda {
    private String descripcion;
    private GeoPoint ubicacionEntrega;
    private double recompensa; // En dólares
    private double distanciaEstimada; // En kilómetros

    public Demanda(String descripcion, GeoPoint ubicacionEntrega, double recompensa) {
        this.descripcion = descripcion;
        this.ubicacionEntrega = ubicacionEntrega;
        this.recompensa = recompensa;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public GeoPoint getUbicacionEntrega() {
        return ubicacionEntrega;
    }

    public double getRecompensa() {
        return recompensa;
    }

    public double getDistanciaEstimada() {
        return distanciaEstimada;
    }

    public void setDistanciaEstimada(double distanciaEstimada) {
        this.distanciaEstimada = distanciaEstimada;
    }
}
