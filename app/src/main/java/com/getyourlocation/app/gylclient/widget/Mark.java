package com.getyourlocation.app.gylclient.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getyourlocation.app.gylclient.R;
import com.palmaplus.nagrand.view.overlay.OverlayCell;


/**
 * Created by lchad on 2016/11/1.
 * Github: https://github.com/lchad
 */
public class Mark extends LinearLayout implements OverlayCell {
    private ImageView mIconView;
    private TextView mPosX;
    private TextView mPosY;
    private TextView mPosId;

    private double[] mGeoCoordinate;
    private int mId;

    /**
     * 此Mark所属的楼层id.
     */
    public long mFloorId;

    public Mark(Context context) {
        super(context);

        initView();
    }

    public Mark(Context context, int id) {
        super(context);

        this.mId = id;
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_mark, this);
        mIconView = (ImageView) findViewById(R.id.mark_icon);
        mPosX = (TextView) findViewById(R.id.mark_x);
        mPosY = (TextView) findViewById(R.id.mark_y);
        mPosId = (TextView) findViewById(R.id.mark_id);
        mPosId.setText(String.valueOf(mId));
    }

    public void setMark(int id, double x, double y) {
        mId = id;
        mPosId.setText(String.valueOf(id));
        mPosX.setText("x: " + x + "");
        mPosY.setText("y: " + y + "");
    }

    public void setMark(int id, double x, double y, int resId) {
        mId = id;
        mPosId.setText(String.valueOf(id));
        mPosX.setText("x: " + x + "");
        mPosY.setText("y: " + y + "");
        mIconView.setBackgroundResource(resId);
    }

    public void setTitle(String title) {
        mPosId.setText(title);
    }

    public void setTitle(int resId) {
        mPosId.setText(getResources().getString(resId));
    }

    @Override
    public void init(double[] doubles) {
        mGeoCoordinate = doubles;
    }

    @Override
    public double[] getGeoCoordinate() {
        return mGeoCoordinate;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void position(double[] doubles) {
        setX((float) doubles[0] - getWidth() / 2);
        setY((float) doubles[1] - getHeight() / 2);
    }

    @Override
    public long getFloorId() {
        return mFloorId;
    }

    /**
     * 设置FloorId
     * @param floorId
     */
    public void setFloorId(long floorId) {
        mFloorId = floorId;
    }
}
