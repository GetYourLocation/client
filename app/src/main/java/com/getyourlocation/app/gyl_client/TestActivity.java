package com.getyourlocation.app.gyl_client;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.ImageLoader;
import com.getyourlocation.app.gyl_client.util.CommonUtil;
import com.getyourlocation.app.gyl_client.util.NetworkUtil;
import com.getyourlocation.app.gyl_client.util.SensorUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class TestActivity extends AppCompatActivity {
    private static final String TAG = "TestActivity";

    private static final String SERVER_IP = "http://120.25.76.106";
    private static final String URL_SUM = SERVER_IP + "/gyl/api/sum";
    private static final String URL_PRODUCT = SERVER_IP + "/gyl/api/product";
    private static final String URL_UPLOAD = SERVER_IP + "/gyl/api/upload";

    private static final int REQ_PICK_IMG = 1;

    private SensorUtil sensorUtil;
    private NetworkUtil networkUtil;

    private TextView sensorInfoTxt;
    private EditText xValTxt;
    private EditText yValTxt;
    private ImageButton imgBtn;
    private ImageView resultImgView;

    private String imgFilename;
    private boolean permitUpload = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initNetwork();
        initSensor();
        initSumProduct();
        initImgBtn();
        initResultImgView();
        initUploadBtn();
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
    }

    private void initNetwork() {
        networkUtil = NetworkUtil.getInstance(this);
    }

    private void initSensor() {
        sensorInfoTxt = (TextView) findViewById(R.id.test_sensorInfoTxt);
        Button resetBtn = (Button) findViewById(R.id.test_resetBtn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorUtil.reset();
            }
        });
        sensorUtil = SensorUtil.getInstance(this);
        sensorUtil.setOnSensorUpdatedListener(new SensorUtil.OnSensorUpdatedListener() {
            @Override
            public void onUpdated() {
                float[] acceleration = sensorUtil.getAcceleration();
                float[] gyroRotate = sensorUtil.getGyroRotate();
                float compassRotate = sensorUtil.getCompassRotate();
                sensorInfoTxt.setText(getString(R.string.sensor_data,
                        acceleration[0],
                        acceleration[1],
                        acceleration[2],
                        gyroRotate[0],
                        gyroRotate[1],
                        gyroRotate[2],
                        compassRotate));
            }
        });
    }

    private void initSumProduct() {
        xValTxt = (EditText) findViewById(R.id.test_x_editTxt);
        yValTxt = (EditText) findViewById(R.id.test_y_editTxt);
        Button sumBtn = (Button) findViewById(R.id.test_sumBtn);
        Button productBtn = (Button) findViewById(R.id.test_productBtn);
        sumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = Integer.parseInt(xValTxt.getText().toString());
                int y = Integer.parseInt(yValTxt.getText().toString());
                computeSum(x, y);
            }
        });
        productBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = Integer.parseInt(xValTxt.getText().toString());
                int y = Integer.parseInt(yValTxt.getText().toString());
                computeProduct(x, y);
            }
        });
    }

    private void computeSum(final int x, final int y) {
        StringRequest req = new StringRequest(Request.Method.GET, URL_SUM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {  // Called when server respond
                Log.d(TAG, response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    int ans = (int)jsonObj.get("ans");
                    CommonUtil.showToast(TestActivity.this, String.valueOf(ans));
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
                params.put("x", String.valueOf(x));
                params.put("y", String.valueOf(y));
                return params;
            }
        };
        networkUtil.addReq(req);
    }

    private void computeProduct(final int x, final int y) {
        StringRequest req = new StringRequest(Request.Method.POST, URL_PRODUCT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {  // Called when server respond
                Log.d(TAG, response);
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    int ans = (int)jsonObj.get("ans");
                    CommonUtil.showToast(TestActivity.this, String.valueOf(ans));
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
                params.put("x", String.valueOf(x));
                params.put("y", String.valueOf(y));
                return params;
            }
        };
        networkUtil.addReq(req);
    }

    private void initImgBtn() {
        imgBtn = (ImageButton) findViewById(R.id.test_imgBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQ_PICK_IMG);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMG && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            Log.d(TAG, "imgUri: " + imgUri.toString());
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(getApplicationContext(), imgUri, proj, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            imgFilename = cursor.getString(colIndex);
            cursor.close();
            Log.d(TAG, "imgFilename: " + imgFilename);
            imgBtn.setImageURI(imgUri);
        }
    }

    private void initResultImgView() {
        resultImgView = (ImageView) findViewById(R.id.test_resultImgView);
    }

    private void initUploadBtn() {
        Button uploadBtn = (Button) findViewById(R.id.test_uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!permitUpload) {
                    CommonUtil.showToast(TestActivity.this, "Restart this activity to upload the next image!");
                    return;
                }
                if (imgFilename == null || imgFilename.isEmpty()) {
                    CommonUtil.showToast(TestActivity.this, "Click the circle to select an image first!");
                    return;
                }
                permitUpload = false;
                SimpleMultiPartRequest req = new SimpleMultiPartRequest(Request.Method.POST, URL_UPLOAD,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, response);
                            CommonUtil.showToast(TestActivity.this, "Upload succeed!");
                            try {
                                JSONArray jsonArr = new JSONArray(response);
                                String uri = (String)jsonArr.get(0);
                                showUploadedImg(SERVER_IP + uri);
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "", error);
                            CommonUtil.showToast(TestActivity.this, "Upload failed! Code:" + error.networkResponse.statusCode);
                        }
                    });
                req.addFile("file", imgFilename);
                req.addMultipartParam("ext", "text/plain", imgFilename.substring(imgFilename.indexOf(".") + 1));
                networkUtil.addReq(req);
            }
        });
    }

    private void showUploadedImg(String imgURI) {
        networkUtil.fetchImage(imgURI, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                resultImgView.setImageBitmap(bitmap);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "", error);
            }
        });
    }
}