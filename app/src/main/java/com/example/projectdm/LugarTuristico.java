package com.example.projectdm;

import org.osmdroid.util.GeoPoint;

import org.osmdroid.util.GeoPoint;

public class LugarTuristico {
    private String nombre;
    private GeoPoint ubicacion;
    private String descripcion;
    private String imagenUrl;

    // Constructor
    public LugarTuristico(String nombre, GeoPoint ubicacion, String descripcion, String imagenUrl) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public GeoPoint getUbicacion() {
        return ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }
}
