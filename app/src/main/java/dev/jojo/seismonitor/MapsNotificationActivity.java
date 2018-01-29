package dev.jojo.seismonitor;

import android.app.AlertDialog;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import dev.jojo.seismonitor.utils.NumParser;

public class MapsNotificationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Double Lat;
    private Double Long;

    private Double Lat2;
    private Double Long2;

    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_notification);

        h = new Handler(this.getMainLooper());

        if(this.getIntent().getBooleanExtra("app_notif",false)){

            String intensity = this.getIntent().getStringExtra("app_intensity");
            String eta = this.getIntent().getStringExtra("app_eta");


            AlertDialog.Builder ab = new AlertDialog.Builder(MapsNotificationActivity.this);
            ab.setTitle("Earthquake Alert");

            ab.setMessage("The earthquake shaking will be felt "
                    + intensity + " in " + eta + " seconds.");

            final AlertDialog ad = ab.create();

            ad.show();

            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ad.dismiss();
                }
            }, NumParser.parseDouble(eta).longValue() * 1000);


        }

        this.Lat = this.getIntent().getDoubleExtra("app_lat",0d);
        this.Long = this.getIntent().getDoubleExtra("app_long",0d);

        this.Lat2 = this.getIntent().getDoubleExtra("dev_lat",0d);
        this.Long2 = this.getIntent().getDoubleExtra("dev_long",0d);


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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(this.Lat, this.Long);
        LatLng sydney2 = new LatLng(this.Lat2,this.Long2);

        CircleOptions circleOptions = new CircleOptions()
                .center(sydney)
                .radius(70000);

        mMap.addMarker(new MarkerOptions().position(sydney).title("Earthquake Location"));
        mMap.addCircle(circleOptions);
        mMap.addMarker(new MarkerOptions().position(sydney2).title("YOU"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,8.0f));
    }
}
