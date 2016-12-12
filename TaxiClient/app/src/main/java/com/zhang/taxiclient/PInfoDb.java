package com.zhang.taxiclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mr.Z on 2016/5/16 0016.
 */
public class PInfoDb extends SQLiteOpenHelper {

    public PInfoDb(Context context) {
        super(context, "pinfo", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pinfo(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT DEFAULT \"\"," +             //   1
                "dis TEXT DEFAULT \"\"," +              //   2
                "des TEXT DEFAULT \"\"," +              //   3
                "tel TEXT DEFAULT \"\"," +              //   4
                "hid TEXT DEFAULT \"\")");              //   5
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
