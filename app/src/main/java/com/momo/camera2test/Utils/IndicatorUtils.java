package com.momo.camera2test.Utils;

public class IndicatorUtils {

    public final static int MIN_INDEX = 0;
    public final static int MAX_INDEX = 5;
    public static int sSelectposition = 1;

    public static long sChangeFuntionTime = 0;

    public static boolean sIsFasterScroller = false;

    public static boolean sIsReStartCameraEnd = false;

    public static boolean sIsClickerIndicator = false;

    public static int getCurrentSelectedIndex() {
        return sSelectposition;
    }

    public static void setSelectedIndex(int index) {
        sSelectposition = index;
    }

}
