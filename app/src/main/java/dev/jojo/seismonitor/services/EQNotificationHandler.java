package dev.jojo.seismonitor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dev.jojo.seismonitor.Dashboard;
import dev.jojo.seismonitor.MapsNotificationActivity;
import dev.jojo.seismonitor.R;
import dev.jojo.seismonitor.database.HistoryCollector;
import dev.jojo.seismonitor.database.HistoryFetcher;
import dev.jojo.seismonitor.network.HTTPManager;
import dev.jojo.seismonitor.objects.HTTPRequestObject;
import dev.jojo.seismonitor.objects.QuakeInfo;
import dev.jojo.seismonitor.utils.EarthquakeCalculator;
import dev.jojo.seismonitor.utils.Haversine;
import dev.jojo.seismonitor.utils.NumParser;

/**
 * Created by myxroft on 26/01/2018.
 */

public class EQNotificationHandler extends Service implements LocationListener {

    private NotificationManager mNM;

    private int NOTIFICATION = R.string.local_service_started;

    private Thread mThread;

    private String ROOT_URL;

    private LocationManager mLocationManager;
    private Location loc;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onCreate(){

        Log.d("SERVICE","SERVICE INVOKED");

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
                this, null);

        ROOT_URL = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).getString("app_ip","");

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        //showNotifString("Testing dynamic string output.");

        initMonitor();

    }



    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(getApplicationContext(),
                "Quake Monitoring Stopped.", Toast.LENGTH_SHORT).show();

        if(mThread != null){
            if(mThread.isAlive()){
                mThread.stop();
            }
        }
    }

    private void showNotification() {

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Dashboard.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_equake_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("QuakeResp")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private void initMonitor(){


        mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d("MONITOR","Thread started.");

                for(;;){

                    HistoryFetcher hf = new HistoryFetcher(getApplicationContext());

                    String latestId = hf.getLatestID();

                    Log.d("LATEST_ID",latestId);

                    hf.closeDB();

                    List<HTTPRequestObject> reqObj = new ArrayList<>();

                    HTTPRequestObject filler = new HTTPRequestObject();
                    filler.PARAM = "latest_id";
                    filler.VALUE = latestId;

                    reqObj.add(filler);

                    HTTPManager httpman = new HTTPManager(ROOT_URL +
                            "/quakemonitor/read_data.php?latest_id=" + latestId,reqObj);

                    final String received = httpman.performRequest();

                    Log.d("received",received);

                            try {

                                JSONArray jAr = new JSONArray(received);

                                int len = jAr.length();

                                final List<QuakeInfo> quakeInfos = new ArrayList<>();

                                for(int i=0;i<len;i++){

                                    QuakeInfo qInf = new QuakeInfo();

                                    JSONObject jAr2 = jAr.getJSONObject(i);

                                    qInf.QUAKE_LAT =  jAr2.getString("Latitude");
                                    qInf.QUAKE_LONG = jAr2.getString("Longitude");

                                    Double mg1 = NumParser
                                            .parseDouble(jAr2.getString("Magsens1"));
                                    Double mg2 = NumParser.
                                            parseDouble(jAr2.getString("Magsens2"));

                                    String mAve = Double.valueOf((mg1 + mg2) / 2).toString();

                                    qInf.QUAKE_MAGNITUDE = mAve;

                                    Calendar c = Calendar.getInstance();
                                    //System.out.println("Current time => " + c.getTime());

                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
                                    String formattedDate = df.format(c.getTime());
                                    qInf.QUAKE_TIMESTAMP = formattedDate;

                                    quakeInfos.add(qInf);
                                }

                                HistoryCollector hc = new HistoryCollector(getApplicationContext());

                                int qSize = quakeInfos.size();

                                for(int i=0;i<qSize;i++){
                                    long stat = hc.saveData(quakeInfos.get(i));

                                    if(stat > 0){
                                        Log.d("SAVE_SERVICE","Service saved.");
                                    }
                                    else{
                                        Log.d("SAVE_SERVICE", "Failed");
                                    }
                                }

                                hc.closeDB();

                                if(loc != null){
                                    if(quakeInfos.size() > 0){
                                        Double dist = Haversine.distance(loc.getLatitude(),loc.getLongitude(),
                                                NumParser.parseDouble(quakeInfos.get(0).QUAKE_LAT),NumParser.parseDouble(quakeInfos.get(0).QUAKE_LONG));

                                        Log.d("DISTANCE_FROM_CAPTURED", dist.toString());

                                        EarthquakeCalculator ec =
                                                new EarthquakeCalculator(NumParser
                                                        .parseDouble(quakeInfos.get(0).QUAKE_MAGNITUDE),dist);

                                        Log.d("INTENSITY", ec.getIntensity());
                                        Log.d("ETA",ec.getETA().toString());

                                        //Intent showAlert = new Intent(getApplicationContext(),)

                                        if(!ec.getIntensity().equals(ec.INTENSITY_UNAFFECTED)){

                                            showNotifString("Earthquake shaking will be felt "
                                                    + ec.getIntensity() + " in "
                                                    + ec.getETA().toString() + " seconds.");

                                            countdown(ec.getETA().intValue(),ec.getIntensity());


                                            Intent startMapNotif = new Intent(getApplicationContext(), MapsNotificationActivity.class);

                                            startMapNotif.putExtra("app_notif",true);
                                            startMapNotif.putExtra("app_intensity", ec.getIntensity());
                                            startMapNotif.putExtra("app_eta", ec.getETA().toString());
                                            startMapNotif.putExtra("dev_lat",loc.getLatitude());
                                            startMapNotif.putExtra("dev_long",loc.getLongitude());
                                            startMapNotif.putExtra("app_lat",NumParser
                                                    .parseDouble(quakeInfos.get(0).QUAKE_LAT));
                                            startMapNotif.putExtra("app_long",
                                                    NumParser.parseDouble(quakeInfos.get(0).QUAKE_LONG));

                                            startActivity(startMapNotif);
                                        }
                                        else{
                                            showNotifString("An earthquake has occured.");
                                        }
                                    }
                                }


                            } catch (JSONException e) {
                                //Toast.makeText(getApplicationContext(), "Service--Nothing received", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                    try{
                        Log.d("SLEEP","Thread Sleeping");
                        Thread.sleep(5000);
                    }
                    catch (InterruptedException iex){
                        Log.e("INTERRUPTED_SLEEP", "Who dared to wake me up????");
                    }
                }

            }
        });

        if(ROOT_URL.length() > 0){
            mThread.start();
        }

    }

    private void countdown(final int countdownnotif,final String intensity){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=countdownnotif;i>0;i--){
                    showNotifString("Earthquake shaking will be felt "
                            + intensity + " in "
                            + Integer.valueOf(i) + " seconds.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.d("CDOWN","Earthquake shaking will be felt "
                            + intensity + " in "
                            + Integer.valueOf(i) + " seconds.");
                }

                showNotification();
            }
        }).start();
    }

    private void showNotifString(String notifStr){

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        //Intent viewDetailedMap

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Dashboard.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_equake_launcher)  // the status icon
                .setTicker(notifStr)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("QuakeResp")  // the label of the entry
                .setContentText(notifStr)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("LOC",location.toString());

        this.loc = location;


        initMonitor();
    }

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
