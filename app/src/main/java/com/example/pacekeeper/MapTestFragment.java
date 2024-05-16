package com.example.pacekeeper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;

public class MapTestFragment extends Fragment {

    private MapView mapView;

    private ImageView routeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_test_static_image, container, false);

        // Create a map programmatically and set the initial camera
      /*  mapView = new MapView(requireContext());
        mapView.getMapboxMap().setCamera(
                new CameraOptions.Builder()
                        .center(Point.fromLngLat(-98.0, 39.5))
                        .pitch(0.0)
                        .zoom(2.0)
                        .bearing(0.0)
                        .build()
        );

       */

        // Add the map view to the fragment's view
        /*
        ViewGroup mapViewLayout = rootView.findViewById(R.id.route_image);
        mapViewLayout.addView(mapView);
         */

        routeView = rootView.findViewById(R.id.route_image);

        Glide.with(this).load(getUrl()).into(routeView);
        return rootView;
    }
    
    /*
    public String setMapImage() {
        try {
            // Set your Mapbox access token
            String accessToken = System.getProperty("MAPBOX_DOWNLOADS_TOKEN");

            // Set the coordinates for the center of the map
            double longitude = -98.0;
            double latitude = 39.5;

            // Set the zoom level of the map
            int zoom = 10;

            // Set the size of the image in pixels (width x height)
            int width = 800;
            int height = 600;

            // Construct the URL for the static map image
            String imageUrl = String.format("https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/%f,%f,%d/%dx%d?access_token=%s",
                    longitude, latitude, zoom, width, height, URLEncoder.encode(accessToken, "UTF-8"));

            // Download the image
            URL url = new URL(imageUrl);
            String destinationFile = "routeImage.png";

            File file = new File(destinationFile);

            if (file.exists()) {
                System.out.println("File exists.");
            } else {
                try {
                    if (file.createNewFile()) {
                        System.out.println("File created successfully.");
                    } else {
                        System.out.println("File creation failed.");
                    }
                } catch (IOException e) {
                    System.out.println("An error occurred while creating the file: " + e.getMessage());
                }
            }

            InputStream is = url.openStream();
            OutputStream os = Files.newOutputStream(Paths.get(destinationFile));

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();

            System.out.println("Static map image URL: " + imageUrl);
            return destinationFile;
        } catch (IOException e) {
            e.printStackTrace();
            return "Entered catch";
        }
    }

     */

        protected String getUrl() {
                // Set your Mapbox access token
                String accessToken = System.getProperty("MAPBOX_DOWNLOADS_TOKEN");

                // Set the coordinates for the center of the map
                double longitude = -98.0;
                double latitude = 39.5;

                // Set the zoom level of the map
                int zoom = 10;

                // Set the size of the image in pixels (width x height)
                int width = 800;
                int height = 600;

                    String imageUrl = String.format("https://api.mapbox.com/styles/v1/mapbox/streets-v11/static/%f,%f,%d/%dx%d?access_token=%s",
                            longitude, latitude, zoom, width, height,accessToken);

                    imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cat_August_2010-4.jpg/1200px-Cat_August_2010-4.jpg";
                    return imageUrl;


                // Construct the URL for the static map image



        }



}
