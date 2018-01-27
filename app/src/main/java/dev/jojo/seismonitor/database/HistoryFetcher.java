package dev.jojo.seismonitor.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import dev.jojo.seismonitor.objects.QuakeInfo;

/**
 * Created by myxroft on 27/01/2018.
 *
 * This will handling fetching a data from database.
 */

public class HistoryFetcher extends HistoryDB {

    private SQLiteDatabase sq;
    private Cursor c;

    public HistoryFetcher(Context ct){
        super(ct);

        this.sq = getReadableDatabase();
    }

    /**
     * Return null if there's no notification stored
     * @return
     */
    public List<QuakeInfo> getAllStoredNotifications(){

        this.c = this.sq.rawQuery("SELECT * FROM "
                + HistoryData.TABLE_NAME,null);

        if(this.c.getCount() > 0){

            List<QuakeInfo> notificationQuakes = new ArrayList<>();

            while(this.c.moveToNext()){

                QuakeInfo qInfo = new QuakeInfo();

                qInfo.QUAKE_DEV_LAT = c.getString(
                        c.getColumnIndex(HistoryData.DEVICE_LOCATION_LAT));
                qInfo.QUAKE_DEV_LONG = c.getString(
                        c.getColumnIndex(HistoryData.DEVICE_LOCATION_LONG));
                qInfo.QUAKE_LAT = c.getString(c.getColumnIndex(HistoryData.LATITUDE));
                qInfo.QUAKE_LONG = c.getString(c.getColumnIndex(HistoryData.LONGITUDE));
                qInfo.QUAKE_MAGNITUDE = c.getString(c.getColumnIndex(HistoryData.MAGNITUDE));
                qInfo.QUAKE_TIMESTAMP = c.getString(c.getColumnIndex(HistoryData.TIMESTAMP));

                notificationQuakes.add(qInfo);
            }

            return notificationQuakes;

        }
        else{
            return null;
        }
    }

    public void clearDB(){

        if(this.sq != null){
            if(this.sq.isOpen()){
                this.sq.close();
            }
        }

        if(this.c != null){
            this.c.close();
        }
    }
}
