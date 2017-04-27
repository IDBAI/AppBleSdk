package com.revenco.database.buss;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.bean.StatisticalBean;
import com.revenco.database.helper.SqlStatementHelper;
import com.revenco.database.helper.SqliteHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 14:52.</p>
 * <p>CLASS DESCRIBE :证书</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class StatisticalBuss {
    public static String tableName = "Statistical";

    public  synchronized static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlStatementHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlStatementHelper.ID_PRIVATE_KEY)
                .append("deviceId  TEXT").append(",")
                .append("deviceAddress TEXT").append(",")
                .append("totalCount INTEGER").append(",")
                .append("successCount INTEGER").append(",")
                .append("timeoutCount  INTEGER").append(",")
                .append("failedCount INTEGER").append(",")
                .append("averageRSSI INTEGER").append(",")
                .append("averageOpenTime REAL").append(",")//REAL 存储float类型
                .append("successRate  REAL").append(",")
                .append("currentDate TEXT").append(",")
                .append("tag TEXT").append(")");
        db.execSQL(sb.toString());
    }

    /**
     * @param context
     * @param bean
     * @return 返回 最新插入数据的自增长主键ID
     */
    public  synchronized static int insertRow(Context context, StatisticalBean bean) {
        int ID = -1;
        if (bean == null)
            return ID;
        SQLiteDatabase db = SqliteHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO  " + tableName + " (deviceId, deviceAddress, totalCount,successCount,timeoutCount,failedCount,averageRSSI,averageOpenTime,successRate,currentDate,tag)  VALUES(?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
                    bean.deviceId, bean.deviceAddress, bean.totalCount, bean.successCount, bean.timeoutCount, bean.failedCount, bean.averageRSSI, bean.averageOpenTime, bean.successRate,bean.currentDate,bean.tag
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
