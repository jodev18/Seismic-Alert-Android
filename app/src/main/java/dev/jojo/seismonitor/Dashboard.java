package dev.jojo.seismonitor;

import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        h = new Handler(this.getMainLooper());

        fetchOnline();
        initNetworkListener();
        fetchNotification();
    }

    private void fetchNotification(){

        ListView lNotifList = (ListView)findViewById(R.id.lvNotifList);




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

               HTTPManager httpman = new HTTPManager("http://192.168.254.100/" +
                       "quakemonitor/read_data.php",reqObj);

               final String received = httpman.performRequest();

               h.post(new Runnable() {
                   @Override
                   public void run() {
                       //Toast.makeText(Dashboard.this,
                         //      "Fetched: " + received, Toast.LENGTH_SHORT).show();

                       try {
                           JSONArray jAr = new JSONArray(received);

                           int len = jAr.length();

                           List<QuakeInfo> quakeInfos = new ArrayList<>();

                           for(int i=0;i<len;i++){
                               QuakeInfo qInf = new QuakeInfo();

                               JSONObject jAr2 = jAr.getJSONObject(i);

                               qInf.QUAKE_LAT =  jAr2.getString("Latitude");
                               qInf.QUAKE_LONG = jAr2.getString("Longitude");

                               Double mg1 = NumParser
                                       .parseDouble(jAr2.getString("Magsens1"));

                           }
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
}
