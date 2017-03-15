package com.revenco.blesdk.utils;

import java.math.BigDecimal;

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
