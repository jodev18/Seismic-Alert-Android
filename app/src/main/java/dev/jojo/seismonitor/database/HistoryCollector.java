package dev.jojo.seismonitor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import dev.jojo.seismonitor.objects.QuakeInfo;

/**
 * Created by myxroft on 27/01/2018.
 */

public class HistoryCollector extends HistoryDB {

    private ContentValues cv;
    private SQLiteDatabase sq;

    public HistoryCollector(Context ct){
        super(ct);

        this.sq = getWritableDatabase();
        this.cv = new ContentValues();
    }

    public void closeDB(){

        if(this.sq != null){
            if(this.sq.isOpen()){
                this.sq.close();
            }
        }

        if(this.cv != null){
            this.cv.clear();
            this.cv = null;
        }
    }

    public long saveData(QuakeInfo quakeInfo){

        this.cv.clear();

        this.cv.put(HistoryData.ID,quakeInfo.QUAKE_ID);
        this.cv.put(HistoryData.DEVICE_LOCATION_LAT, quakeInfo.QUAKE_DEV_LAT);
        this.cv.put(HistoryData.DEVICE_LOCATION_LONG, quakeInfo.QUAKE_DEV_LONG);
        this.cv.put(HistoryData.LATITUDE, quakeInfo.QUAKE_LAT);
        this.cv.put(HistoryData.LONGITUDE, quakeInfo.QUAKE_LONG);
        this.cv.put(HistoryData.MAGNITUDE, quakeInfo.QUAKE_MAGNITUDE);
        this.cv.put(HistoryData.TIMESTAMP, quakeInfo.QUAKE_TIMESTAMP);

        long res = this.sq.insert(HistoryData.TABLE_NAME,HistoryData.ID,this.cv);

        return res;
    }

}
