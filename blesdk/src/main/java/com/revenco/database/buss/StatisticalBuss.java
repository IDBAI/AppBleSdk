package com.revenco.database.buss;

import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.SqlHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 14:52.</p>
 * <p>CLASS DESCRIBE :证书</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class StatisticalBuss {
    private static String tableName = "Statistical";

    public static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlHelper.ID_PRIVATE_KEY)
                .append("deviceId  TEXT").append(",")
                .append("deviceAddress TEXT").append(",")
                .append("totalCount INTEGER").append(",")
                .append("openSuccessCount INTEGER").append(",")
                .append("timeoutCount  INTEGER").append(",")
                .append("failedCount INTEGER").append(",")
                .append("averageRSSI INTEGER").append(",")
                .append("averageOpenTime REAL").append(",")//REAL 存储float类型
                .append("successRate  REAL").append(")");
        db.execSQL(sb.toString());
    }
}
