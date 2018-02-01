package dev.jojo.seismonitor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import dev.jojo.seismonitor.services.EQNotificationHandler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SeismoSplash extends AppCompatActivity {

    private Handler h;

    private Disposable netDisposable;

    private AlertDialog alertInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_seismo_splash);

        //startingPoint();
//        Dexter.withActivity(activity)
//                .withPermission(permission)
//                .withListener(listener)
//                .check();


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SeismoSplash.this);

        Boolean permission = sp.getBoolean("has_permission",false);

        if(permission){

            Log.d("STARTING SERVICE","Starting service...");
            Intent startSeismo = new Intent().setClass(getApplicationContext(), EQNotificationHandler.class);
            startService(startSeismo);
        }
        else {
            Dexter.withActivity(SeismoSplash.this)
                    .withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {

                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SeismoSplash.this);

                            SharedPreferences.Editor e = sp.edit();

                            e.putBoolean("has_permission",true);
                            e.commit();
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                    }).check();
        }
        startingPoint();


    }

    private void startingPoint(){
        h = new Handler(this.getMainLooper());

        initNetworkListener();

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String currIP = sp.getString("app_ip",null);

        if(currIP == null){

            final SharedPreferences.Editor e = sp.edit();

            AlertDialog.Builder ab = new AlertDialog.Builder(SeismoSplash.this);
            ab.setTitle("Enter server address");

            View v = this.getLayoutInflater().inflate(R.layout.layout_input,null);

            final TextView tvInp = (TextView)v.findViewById(R.id.etInput);

            ab.setView(v);

            ab.setPositiveButton("Save",null);
            ab.setNegativeButton("Cancel",null);

            AlertDialog ad = ab.create();

            ad.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {

                    Button saveIP = ((AlertDialog) dialog)
                            .getButton(AlertDialog.BUTTON_POSITIVE);
                    Button cancelIP = ((AlertDialog) dialog)
                            .getButton(AlertDialog.BUTTON_NEGATIVE);

                    saveIP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String ipInput = tvInp.getText().toString();

                            if(ipInput.length() > 0){

                                if(ipInput.contains("http://")){

                                    e.putString("app_ip",ipInput);

                                    if(e.commit()){
                                        Toast.makeText(SeismoSplash.this, "Saved!", Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent().setClass(getApplicationContext(),MainMap.class));
                                        finish();
                                        Intent startSeismo = new Intent(getApplicationContext(), EQNotificationHandler.class);
                                        startService(startSeismo);
                                        dialog.dismiss();
                                    }
                                    else{
                                        Toast.makeText(SeismoSplash.this, "Failed to save.", Toast.LENGTH_SHORT).show();
                                    }

                                }
                                else{
                                    Toast.makeText(SeismoSplash.this,
                                            "Please enter a valid web address.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(SeismoSplash.this,
                                        "Please enter an IP address.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    cancelIP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder ab = new AlertDialog.Builder(SeismoSplash.this);

                            ab.setTitle("Cancel");
                            ab.setMessage("Are you sure you want cancel? " +
                                    "You may not be able to use this application without it.");

                            ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog2, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });

                            ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            ab.create().show();

                        }
                    });

                }
            });

            ad.setCancelable(false);
            ad.show();


            //ab.create().show();

        }
        else{
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent().setClass(getApplicationContext(),MainMap.class));
                    finish();
                }
            },3000);
        }
    }

    private void initNetworkListener(){

        netDisposable = ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Connectivity>() {
                    @Override public void accept(final Connectivity connectivity) {
                        // do something with connectivity
                        // you can call connectivity.getState();
                        // connectivity.getType(); or connectivity.toString();
                        if(connectivity.getState().equals(NetworkInfo.State.CONNECTED)){
                            if(alertInfoDialog != null){
                                if(alertInfoDialog.isShowing()){
                                    alertInfoDialog.dismiss();
                                }
                            }
                            Toast.makeText(getApplicationContext(),
                                    "Device connected.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder dc = new AlertDialog.Builder(SeismoSplash.this);
                            dc.setTitle("Device offline");
                            dc.setMessage("The device is currently offline. Service is unavailable.");
                            dc.setCancelable(false);
                            alertInfoDialog = dc.create();
                            alertInfoDialog.show();
                        }
                    }
                });
    }

    @Override
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }
        super.onDestroy();
    }
}
