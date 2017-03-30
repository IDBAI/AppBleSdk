package com.revenco.database.buss;

import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.helper.SqlStatementHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 15:18.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class BleOpenRecordBuss {
    static final String tableName = "BleOpenRecord";

    public static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlStatementHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlStatementHelper.ID_PRIVATE_KEY)
                .append("userId  TEXT").append(",")
                .append("deviceId TEXT").append(",")
                .append("deviceAddress TEXT").append(",")
                .append("RSSI INTEGER").append(",")
                .append("scanTime INTEGER").append(",")
                .append("CertificateIndex INTEGER").append(",")
                .append("openResult TEXT").append(",")
                .append("reason TEXT").append(",")
                .append("openConsumeTime INTEGER").append(")");
        db.execSQL(sb.toString());
    }
}
