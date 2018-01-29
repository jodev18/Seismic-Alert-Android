package dev.jojo.seismonitor.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import dev.jojo.seismonitor.SeismoSplash;
import dev.jojo.seismonitor.services.EQNotificationHandler;

import static android.app.Service.START_NOT_STICKY;

/**
 * Created by myxroft on 28/01/2018.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("startuptest", "StartUpBootReceiver BOOT_COMPLETED");

            Intent startSeismo = new Intent(context, EQNotificationHandler.class);
            context.startService(startSeismo);
        }
    }


}
