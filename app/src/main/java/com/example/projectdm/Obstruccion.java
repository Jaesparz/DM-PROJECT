package com.example.projectdm;


import org.osmdroid.util.GeoPoint;

public class Obstruccion {
    private String nombreCalle;
    private GeoPoint ubicacion; // Solo una ubicación
    private String tipo; // "Tráfico" o "Obstrucción"
    private String descripcion; // Descripción detallada

    public Obstruccion(String nombreCalle, GeoPoint ubicacion, String tipo, String descripcion) {
        this.nombreCalle = nombreCalle;
        this.ubicacion = ubicacion;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public String getNombreCalle() {
        return nombreCalle;
    }

    public GeoPoint getUbicacion() {
        return ubicacion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
