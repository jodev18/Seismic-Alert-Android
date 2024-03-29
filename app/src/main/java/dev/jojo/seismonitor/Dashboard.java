package dev.jojo.seismonitor;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.common.data.DataHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dev.jojo.seismonitor.adapters.QuakeNotifListAdapter;
import dev.jojo.seismonitor.database.HistoryCollector;
import dev.jojo.seismonitor.database.HistoryFetcher;
import dev.jojo.seismonitor.network.HTTPManager;
import dev.jojo.seismonitor.objects.HTTPRequestObject;
import dev.jojo.seismonitor.objects.QuakeInfo;
import dev.jojo.seismonitor.utils.NumParser;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Dashboard extends AppCompatActivity {

    private Handler h;

    private Disposable netDisposable;

    private AlertDialog alertInfoDialog;

    private String ROOT_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        h = new Handler(this.getMainLooper());

        ROOT_URL = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext()).getString("app_ip",null);

        Toast.makeText(this, "ROOT IP: "+ ROOT_URL, Toast.LENGTH_SHORT).show();

        HistoryFetcher hf = new HistoryFetcher(Dashboard.this);
        List<QuakeInfo> data = hf.getAllStoredNotifications();

        if(data == null){
            fetchOnline();
        }
        else {
            fetchNotification(data);
        }

        initNetworkListener();

    }

    private void fetchNotification(final List<QuakeInfo> quakeInfos){

        if(quakeInfos != null){

            TextView tvNotif = (TextView)findViewById(R.id.tvListEmpty);
            tvNotif.setVisibility(TextView.GONE);
            ImageView imNotif = (ImageView)findViewById(R.id.imgStatIcon);
            imNotif.setVisibility(ImageView.GONE);

            ListView lNotifList = (ListView)findViewById(R.id.lvNotifList);

            QuakeNotifListAdapter qAdapt = new QuakeNotifListAdapter(quakeInfos,Dashboard.this);

            lNotifList.setAdapter(qAdapt);
            lNotifList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent viewMap = new Intent(getApplicationContext(),MapsNotificationActivity.class);

                    viewMap.putExtra("app_lat",Double.parseDouble(quakeInfos.get(position).QUAKE_LAT));
                    viewMap.putExtra("app_long",Double.parseDouble(quakeInfos.get(position).QUAKE_LONG));

                    startActivity(viewMap);

                }
            });
        }




    }

    private void fetchOnline(){

       new Thread(new Runnable() {
           @Override
           public void run() {
               List<HTTPRequestObject> reqObj = new ArrayList<>();

               HTTPRequestObject filler = new HTTPRequestObject();
               filler.PARAM = "";
               filler.VALUE = "";

               reqObj.add(filler);

               HTTPManager httpman = new HTTPManager(ROOT_URL +
                       "/quakemonitor/read_data.php",reqObj);

               final String received = httpman.performRequest();

               h.post(new Runnable() {
                   @Override
                   public void run() {
                       //Toast.makeText(Dashboard.this,
                         //      "Fetched: " + received, Toast.LENGTH_SHORT).show();

                       try {

                           JSONArray jAr = new JSONArray(received);

                           int len = jAr.length();

                           final List<QuakeInfo> quakeInfos = new ArrayList<>();

                           for(int i=0;i<len;i++){

                               QuakeInfo qInf = new QuakeInfo();

                               JSONObject jAr2 = jAr.getJSONObject(i);

                               qInf.QUAKE_ID = jAr2.getString("id");
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

                           if(quakeInfos.size() > 0){

                               TextView tvNotif = (TextView)findViewById(R.id.tvListEmpty);
                               tvNotif.setVisibility(TextView.GONE);
                               ImageView imNotif = (ImageView)findViewById(R.id.imgStatIcon);
                               imNotif.setVisibility(ImageView.GONE);
                           }

                           HistoryCollector hc = new HistoryCollector(Dashboard.this);

                           int qsize = quakeInfos.size();

                           for(int i=0;i<qsize;i++){
                               Log.d("SAVING_DB",quakeInfos.get(i).QUAKE_ID);
                               long stat = hc.saveData(quakeInfos.get(i));

                               if(stat > 0){
                                   Log.d("DB_STAT","Saved to database!");
                               }
                               else{
                                    Log.d("DB_STAT","Saving failed!");
                               }
                           }

                           hc.closeDB();

                           QuakeNotifListAdapter qAdapt = new QuakeNotifListAdapter(quakeInfos,Dashboard.this);
                           ListView lNotifList = (ListView)findViewById(R.id.lvNotifList);

                           lNotifList.setAdapter(qAdapt);
                           lNotifList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                               @Override
                               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent viewMap = new Intent(getApplicationContext(),MapsNotificationActivity.class);

                                    viewMap.putExtra("app_lat",Double.parseDouble(quakeInfos.get(position).QUAKE_LAT));
                                    viewMap.putExtra("app_long",Double.parseDouble(quakeInfos.get(position).QUAKE_LONG));

                                    startActivity(viewMap);

                               }
                           });


                       } catch (JSONException e) {
                           Toast.makeText(Dashboard.this, "Error handling data", Toast.LENGTH_SHORT).show();
                           e.printStackTrace();
                       }
                   }
               });
           }
       }).start();
    }

    private void initNetworkListener(){

        netDisposable = ReactiveNetwork.observeNetworkConnectivity(Dashboard.this)
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
                            AlertDialog.Builder dc = new AlertDialog.Builder(Dashboard.this);
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
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }
        super.onDestroy();
    }
}
