package com.example.pacekeeper;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.models.StaticPolylineAnnotation;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;


import java.util.ArrayList;
import java.util.List;

/**
 * This class creates the route image with the use of MapBox
 * and returns a URL which is then used by Glide to get the
 * route image.
 */
public class MapGenerator {

    public MapGenerator(){
    }

    /**
     * Method which returns the url containing a route image.
     * This method is used with a current session (not a saved session).
     * @param accessToken the token which is needed to use the MapBox API.
     * @param currentSession the session which the coordinates is fetched from.
     * @return URL as a string
     */
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

    /**
     *Method which returns the url containing a route image.
     *This method is used with a stored session.
     * @param accessToken accessToken the token which is needed to use the MapBox API.
     * @param session the stored session from which the coordinates is retrieved.
     * @return URL as a string
     */
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
