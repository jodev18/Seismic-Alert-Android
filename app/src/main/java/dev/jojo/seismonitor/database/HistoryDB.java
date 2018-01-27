package dev.jojo.seismonitor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by myxroft on 26/01/2018.
 */

public class HistoryDB extends SQLiteOpenHelper {

    public HistoryDB(Context ct){
        super(ct,"hist.db",null,1);
    }

    protected class HistoryData{

        public static final String TABLE_NAME = "tbl_data";

        public static final String ID = "data_id";

        public static final String LATITUDE = "data_lat";

        public static final String LONGITUDE = "data_long";

        public static final String TIMESTAMP = "data_timestamp";

        public static final String MAGNITUDE = "data_magnitude";

        public static final String DEVICE_LOCATION_LAT = "data_device_loc_lat";

        public static final String DEVICE_LOCATION_LONG = "data_device_loc_long";

        public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME +
                "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LATITUDE + " TEXT,"
                + LONGITUDE + " TEXT,"
                + TIMESTAMP + " TEXT,"
                + MAGNITUDE + " TEXT,"
                + DEVICE_LOCATION_LAT + " TEXT,"
                + DEVICE_LOCATION_LONG + " TEXT);";


    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryData.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
