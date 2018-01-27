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
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
