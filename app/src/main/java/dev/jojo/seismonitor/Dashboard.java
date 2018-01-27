package dev.jojo.seismonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        fetchNotification();
    }

    private void fetchNotification(){

    }

    @Override
    public void onResume(){
        super.onResume();
    }
}
