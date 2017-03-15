package com.revenco.blesdk.utils;

/**
 * Created by Administrator on 2016/11/10.
 */
public class Constants {
    public static final String TAG_Broadcast_Filter_UUID = "TAG_Broadcast_Filter_UUID";
    public static final String TAG_Service_UUID = "TAG_Service_UUID";
    public static final String TAG_Notify_UUID = "TAG_Notify_UUID";
    public static final String TAG_Write_UUID = "TAG_Write_UUID";

    //0215 是前缀，
    public static final String ALTBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";//"m:2-3=2F42,i:4-19,i:20-21,i:22-23,p:24-24";//
    public static final String EDDYSTONE_UID_LAYOUT = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    public static final String TAG_MyDeviceAddress = "TAG_MyDeviceAddress";
}
