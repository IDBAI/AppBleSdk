package com.revenco.network.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 2017/1/3.
 */
public class StorageUtils {
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "brt-cache";
    private static final String TAG = "StorageUtils";

    private StorageUtils() {
    }

    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        if (preferExternal && "mounted".equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Log.w(TAG, "Can\'t define system cache directory! \'%s\' will be used.");
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    public static File getIndividualCacheDirectory(Context context) {
        File cacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(cacheDir, "brt-cache");
        if (!individualCacheDir.exists() && !individualCacheDir.mkdir()) {
            individualCacheDir = cacheDir;
        }
        return individualCacheDir;
    }

    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if ("mounted".equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    public static File getFilePath(Context context, String name) {
        File file = new File(getExternalTextDir(context).getAbsolutePath() + "/" + name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }
        return file;
    }

    //
    public static String splitName(String path) {
        String[] arr = path.split("/");
        return arr[arr.length - 1];
    }

    /**
     * @param context
     * @param name eg：StorageUtils.getDataPath(context, "Certificate/privatekey.pem");
     * @return
     */
    public static File getDataPath(Context context, String name) {
        File file = new File(getExternalDataDir(context).getAbsolutePath() + "/" + name);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();//级联创建文件夹,才能创建文件
            }
            try {
                file.createNewFile();//创建文件，必须要存在父文件夹
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean exitesFile(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        } else {
            File file = new File(getExternalDataDir(context).getAbsolutePath() + "/" + splitName(name));
            return file.exists();
        }
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                (new File(appCacheDir, ".nomedia")).createNewFile();
            } catch (IOException var4) {
                Log.i(TAG, "Can\'t create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    public static File getExternalTextDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "text");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "Unable to create external text directory");
                return null;
            }
            try {
                (new File(appCacheDir, ".nomedia")).createNewFile();
            } catch (IOException var4) {
                Log.i(TAG, "Can\'t create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    public static File getExternalDataDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "data");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "Unable to create external text directory");
                return null;
            }
            try {
                (new File(appCacheDir, ".nomedia")).createNewFile();
            } catch (IOException var4) {
                Log.i(TAG, "Can\'t create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    public static boolean exists(File file) {
        return file == null ? false : file.exists();
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        return perm == 0;
    }
}
