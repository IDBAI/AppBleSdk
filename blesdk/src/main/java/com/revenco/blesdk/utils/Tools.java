package com.revenco.blesdk.utils;

import android.content.Context;
import android.os.Build;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Tools {
    /**
     * @param d
     * @return
     */
    public static double formatDouble(double d) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }




}
