package com.example.earthquakemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Objects;

import Model.EarthQuake;
import UI.CustomWindow;
import Util.Constants;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        queue= Volley.newRequestQueue(this);
        getEarthQuakes();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
        PackageManager.PERMISSION_GRANTED){
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Location location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        }
    }
    private void getEarthQuakes() {
        final EarthQuake earthQuake=new EarthQuake();

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, Constants.URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features=response.getJSONArray("features");
                            for (int i=0;i<Constants.LIMIT;i++){
                                JSONObject properties=features.getJSONObject(i).getJSONObject("properties");
                                JSONObject geometry=features.getJSONObject(i).getJSONObject("geometry");
                                JSONArray coordinates=geometry.getJSONArray("coordinates");

                                double lon=coordinates.getDouble(0);
                                double lat=coordinates.getDouble(1);
                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                java.text.DateFormat dateFormat=java.text.DateFormat.getDateInstance();
                                String formattedDate=dateFormat.format(new Date(Long.valueOf(properties.getLong("time")))
                                        .getTime());

                                MarkerOptions markerOptions=new MarkerOptions();
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.position(new LatLng(lat, lon));
                                markerOptions.snippet("Magnitude: "+earthQuake.getMagnitude()+"\n"+"Date: "+formattedDate);

                                Marker marker=mMap.addMarker(markerOptions);
                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),1));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
    });
        queue.add(jsonObjectRequest);
 }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String url=marker.getTag().toString();
        Log.d("Kartik", "onInfoWindowClick: "+url);
        getQuakeDetail(url);
    }

    private void getQuakeDetail( String url) {
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String detailsUrl="";
                try {
                    JSONObject properties=response.getJSONObject("properties");
                    JSONObject products=properties.getJSONObject("products");
                    JSONArray geoserve=products.getJSONArray("nearby-cities");

                    for(int i=0;i<geoserve.length();i++){
                     JSONObject geoserveObj=geoserve.getJSONObject(i);

                        JSONObject contentObj = geoserveObj.getJSONObject("contents");
                        JSONObject geoJsonObj=contentObj.getJSONObject("nearby-cities.json");

                     detailsUrl=geoJsonObj.getString("url");

                    }
                    Log.d("kartik", "onResponse: "+detailsUrl);
                    getMoreDetail(detailsUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    public void getMoreDetail(String url){
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialogBuilder=new AlertDialog.Builder(MapsActivity.this);
                        View view=getLayoutInflater().inflate(R.layout.popup,null);

                        Button dismissButton= (Button) view.findViewById(R.id.buttonDismiss);
                        Button dismissTopButton= (Button) view.findViewById(R.id.dismissPopup);
                        TextView popList=(TextView) view.findViewById(R.id.popList);
                        WebView htmlPop=(WebView) view.findViewById(R.id.htmlWebview);

                        StringBuilder stringBuilder=new StringBuilder();

                        try {
                            JSONArray cities=response.getJSONArray("cities");

                            for(int i=0;i<cities.length();i++ ){
                                JSONObject citiesObj=cities.getJSONObject(i);

                                stringBuilder.append("City: "+citiesObj.getString("name")+"\n"
                                +"Distance: "+citiesObj.getString("distance"+"\n"+"Populatuion: "
                                +citiesObj.getString("population")));

                                stringBuilder.append("\n\n");
                            }
                            popList.setText(stringBuilder);

                            dialogBuilder.setView(view);
                            dialog=dialogBuilder.create();
                            dialog.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}