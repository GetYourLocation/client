package com.getyourlocation.app.gylclient.util;

import android.content.Context;
import android.widget.Toast;


/**
 * Commonly used methods.
 */
public class CommonUtil {
    private static final String TAG = "CommonUtil";

    /**
     * Show a Toast message
     * @param context The context
     * @param strId The string resource id
     */
    public static void showToast(Context context, int strId) {
        showToast(context, context.getString(strId));
    }

    /**
     * Show a Toast message
     * @param context The context
     * @param str The string content
     */
    public static void showToast(Context context, String str) {
        if (context != null) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }
}
