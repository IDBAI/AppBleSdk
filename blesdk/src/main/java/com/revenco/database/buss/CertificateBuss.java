package com.revenco.database.buss;

import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.helper.SqlStatementHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 14:52.</p>
 * <p>CLASS DESCRIBE :证书</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class CertificateBuss {
    private static String tableName = "Certificate";

    /**
     * 本地自增长ID
     * ID , Index , deviceId ,deviceAddress, appBleMac , Content , Tag ,
     */
    public static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlStatementHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlStatementHelper.ID_PRIVATE_KEY)
                .append("Index  INTEGER").append(",")
                .append("deviceId TEXT").append(",")
                .append("deviceAddress TEXT").append(",")
                .append("appBleMac TEXT").append(",")
                .append("Content BLOB").append(",")//证书内容，base64格式
                .append("Tag TEXT").append(")");
        db.execSQL(sb.toString());
    }
}
