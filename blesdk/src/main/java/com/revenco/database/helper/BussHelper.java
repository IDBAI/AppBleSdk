package com.revenco.database.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 18:30.</p>
 * <p>CLASS DESCRIBE :SQLite查数据表，并转换为需要的对象</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class BussHelper {
    /**
     * @param context
     * @param javaBeanClass 需要转换的为的对象
     * @param tableName     对应的SQLite数据库的表名
     * @param <T>           返回一个 javaBeanClass 对应的列表
     * @return
     */
    public static <T> List<T> queryAll(Context context, Class<T> javaBeanClass, String tableName) {
        List<T> list = new ArrayList<>();
        SQLiteDatabase db =  SqliteHelper.getInstance(context).getReadableDatabase();
        T bean;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);
            while (cursor.moveToNext()) {
                bean = javaBeanClass.newInstance();
                bindValues(bean, cursor);
                list.add(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            db.close();
        }
        return list;
    }

    /**
     * 获取游标的值，为泛型对象的成员赋值，
     *
     * @param bean
     * @param cursor
     * @param <T>
     */
    private static <T> void bindValues(T bean, Cursor cursor) {
        Class<?> aClass = bean.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            Class<?> type = field.getType();
            Object values = null;
            if (type == int.class)
                values = cursor.getInt(cursor.getColumnIndex(name));
            else if (type == float.class)
                values = cursor.getFloat(cursor.getColumnIndex(name));
            else if (type == double.class)
                values = cursor.getDouble(cursor.getColumnIndex(name));
            else if (type == String.class)
                values = cursor.getString(cursor.getColumnIndex(name));
            AssignValueForAttributeHelper.setAttrributeValue(bean, name, values);
        }
    }
}
