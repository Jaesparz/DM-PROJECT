package com.example.projectdm;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonParser {

    public static List<GeoPoint> parseGeoJson(String geoJson) {
        List<GeoPoint> points = new ArrayList<>();

        try {
            // El GeoJSON se espera que tenga un formato de tipo "LineString"
            JSONArray coordinates = new JSONObject(geoJson).getJSONArray("coordinates");

            for (int i = 0; i < coordinates.length(); i++) {
                JSONArray coordinate = coordinates.getJSONArray(i);
                double lon = coordinate.getDouble(0);
                double lat = coordinate.getDouble(1);
                points.add(new GeoPoint(lat, lon));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return points;
    }
}
