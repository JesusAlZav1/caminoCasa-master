package com.example.caminocasa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MapView map = null;

    private MyLocationNewOverlay mLocationOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;

    private RequestQueue queue;
    private JsonObjectRequest requestMapRequest;
    private FusedLocationProviderClient fusedLocationClient;

    private ArrayList<GeoPoint> puntosDeRuta = new ArrayList<>();
    Double myLat;
    Double myLon;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fab = findViewById(R.id.fab);
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(19.0);
        GeoPoint startPoint = new GeoPoint(20.12643066801037, -101.2036718742518);
        mapController.setCenter(startPoint);

        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx
        ),map);
        this.mLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.mLocationOverlay);

        Marker startMarker = new Marker(map);

        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);

        List<GeoPoint> geoPoints = new ArrayList<>();

        geoPoints.add(startPoint);
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() +
                        "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        obtenerRouteFromMapRequest(20.139730, -101.150765);

    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();

    }

    private void obtenerRouteFromMapRequest(Double latitud, Double longitud){
        puntosDeRuta=new ArrayList<>();
        queue = Volley.newRequestQueue(this);
        requestMapRequest =
                new JsonObjectRequest(
                        "http://www.mapquestapi.com/directions/v2/route?key=EwNXPABPnLR6R250CknmVJPk8ZGaFnqp&from="
                                +latitud+","+longitud+"&to=20.12643066801037, -101.2036718742518",
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("GIVO", "se ejecuto");
                                try {
                                    JSONArray indicaiones =  response.getJSONObject("route")
                                            .getJSONArray("legs")
                                            .getJSONObject(0).
                                                    getJSONArray("maneuvers");
                                    for( int i =0 ;  i <indicaiones.length(); i++){
                                        JSONObject indi = indicaiones.getJSONObject(i);
                                        GeoPoint punto = new GeoPoint(
                                                Double.parseDouble(indi.getJSONObject("startPoint").get("lat").toString()),
                                                Double.parseDouble(indi.getJSONObject("startPoint").get("lng").toString()));
                                        puntosDeRuta.add(punto);
                                        String strlatlog = indi.getJSONObject("startPoint").get("lat").toString()
                                                + "," +
                                                indi.getJSONObject("startPoint").get("lng").toString();

                                        Log.d("GIVO", "se ejecuto: " +  strlatlog );

                                    }
                                    Polyline line = new Polyline();   //see note below!
                                    line.setPoints(puntosDeRuta);
                                    map.getOverlayManager().add(line);
                                    //listaPuntos.add(line);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("GIVO", "Error "
                                );

                            }
                        }
                );
        queue.add(requestMapRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}