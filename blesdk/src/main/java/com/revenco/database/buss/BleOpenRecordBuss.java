package com.revenco.database.buss;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.bean.BleOpenRecordBean;
import com.revenco.database.helper.SqlStatementHelper;
import com.revenco.database.helper.SqliteHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 15:18.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class BleOpenRecordBuss {
    public static final String tableName = "BleOpenRecord";

    public synchronized static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlStatementHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlStatementHelper.ID_PRIVATE_KEY)
                .append("userId  TEXT").append(",")
                .append("deviceId TEXT").append(",")
                .append("deviceAddress TEXT").append(",")
                .append("RSSI INTEGER").append(",")
                .append("scanTime REAL").append(",")
                .append("certificateIndex INTEGER").append(",")
                .append("openResult TEXT").append(",")
                .append("reason TEXT").append(",")
                .append("openConsumeTime REAL").append(",")
                .append("currentDate TEXT").append(",")
                .append("tag TEXT").append(")");
        db.execSQL(sb.toString());
    }

    /**
     * @param context
     * @param bean
     * @return 返回 最新插入数据的自增长主键ID
     */
    public synchronized static int insertRow(Context context, BleOpenRecordBean bean) {
        int ID = -1;
        if (bean == null)
            return ID;
        SQLiteDatabase db = SqliteHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO  " + tableName + " (userId,deviceId, deviceAddress, RSSI,scanTime,certificateIndex,openResult,reason,openConsumeTime,currentDate,tag)  VALUES(?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
                    bean.userId, bean.deviceId, bean.deviceAddress, bean.RSSI, bean.scanTime, bean.certificateIndex, bean.openResult, bean.reason, bean.openConsumeTime, bean.currentDate, bean.tag
            });
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + tableName, null);
            if (cursor.moveToFirst()) {
                ID = cursor.getInt(0);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return ID;
    }
}
