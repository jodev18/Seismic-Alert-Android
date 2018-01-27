package dev.jojo.seismonitor.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import dev.jojo.seismonitor.objects.NotificationQuake;

/**
 * Created by myxroft on 27/01/2018.
 */

public class QuakeNotifListAdapter extends BaseAdapter {

    private List<NotificationQuake> qList;
    private Activity act;

    public QuakeNotifListAdapter(List<NotificationQuake> nQuake, Activity cActivity){
        this.qList = nQuake;
        this.act = cActivity;
    }

    @Override
    public int getCount() {
        return qList.size();
    }

    @Override
    public NotificationQuake getItem(int position) {
        return this.qList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){

        }

        return convertView;
    }
}
