package com.getyourlocation.app.client.activity;

import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.getyourlocation.app.client.Constant;
import com.getyourlocation.app.client.R;
import com.getyourlocation.app.client.util.CommonUtil;
import com.getyourlocation.app.client.util.NetworkUtil;
import com.getyourlocation.app.client.widget.CameraPreview;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xusy on 2017/6/27.
 */

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = "PhotoActivity";
    private Camera camera;
    private CameraPreview cameraPreview;
    private Button captureBtn;
    private Button testBtn;
    private Button PositionBtn;
    private NetworkUtil networkUtil;
    private float [][] imgLocation = new float[3][2];
    private boolean [] imgCapturedStatus = new boolean[3];
    private boolean [] imgUploadStatus = new boolean[3];
    private int imgCaptured;
    private int imgUpload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        initNetwork();
        initCamera();
        initCaptureBtn();
        initData();
        initTestBtn();
        initPositionBtn();
    }
    private void initPositionBtn() {
        PositionBtn = (Button) findViewById(R.id.position_btn);
        PositionBtn.setOnClickListener(new View.OnClickListener() {
            float alpha = 100;
            float beta = 100;
            float x1 = 100, y1 = 100, x2 = 100, y2 = 100, x3 = 100, y3 = 100;
            @Override
            public void onClick(View v) {
                TrianglePosition(alpha, beta, x1, y1, x2, y2, x3, y3);
            }
        });
    }

    private void initTestBtn() {
        testBtn = (Button) findViewById(R.id.test_btn);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadImage("/storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1495269258215.jpg");
            }
        });
    }

    private void initNetwork() {
        networkUtil = NetworkUtil.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera(){
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
    private void initCamera() {
        camera = Camera.open();
        cameraPreview = new CameraPreview(this, camera, new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });
        FrameLayout layout = (FrameLayout) findViewById(R.id.photo_preview_layout);
        layout.addView(cameraPreview);
    }
    private void initCaptureBtn() {
        captureBtn = (Button) findViewById(R.id.capture_btn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                camera.takePicture(null, null, pictureCallBack);

            }
        });
    }
    private Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: " );
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES ),"CollectData");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
    private void initData()
    {
        for (int i = 0; i < 3; i++) {
            imgCapturedStatus[i] = false;
            imgUploadStatus[i] = false;
        }
        imgCaptured = 0;
        imgUpload = 0;
    }
    /** upload single image */
    private void uploadImage(String imgFilename)
    {
        if (imgFilename == null || imgFilename.isEmpty()) {
            CommonUtil.showToast(PhotoActivity.this, "imgFilename is not correct!");
            return;
        }
        SimpleMultiPartRequest req = new SimpleMultiPartRequest(Request.Method.POST, Constant.URL_API_SHOPLOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        CommonUtil.showToast(PhotoActivity.this, "Upload succeed!");
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            //if (imgUpload >= imgCaptured) return;
                            for (int i = 0; i < 3; i++) {
                                if (imgCapturedStatus[i] == true && imgUploadStatus[i] == false) {
                                    imgLocation[i][0] = (float)jsonObj.get("x");
                                    imgLocation[i][1] = (float)jsonObj.get("y");
                                    imgUploadStatus[i] = true;
                                    imgUpload++;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
                CommonUtil.showToast(PhotoActivity.this, "Upload failed! Code:" + error.networkResponse.statusCode);
            }
        });
        req.addFile("img", imgFilename);
        //req.addMultipartParam("ext", "text/plain", imgFilename.substring(imgFilename.indexOf(".") + 1));
        Log.d(TAG, req.toString());
        networkUtil.addReq(req);
    }

    public void TrianglePosition(final float alpha, final float beta, final float x1, final float y1,
                                 final float x2, final float y2, final float x3, final float y3) {
        StringRequest req = new StringRequest(Request.Method.GET, Constant.URL_API_POSITION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {  // Called when server respond
                        Log.d(TAG, response);
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            float x = (float)jsonObj.get("x");
                            float y = (float)jsonObj.get("y");
                            CommonUtil.showToast(PhotoActivity.this, "x:" + x + ",y:" + y);
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("alpha", String.valueOf(alpha));
                params.put("beta", String.valueOf(beta));
                params.put("x1", String.valueOf(x1));
                params.put("y1", String.valueOf(y1));
                params.put("x2", String.valueOf(x2));
                params.put("y2", String.valueOf(y2));
                params.put("x3", String.valueOf(x3));
                params.put("y3", String.valueOf(y3));
                return params;
            }
        };
        networkUtil.addReq(req);
    }
}
