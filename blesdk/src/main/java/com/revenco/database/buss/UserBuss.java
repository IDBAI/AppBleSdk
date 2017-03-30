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
public class UserBuss {
    private static String tableName = "user";

    public static void createTable(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append(SqlStatementHelper.CreateTablePre)
                .append(tableName)
                .append("(")
                .append(SqlStatementHelper.ID_PRIVATE_KEY)
                .append("userId  TEXT").append(",")
                .append("mobileNum TEXT").append(",")
                .append("communityId TEXT").append(")");
        db.execSQL(sb.toString());
    }
}
