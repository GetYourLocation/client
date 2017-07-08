package com.getyourlocation.app.client.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

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

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class PhotoActivity extends AppCompatActivity {
    private static final String TAG = "PhotoActivity";
    public final static int RES_POSITION = 1;
    private final int resizeWidth = 228;
    private final int resizeHeight = 128;
    private SensorUtil sensorUtil;
    private NetworkUtil networkUtil;

    private Camera camera;
    private CameraPreview cameraPreview;
    private ImageView[] mipmap = new ImageView[3];
    private TextView[] mipmap_info = new TextView[3];
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
    private boolean isView = true;
    private boolean initCam = false;
    private boolean isHelpShowed = false;
    private boolean finishUpload = true;
    private String Hint_start = "从右向左旋转手机拍照\n" +
            "旋转过程中传感器会记录旋转角度\n"+
            "拍满三张可以提交并定位\n" +
            "长按图片可取消已拍图片并按顺序重拍\n"
            ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Index.initIndex();
        initData();
        initNetwork();
        initMipmap();
        initSensor();
        initCamera();
        initCaptureBtn();
        initPositionBtn();
        initZoomBtn();
        initHelpBtn();
    }

    private void returnResult() {
        float[] result = getUserLocation();
        Intent intent=new Intent();
        intent.putExtra("x", result[0]);
        intent.putExtra("y", result[1]);
        setResult(RES_POSITION, intent);
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
//                    alpha = 45; beta = 45;x1 =-1;y1=0;x2=0;y2=-1;x3=1;y3=0;
                    TrianglePosition(alpha, beta, x1, y1, x2, y2, x3, y3);
                }
            }
        });
    }
    private void initHelpBtn(){
        final Button helpBtn = (Button) findViewById(R.id.button_help);
        final TextView helpText = (TextView) findViewById(R.id.infomation_help);
        helpText.setText(Hint_start);
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isHelpShowed == false) {
                    isHelpShowed = true;
                    helpText.setVisibility(View.VISIBLE);
                    helpBtn.setText("取消帮助");
                } else {
                    isHelpShowed = false;
                    helpText.setVisibility(View.INVISIBLE);
                    helpBtn.setText("帮助");
                }
            }
        });
    }
    private void initMipmap(){
        mipmap_info[0] = (TextView)findViewById(R.id.mipmap1_info);
        mipmap_info[0].setVisibility(View.INVISIBLE);
        mipmap[0] =(ImageView) findViewById(R.id.mipmap1);
        mipmap[0].setVisibility(View.INVISIBLE);
        mipmap[0].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView mipmap1 = (ImageView) findViewById(R.id.mipmap1);
                mipmap1.setVisibility(View.INVISIBLE);
                mipmap_info[0].setVisibility(View.INVISIBLE);
                Index.resetIndex(0);
                return true;
            }
        });

        mipmap_info[1] = (TextView)findViewById(R.id.mipmap2_info);
        mipmap_info[1].setVisibility(View.INVISIBLE);
        mipmap[1] =(ImageView) findViewById(R.id.mipmap2);
        mipmap[1].setVisibility(View.INVISIBLE);
        mipmap[1].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView mipmap2 = (ImageView) findViewById(R.id.mipmap2);
                mipmap2.setVisibility(View.INVISIBLE);
                mipmap_info[1].setVisibility(View.INVISIBLE);
                Index.resetIndex(1);
                return true;
            }
        });

        mipmap_info[2] = (TextView)findViewById(R.id.mipmap3_info);
        mipmap_info[2].setVisibility(View.INVISIBLE);
        mipmap[2] =(ImageView) findViewById(R.id.mipmap3);
        mipmap[2].setVisibility(View.INVISIBLE);

        mipmap[2].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView mipmap3 = (ImageView) findViewById(R.id.mipmap3);
                mipmap3.setVisibility(View.INVISIBLE);
                mipmap_info[2].setVisibility(View.INVISIBLE);
                Index.resetIndex(2);
                return true;
            }
        });

    }
    private void initZoomBtn() {
        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar_zoom);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekBarZoom(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
        if (initCam == false) {
           initCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorUtil.unregister();
        releaseCamera();
        FrameLayout layout = (FrameLayout) findViewById(R.id.mypreviewlayout);
        layout.removeAllViewsInLayout();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorUtil.unregister();
        releaseCamera();
    }


    private void releaseCamera(){
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            initCam = false;
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
        cameraPreview = new CameraPreview(this,camera,null);
        FrameLayout layout = (FrameLayout) findViewById(R.id.mypreviewlayout);
        layout.addView(cameraPreview);
        initCam = true;
    }

    private void initCaptureBtn() {
        captureBtn = (Button) findViewById(R.id.button_shoot);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isView == true) {
                    if (finishUpload == true) {
                        if (Index.Available() ) {
                            finishUpload = false;
                            isView = false;
                            camera.takePicture(null, null, pictureCallBack);
                        }
                    }
                }
            }
        });
    }

    /**
     *  处理图片
     * @param bm 所要转换的bitmap
     * @param newWidth 新的宽
     * @param newHeight 新的高
     * @return 指定宽高的bitmap
     */
    private static Bitmap resizeImg(Bitmap bm, int newWidth ,int newHeight){
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    private Camera.PictureCallback pictureCallBack = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: " );
                camera.startPreview();
                isView = true;
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap originPic = BitmapFactory.decodeByteArray(data, 0, data.length);
//                Log.d(TAG, "originPic size: Height "+ originPic.getHeight() + ",Width "+originPic.getWidth());
//                System.out.println( "originPic size: Height "+ originPic.getHeight() + ",Width "+originPic.getWidth());
                Bitmap pic = resizeImg(originPic, resizeWidth, resizeHeight);
//                Log.d(TAG, "newPic size: Height "+ pic.getHeight() + ",Width "+pic.getWidth());
//                System.out.println( "newPic size: Height "+ pic.getHeight() + ",Width "+pic.getWidth());
                pic.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                mipmap[Index.getAvailableIndex()].setImageBitmap(pic);
                mipmap[Index.getAvailableIndex()].setVisibility(View.VISIBLE);
                sensorData[Index.getAvailableIndex()] = (float)sensorUtil.getLastGyroRotate();
                refPicture[Index.getAvailableIndex()] = pictureFile;
                uploadImage(refPicture[Index.getAvailableIndex()].getAbsolutePath());
                System.out.println( "upload file:"+refPicture[Index.getAvailableIndex()].getAbsolutePath());
                camera.startPreview();
                isView = true;

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    public void setSeekBarZoom(int value){
        try{
            Camera.Parameters params = camera.getParameters();
            params.setZoom(value);
            camera.setParameters(params);
            Log.d(TAG, "Is support Zoom " + params.isZoomSupported());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type) {
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

    private void initData() {
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
    private void uploadImage(final String imgFilename) {
        if (imgFilename == null || imgFilename.isEmpty()) {
            CommonUtil.showToast(PhotoActivity.this, "imgFilename is not correct!");
            return;
        }
        SimpleMultiPartRequest req = new SimpleMultiPartRequest(Request.Method.POST, Constant.URL_API_SHOP_LOCATION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                    //    CommonUtil.showToast(PhotoActivity.this, "Upload succeed!");
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            int i = Index.getAvailableIndex();
                            double x = ((Number)jsonObj.get("x")).doubleValue();
                            double y = ((Number)jsonObj.get("y")).doubleValue();
                            imgLocation[i][0] = (float) x;
                            imgLocation[i][1] =  (float)y;
                            imgUploadStatus[i] = true;
                            imgUpload++;
                            String loc_info = String.format("%d,%d",(int)imgLocation[i][0],(int)imgLocation[i][1] );
                            mipmap_info[i].setText(loc_info);
                            mipmap_info[i].setVisibility(View.VISIBLE);
                            CommonUtil.showToast(PhotoActivity.this, "目前相对旋转角度为："+ sensorData[Index.getAvailableIndex()]);
                            finishUpload = true;
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                            finishUpload = true;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
                CommonUtil.showToast(PhotoActivity.this, "上传失败，请检查您的网络设置，错误代码：" + error.networkResponse.statusCode);
//                CommonUtil.showToast(PhotoActivity.this, "Upload failed! Code:" + error.networkResponse.statusCode);
                finishUpload = true;
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
//                            // uncompleted api test
//                            userLocation[0] = (float)100;
//                            userLocation[1] = (float)120;
                            Log.d(TAG,  "x:" + x + ",y:" + y);
                            CommonUtil.showToast(PhotoActivity.this, "x:" + x + ",y:" + y);
                          //  Thread.currentThread().sleep(5000);
                            returnResult();
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                            CommonUtil.showToast(PhotoActivity.this, "提交失败，请检查您的网络");
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
