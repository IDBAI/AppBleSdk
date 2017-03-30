package com.revenco.database.buss;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.revenco.database.bean.UserBean;
import com.revenco.database.helper.SqlStatementHelper;
import com.revenco.database.helper.SqliteHelper;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 14:52.</p>
 * <p>CLASS DESCRIBE :证书</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class UserBuss {
    public static String tableName = "user";

    public static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlStatementHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlStatementHelper.ID_PRIVATE_KEY)
                .append("userId  TEXT").append(",")
                .append("mobileNum TEXT").append(",")
                .append("communityId TEXT").append(",")
                .append("tag TEXT").append(")");
        db.execSQL(sb.toString());
    }

    /**
     * @param context
     * @param bean
     * @return 返回 最新插入数据的自增长主键ID
     */
    public static int insertRow(Context context, UserBean bean) {
        int ID = -1;
        if (bean == null)
            return ID;
        SQLiteDatabase db = new SqliteHelper(context).getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("INSERT INTO  " + tableName + " (userId, mobileNum, communityId,tag)  VALUES(?,?,?,?)", new Object[]{
                    bean.userId, bean.mobileNum, bean.communityId,bean.tag
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
