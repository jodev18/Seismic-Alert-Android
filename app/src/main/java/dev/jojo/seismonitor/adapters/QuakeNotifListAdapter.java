package dev.jojo.seismonitor.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import dev.jojo.seismonitor.R;
import dev.jojo.seismonitor.objects.NotificationQuake;
import dev.jojo.seismonitor.objects.QuakeInfo;

/**
 * Created by myxroft on 27/01/2018.
 */

public class QuakeNotifListAdapter extends BaseAdapter {

    private List<QuakeInfo> qList;
    private Activity act;

    public QuakeNotifListAdapter(List<QuakeInfo> nQuake, Activity cActivity){
        this.qList = nQuake;
        this.act = cActivity;
    }

    @Override
    public int getCount() {
        return qList.size();
    }

    @Override
    public QuakeInfo getItem(int position) {
        return this.qList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View vv, ViewGroup parent) {

        if(vv == null){

            vv = this.act.getLayoutInflater()
                    .inflate(R.layout.list_item_quake,null);

        }
        QuakeInfo qInf = qList.get(position);

        TextView date = (TextView)vv.findViewById(R.id.tvTimeStamp);
        TextView magn = (TextView)vv.findViewById(R.id.tvMagnitude);
        TextView locn = (TextView)vv.findViewById(R.id.tvLocation);

        date.setText(qInf.QUAKE_TIMESTAMP);
        magn.setText(qInf.QUAKE_MAGNITUDE);
        locn.setText(qInf.QUAKE_LAT + " , " + qInf.QUAKE_LONG);

        return vv;
    }
}
