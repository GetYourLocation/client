package com.getyourlocation.app.client.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.cache.DiskLruBasedCache;
import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


/**
 * Manage HTTp request and response.
 */
public class NetworkUtil {
    private static final String TAG = "NetworkUtil";
    private static final String REQ_TAG = "req";
    private static final int REQ_TIMEOUT = 10000;

    private static NetworkUtil instance = null;

    private RequestQueue queue;
    private Context context;

    /**
     * Return the only instance.
     */
    public static NetworkUtil getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtil(context);
        }
        return instance;
    }

    /**
     * Initialize.
     */
    private NetworkUtil(Context context) {
        this.context = context;
        queue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Cancel all requests in the queue.
     */
    public void cancelAll() {
        queue.cancelAll(REQ_TAG);
    }

    /**
     * Return true if current network is available.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
        return false;
    }

    /**
     * Add a network request to the request queue.
     *
     * @param request The request to service
     */
    public <T> void addReq(Request<T> request) {
        request.setRetryPolicy(new DefaultRetryPolicy(REQ_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(REQ_TAG);
        queue.add(request);
    }

    /**
     * Fetch an image from server.
     *
     * @param imageURI The URI of the image.
     * @param listener The listen called when the image is responded or error occurs.
     */
    public void fetchImage(String imageURI, ImageLoader.ImageListener listener) {
        DiskLruBasedCache.ImageCacheParams cacheParams =
                new DiskLruBasedCache.ImageCacheParams(context.getApplicationContext(), "CacheDirectory");
        cacheParams.setMemCacheSizePercent(0.5f);
        SimpleImageLoader mImageFetcher = new SimpleImageLoader(context.getApplicationContext(), cacheParams);
        mImageFetcher.setMaxImageSize(300);
        mImageFetcher.get(imageURI, listener);
    }
}
