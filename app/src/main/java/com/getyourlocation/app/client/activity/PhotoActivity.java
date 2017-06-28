package com.getyourlocation.app.client.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.getyourlocation.app.client.Constant;
import com.getyourlocation.app.client.R;
import com.getyourlocation.app.client.util.SensorUtil;
import com.getyourlocation.app.client.util.CommonUtil;
import com.getyourlocation.app.client.util.NetworkUtil;

import com.getyourlocation.app.client.widget.CameraPreview;
import com.getyourlocation.app.client.widget.Index;

import java.io.ByteArrayOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by xusy on 2017/6/27.
 */

public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = "PhotoActivity";
    public final static int RESULT_CODE=1;

    private SensorUtil sensorUtil;
    private NetworkUtil networkUtil;

    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView[] mipmap = new ImageView[3];
    private Button captureBtn;
    private Button PositionBtn;

    private float[] sensorData = new float[3];
    private File[] refPicture = new File[3];

    private float [][] imgLocation = new float[3][2];
    private boolean [] imgCapturedStatus = new boolean[3];
    private boolean [] imgUploadStatus = new boolean[3];
    private int imgCaptured;
    private int imgUpload;
    private float[] userLocation = new float[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Index.initIndex();
        initData();
        initNetwork();
        initMipmap();
        initCancelBtn();
        initSensor();
        initCamera();
        initCaptureBtn();
        initPositionBtn();

    }
    private void returnResult() {
        float[] result = getUserLocation();
        Intent intent=new Intent();
        intent.putExtra("x", result[0]);
        intent.putExtra("y", result[1]);
        setResult(RESULT_CODE, intent);
        finish();
    }
    private float[] getUserLocation() {
        return userLocation;
    }
    private void initPositionBtn() {
        PositionBtn = (Button) findViewById(R.id.button_upload);
        PositionBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!Index.Available()) {
                    float[] angles = sensorData.clone();
                    Arrays.sort(angles);
                    float alpha = angles[2]-angles[1];
                    float beta = angles[1]-angles[0];
                    float x1 = imgLocation[0][0];
                    float y1 = imgLocation[0][1];
                    float x2 = imgLocation[1][0];
                    float y2 = imgLocation[1][1];
                    float x3 = imgLocation[2][0];
                    float y3 = imgLocation[2][1];
//                   a test example
                    alpha = 45; beta = 45;x1 =-1;y1=0;x2=0;y2=-1;x3=1;y3=0;
                    TrianglePosition(alpha, beta, x1, y1, x2, y2, x3, y3);
                }
            }
        });
    }
    private void initMipmap(){
        mipmap[0] =(ImageView) findViewById(R.id.mipmap1);
        mipmap[0].setImageResource(R.mipmap.ic_launcher_round);
        mipmap[0].setVisibility(View.VISIBLE);
        mipmap[1] =(ImageView) findViewById(R.id.mipmap2);
        mipmap[1].setImageResource(R.mipmap.ic_launcher_round);
        mipmap[1].setVisibility(View.VISIBLE);
        mipmap[2] =(ImageView) findViewById(R.id.mipmap3);
        mipmap[2].setImageResource(R.mipmap.ic_launcher_round);
        mipmap[2].setVisibility(View.VISIBLE);
    }
    private void initCancelBtn(){
        ImageButton cancelBtn1 = (ImageButton) findViewById(R.id.cancel_1);
            cancelBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView mipmap1 = (ImageView) findViewById(R.id.mipmap1);
                mipmap1.setImageResource(R.mipmap.ic_launcher_round);
                Index.resetIndex(0);
            }
        });
        ImageButton cancelBtn2 = (ImageButton) findViewById(R.id.cancel_2);
        cancelBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView mipmap2 = (ImageView) findViewById(R.id.mipmap2);
                mipmap2.setImageResource(R.mipmap.ic_launcher_round);
                Index.resetIndex(1);
            }
        });
        ImageButton cancelBtn3 = (ImageButton) findViewById(R.id.cancel_3);
        cancelBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView mipmap3 = (ImageView) findViewById(R.id.mipmap3);
                mipmap3.setImageResource(R.mipmap.ic_launcher_round);
                Index.resetIndex(2);
            }
        });
    }
    private void initNetwork() {
        networkUtil = NetworkUtil.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorUtil.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorUtil.unregister();
        releaseCamera();
    }

    private void releaseCamera(){
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
    private void initSensor() {
        for (int i = 0; i <sensorData.length;i++) {
            sensorData[i] = 0;
        }
        sensorUtil = SensorUtil.getInstance(this);
    }


    private void initCamera() {
        camera = Camera.open();
        cameraPreview = new CameraPreview(this, camera, new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });
        FrameLayout layout = (FrameLayout) findViewById(R.id.mypreviewlayout);
        layout.addView(cameraPreview);
    }
    private void initCaptureBtn() {
        captureBtn = (Button) findViewById(R.id.button_shoot);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Index.Available()) {
                    camera.takePicture(null, null, pictureCallBack);
                    camera.startPreview();
                }
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
                String filepath = pictureFile.getAbsolutePath();
                Bitmap pic = BitmapFactory.decodeFile(filepath);
                mipmap[Index.getAvailableIndex()].setImageBitmap(pic);
                sensorData[Index.getAvailableIndex()] = (float)sensorUtil.getLastGyroRotate();
                refPicture[Index.getAvailableIndex()] = pictureFile;
                uploadImage(refPicture[Index.getAvailableIndex()].getAbsolutePath());

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

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath()+File.separator+"ref"+
                    Index.getAvailableIndex()+".jpg");
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
        userLocation[0] = 0;
        userLocation[1] = 0;
    }
    /** upload single image */
    private void uploadImage(final String imgFilename)
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
                            int i = Index.getAvailableIndex();
                            double x = ((Number)jsonObj.get("x")).doubleValue();
                            double y = ((Number)jsonObj.get("y")).doubleValue();
                            imgLocation[i][0] = (float) x;
                            imgLocation[i][1] =  (float)y;
                            imgUploadStatus[i] = true;
                            imgUpload++;
                            CommonUtil.showToast(PhotoActivity.this, imgFilename+":location is x:"+imgLocation[i][0]+" y:"+imgLocation[i][1]);
                            CommonUtil.showToast(PhotoActivity.this, "Angle is "+ sensorData[Index.getAvailableIndex()]);
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
                            double x = ((Number)jsonObj.get("x")).doubleValue();
                            double y = ((Number)jsonObj.get("y")).doubleValue();
                            userLocation[0] = (float)x;
                            userLocation[1] = (float)y;
                            // uncompleted api test
                            userLocation[0] = (float)100;
                            userLocation[1] = (float)120;
                            Log.d(TAG,  "x:" + x + ",y:" + y);
                            CommonUtil.showToast(PhotoActivity.this, "x:" + x + ",y:" + y);
                          //  Thread.currentThread().sleep(5000);
                            returnResult();
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
