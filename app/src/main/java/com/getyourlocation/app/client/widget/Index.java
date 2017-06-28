package com.getyourlocation.app.client.widget;

/**
 * Created by xusy on 2017/6/27.
 */

public class Index {
    private static final int indexSize = 3;
    private static boolean[] index = new boolean[indexSize];
    private static int currentIndex = -1;
    public static void initIndex(){
        for (int i =0; i < indexSize; i++) {
            index[i] = false;
        }
    }
    public static void setCurrentIndex(int i) {
        if (i>=0 && i<indexSize) {
            currentIndex = i;
        }
    }

    public static void resetIndex(int i) {
        if (i>=0 && i<indexSize) {
            index[i] = false;
        }
    }
    public static boolean Available(){
        int i = 0;
        boolean result = false;
        for (; i < indexSize;i++) {
            if (index[i]==false) {
                result = true;
                currentIndex = i;
                break;
            }
        }
        return result;
    }
    public static int getAvailableIndex(){
        if (currentIndex != -1) {
            index[currentIndex] = true;
            return currentIndex;
        }
        return -1;
    }
}
