package dev.jojo.seismonitor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.jojo.seismonitor.network.HTTPManager;
import dev.jojo.seismonitor.objects.HTTPRequestObject;
import dev.jojo.seismonitor.utils.EarthquakeCalculator;
import dev.jojo.seismonitor.utils.Haversine;
import dev.jojo.seismonitor.utils.NumParser;

public class MainMap extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private String ROOT_URL;

    private Location loc;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        sp = PreferenceManager.getDefaultSharedPreferences(MainMap.this);

        ROOT_URL = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getString("app_ip","");

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                this, null);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        mMap.setMyLocationEnabled(true);

        loader.start();

        // Add a marker in Sydney and move the camera
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currLoc = new LatLng(location.getLatitude(),location.getLongitude());
        LatLng currLoc2 = new LatLng(location.getLatitude()+ 10,location.getLongitude() + 10);
        mMap.addMarker(new MarkerOptions().position(currLoc).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc));

        loc = location;
//        // Instantiating the class PolylineOptions to plot polyline in the map
//        PolylineOptions polylineOptions = new PolylineOptions();
//
//        // Setting the color of the polyline
//        polylineOptions.color(Color.RED);
//
//        // Setting the width of the polyline
//        polylineOptions.width(3);
//
//        polylineOptions.add(currLoc);
//        polylineOptions.add(currLoc2);
//
//        mMap.addPolyline(polylineOptions);

    }

    List<LatLng> points = new ArrayList<>();

    private Thread loader = new Thread(new Runnable() {
        @Override
        public void run() {

            while(true){

                try{

                    Log.d("Initialize_wait","began loader");

                    List<HTTPRequestObject> reqObj = new ArrayList<>();

                    HTTPRequestObject filler = new HTTPRequestObject();
                    filler.PARAM = "";
                    filler.VALUE = "";

                    reqObj.add(filler);

                    HTTPManager httpman = new HTTPManager(ROOT_URL +
                            "/quakemonitor/read_data_1.php",reqObj);

                    final String received = httpman.performRequest();

                    Log.d("REC_DATA_1",received);

                    JSONArray jAr = new JSONArray(received);

                    if(jAr.length() == 1){

                        if(loc != null){

                            JSONObject jsonObject = jAr.getJSONObject(0);

                            Double qLat = NumParser.parseDouble(jsonObject.getString("Latitude"));
                            Double qLong = NumParser.parseDouble(jsonObject.getString("Longitude"));

                            String id = jsonObject.getString("id");

                            String stored = sp.getString("latest_id","");

                            if(stored.length() == 0 ){
                                SharedPreferences.Editor e = sp.edit();

                                e.putString("latest_id",id);
                                e.commit();

                                Double mg1 = NumParser
                                        .parseDouble(jsonObject.getString("Magsens1"));
                                Double mg2 = NumParser.
                                        parseDouble(jsonObject.getString("Magsens2"));

                                Double aveMg = (mg1 + mg2) / 2;

                                final Double dist = Haversine.distance(loc.getLatitude(),loc.getLongitude(),
                                        qLat,qLong);

                                Log.d("DISTANCE_FROM_CAPTURED", dist.toString());

                                final EarthquakeCalculator ec =
                                        new EarthquakeCalculator(aveMg,dist);

                                Log.d("INTENSITY", ec.getIntensity());
                                Log.d("ETA",ec.getETA().toString());

                                final LatLng currLoc = new LatLng(loc.getLatitude(),loc.getLongitude());
                                final LatLng currLoc2 = new LatLng(qLat,qLong);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.clear();
                                        mMap.addMarker(new MarkerOptions().position(currLoc).title("Current Location"));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc));

                                        mMap.addMarker(new MarkerOptions().position(currLoc2).title("Earthquake"));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc2));

                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLoc2,8.0f));
                                    }
                                });

                                final PolylineOptions polylineOptions = new PolylineOptions();
//
                                // Setting the color of the polyline
                                polylineOptions.color(Color.RED);

                                // Setting the width of the polyline
                                polylineOptions.width(3);

                                polylineOptions.add(currLoc);
                                polylineOptions.add(currLoc2);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMap.addPolyline(polylineOptions);

                                        AlertDialog.Builder ab = new AlertDialog.Builder(MainMap.this);

                                        View v = MainMap.this.getLayoutInflater().inflate(R.layout.layout_countdown,null);

                                        final TextView cDown = (TextView)v.findViewById(R.id.tvCountdown);

                                        TextView dets = (TextView)v.findViewById(R.id.tvDetails);

                                        dets.setText("The earthquake will be felt " + ec.getIntensity() + "." + "\nDistance from detected wave: " + dist.longValue() + " km");

                                        ab.setView(v);

                                        final AlertDialog ad = ab.create();

                                        ad.show();

                                        cDown.setText(Integer.valueOf(ec.getETA().intValue()).toString());

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                for(int i=ec.getETA().intValue();i>=0;i--){
                                                    final Integer ic = i;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            cDown.setText(Integer.valueOf(ic).toString());
                                                        }
                                                    });
                                                    try {
                                                        Thread.sleep(1000);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                //1.5s before dismiss
                                                try {
                                                    Thread.sleep(1500);
                                                    ad.dismiss();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();

                                    }
                                });
                            }
                            else{
                                if(!stored.equals(id)){
                                    SharedPreferences.Editor e = sp.edit();

                                    e.putString("latest_id",id);
                                    e.commit();

                                    Double mg1 = NumParser
                                            .parseDouble(jsonObject.getString("Magsens1"));
                                    Double mg2 = NumParser.
                                            parseDouble(jsonObject.getString("Magsens2"));

                                    Double aveMg = (mg1 + mg2) / 2;

                                    final Double dist = Haversine.distance(loc.getLatitude(),loc.getLongitude(),
                                            qLat,qLong);

                                    Log.d("DISTANCE_FROM_CAPTURED", dist.toString());

                                    final EarthquakeCalculator ec =
                                            new EarthquakeCalculator(aveMg,dist);

                                    Log.d("INTENSITY", ec.getIntensity());
                                    Log.d("ETA",ec.getETA().toString());

                                    final LatLng currLoc = new LatLng(loc.getLatitude(),loc.getLongitude());
                                    final LatLng currLoc2 = new LatLng(qLat,qLong);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMap.clear();
                                            mMap.addMarker(new MarkerOptions().position(currLoc).title("Current Location"));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc));

                                            mMap.addMarker(new MarkerOptions().position(currLoc2).title("Earthquake"));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc2));

                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLoc2,8.0f));
                                        }
                                    });

                                    final PolylineOptions polylineOptions = new PolylineOptions();
//
                                    // Setting the color of the polyline
                                    polylineOptions.color(Color.RED);

                                    // Setting the width of the polyline
                                    polylineOptions.width(3);

                                    polylineOptions.add(currLoc);
                                    polylineOptions.add(currLoc2);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMap.addPolyline(polylineOptions);

                                            AlertDialog.Builder ab = new AlertDialog.Builder(MainMap.this);

                                            View v = MainMap.this.getLayoutInflater().inflate(R.layout.layout_countdown,null);

                                            final TextView cDown = (TextView)v.findViewById(R.id.tvCountdown);

                                            TextView dets = (TextView)v.findViewById(R.id.tvDetails);

                                            dets.setText("The earthquake will be felt " + ec.getIntensity() + "." + "\nDistance from detected wave: " + dist.longValue() + " km");

                                            ab.setView(v);

                                            final AlertDialog ad = ab.create();

                                            ad.show();

                                            cDown.setText(Integer.valueOf(ec.getETA().intValue()).toString());

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    for(int i=ec.getETA().intValue();i>=0;i--){
                                                        final Integer ic = i;
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                cDown.setText(Integer.valueOf(ic).toString());
                                                            }
                                                        });
                                                        try {
                                                            Thread.sleep(1000);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    //1.5s before dismiss
                                                    try {
                                                        Thread.sleep(1500);
                                                        ad.dismiss();
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).start();

                                        }
                                    });
                                }
                                else{

                                }
                            }


                        }

                    }

                    Thread.sleep(20000);
                }
                catch (JSONException jEx){
                    jEx.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    });

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
