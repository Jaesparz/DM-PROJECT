package com.example.projectdm;

import org.osmdroid.util.GeoPoint;

import org.osmdroid.util.GeoPoint;

public class LugarTuristico {
    private String nombre;
    private GeoPoint ubicacion;

    public LugarTuristico(String nombre, GeoPoint ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    public String getNombre() {
        return nombre;
    }

    public GeoPoint getUbicacion() {
        return ubicacion;
    }
}
