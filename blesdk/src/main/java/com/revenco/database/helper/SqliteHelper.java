package com.revenco.database.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.revenco.database.buss.BleOpenRecordBuss;
import com.revenco.database.buss.CertificateBuss;
import com.revenco.database.buss.StatisticalBuss;
import com.revenco.database.buss.UserBuss;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 14:44.</p>
 * <p>CLASS DESCRIBE :ble 存储开门证书，开门记录等等。</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class SqliteHelper extends SQLiteOpenHelper {
    private static final int VERSION_CODE = 1;
    private static final String DBNAME = "ble.db";
    private static final String TAG = "SqliteHelper";

    public SqliteHelper(Context context) {
        this(context, DBNAME, null, VERSION_CODE);
    }

    public SqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        BleOpenRecordBuss.createTable(db);
        CertificateBuss.createTable(db);
        StatisticalBuss.createTable(db);
        UserBuss.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //1  -> 2
        //1 -> 3
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            switch (version) {//版本号，依次按版本号处理
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    }
}
