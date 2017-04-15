package com.revenco.database.buss;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.bean.CertificateBean;
import com.revenco.database.helper.SqlStatementHelper;
import com.revenco.database.helper.SqliteHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 14:52.</p>
 * <p>CLASS DESCRIBE :证书</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class CertificateBuss {
    public static String tableName = "Certificate";

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
                .append("certificateIndex  INTEGER").append(",")
                .append("deviceId TEXT").append(",")
                .append("deviceAddress TEXT").append(",")
                .append("appBleMac TEXT").append(",")
                .append("content BLOB").append(",")//证书内容，base64格式
                .append("tag TEXT").append(")");
        db.execSQL(sb.toString());
    }

    /**
     * @param context
     * @param bean
     * @return 返回 最新插入数据的自增长主键ID
     */
    public static int insertRow(Context context, CertificateBean bean) {
        int ID = -1;
        if (bean == null)
            return ID;
        SQLiteDatabase db = SqliteHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO  " + tableName + " (certificateIndex, deviceId, deviceAddress,appBleMac,content,tag)  VALUES(?,?,?,?,?,?)", new Object[]{
                    bean.certificateIndex, bean.deviceId, bean.deviceAddress, bean.appBleMac, bean.content, bean.tag
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
