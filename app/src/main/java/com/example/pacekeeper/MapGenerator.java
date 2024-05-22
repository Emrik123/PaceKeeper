package com.example.pacekeeper;
import android.location.Location;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.models.StaticPolylineAnnotation;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;


import java.util.ArrayList;
import java.util.List;

public class MapGenerator {
    
    public MapGenerator(){
    }



        public String getUrl(String accessToken, Session currentSession) {

            ArrayList<Point> routeCoordinates = currentSession.getRoute();
            List<StaticPolylineAnnotation> polylineAnnotation = new ArrayList<>();

            String polyline = PolylineUtils.encode(routeCoordinates, Constants.PRECISION_5);
            StaticPolylineAnnotation annotation = StaticPolylineAnnotation.builder()
                    .polyline(polyline)
                    .strokeColor("f90b0b")
                    .strokeWidth(4.0)
                    .build();

            polylineAnnotation.add(annotation);

            MapboxStaticMap staticImage = MapboxStaticMap.builder()
                    .accessToken(accessToken)
                    .staticPolylineAnnotations(polylineAnnotation)
                    .styleId("streets-v12")
                    .cameraAuto(true)
                    .cameraZoom(13)
                    .width(350)
                    .height(350)
                    .attribution(true)
                    .retina(true)
                    .build();

            System.out.println(staticImage.url());
            return staticImage.url().toString();

        }

    public String getUrlFromStoredSession(String accessToken, Session.StoredSession session) {

        ArrayList<Point> routeCoordinates = session.getRoute();
        List<StaticPolylineAnnotation> polylineAnnotation = new ArrayList<>();

        String polyline = PolylineUtils.encode(routeCoordinates, Constants.PRECISION_5);
        StaticPolylineAnnotation annotation = StaticPolylineAnnotation.builder()
                .polyline(polyline)
                .strokeColor("f90b0b")
                .strokeWidth(4.0)
                .build();

        polylineAnnotation.add(annotation);

        MapboxStaticMap staticImage = MapboxStaticMap.builder()
                .accessToken(accessToken)
                .staticPolylineAnnotations(polylineAnnotation)
                .styleId("streets-v12")
                .cameraAuto(true)
                .cameraZoom(13)
                .width(350)
                .height(350)
                .attribution(true)
                .retina(true)
                .build();

        System.out.println(staticImage.url());
        return staticImage.url().toString();

    }



}
