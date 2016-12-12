package com.zhang.taxiclient;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TaxiActivity extends AppCompatActivity implements BDLocationListener, ServiceConnection, View.OnClickListener {

    private LocationClient mLocationClient = null;
    private LatLng mLatLng;

    private boolean shareable = true;//判断服务能否开始或停止
    private boolean isBound = false;//判断服务是否绑定
    private MyService.Binder binder;

    private String taxiName, taxiNum;
    private String passengerName, distance, destination, passengerNum, hiberId;

    private LinearLayout layoutPassengerList, layoutTakeOrder;
    private ImageButton btnBack, btnTBack;
    private TextView tvBack, tvTBack;
    private TextView tvPassengerName, tvDis, tvDes, tvPassengerNum, tvCallPassenger;

    private ListView passengerList;
    private PassengerAdapter adapter;

    private SQLiteDatabase dbWrite, dbRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_taxi_content);

        layoutPassengerList = (LinearLayout) findViewById(R.id.passengerList_layout);
        layoutTakeOrder = (LinearLayout) findViewById(R.id.takeOrder_layout);

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnTBack = (ImageButton) findViewById(R.id.btnTBack);
        tvBack = (TextView) findViewById(R.id.tvBack);
        tvTBack = (TextView) findViewById(R.id.tvTBack);

        tvPassengerName = (TextView) findViewById(R.id.tvPassengerName);
        tvDis = (TextView) findViewById(R.id.tvDis);
        tvDes = (TextView) findViewById(R.id.tvDes);
        tvPassengerNum = (TextView) findViewById(R.id.tvPassengerNum);
        tvCallPassenger = (TextView) findViewById(R.id.tvCallPassenger);

        passengerList = (ListView) findViewById(R.id.passengerList);

        //获取司机电话、名字
        taxiNum = getIntent().getStringExtra("telNum");
        taxiName = getIntent().getStringExtra("name");


        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener(this);    //注册监听函数

        //设置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

        mLocationClient.start();

        PInfoDb db = new PInfoDb(this);
        dbRead = db.getReadableDatabase();
        dbWrite = db.getWritableDatabase();

        //进入主界面开启service
        if (shareable) {
            Intent i = new Intent(TaxiActivity.this, MyService.class);
            //启动service
            startService(i);
            //绑定service，利用onServiceConnected来获取socket传回的数据
            isBound = bindService(i, this, Context.BIND_AUTO_CREATE);

            shareable = false;//开启后 才可停止
        }


        Cursor c = dbRead.query("pinfo", null, null, null, null, null, "_id desc");
        adapter = new PassengerAdapter(this, c);
        passengerList.setAdapter(adapter);

        refreshList();

        //点击listItem即表示接单
        passengerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //获取客户数据
                Cursor c = dbRead.query("pinfo", null, null, null, null, null, "_id desc");
                c.moveToPosition(position);
                passengerName = c.getString(1);
                distance = c.getString(2);
                destination = c.getString(3);
                passengerNum = c.getString(4);
                hiberId = c.getString(5);

                int dbId = c.getInt(0);

                //显示接单界面
                tvPassengerName.setText(passengerName);
                tvDis.setText("距离" + distance + "米");
                tvDes.setText("到 " + destination);
                tvPassengerNum.setText(passengerNum);

                layoutPassengerList.setVisibility(View.GONE);
                layoutTakeOrder.setVisibility(View.VISIBLE);

                //接单完成删除本地数据
                dbWrite.delete("pinfo", "_id = ?", new String[]{dbId + ""});
                refreshList();

                //发送消息到服务器，删除服务器该客户数据，再广播给各个司机端删除该客户数据，同时广播给客户
                String orderInfo = "order" + "@" + passengerName + "@" + destination + "@" + passengerNum + "@" + hiberId + "@" + taxiName + "@" + taxiNum;
                if (binder != null) {
                    binder.uploadData(orderInfo);
                }

            }
        });

        btnBack.setOnClickListener(this);
        btnTBack.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        tvTBack.setOnClickListener(this);
        tvCallPassenger.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //关闭程序是停止共享及服务
        stopService(new Intent(TaxiActivity.this, MyService.class));
        //解绑服务  如果没解绑会报错崩溃  若为绑定就解绑也会崩溃
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
        shareable = true;

        //退出程序 删除SQL数据
        dbWrite.delete("pinfo", null, null);
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        //出租车位置信息
        String taxiLocInfo = "taxi" + "@" + bdLocation.getLatitude() + " @ " + bdLocation.getLongitude();

        //向服务器传递出租车的位置信息
        if (binder != null) {
            binder.uploadData(taxiLocInfo);
        }

        mLatLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MyService.Binder) service;
        binder.getService().setCallback(
                new MyService.Callback() {
                    @Override
                    public void onDataChange(String data) {


                        //乘客订单信息
                        if (data.contains("passenger")) {

                            String[] tempInfo = data.split("@");
                            //乘客位置
                            double mLat = Double.parseDouble(tempInfo[2]);
                            double mLon = Double.parseDouble(tempInfo[3]);
                            LatLng pLL = new LatLng(mLat, mLon);
                            //距离
                            int distance = (int) DistanceUtil.getDistance(mLatLng, pLL);

                            //1.5公里以内
                            if (distance < 1500) {
                                Cursor c = dbRead.query("pinfo", null, null, null, null, null, null);
                                ArrayList<String> dates = new ArrayList<String>();
                                String s;
                                while (c.moveToNext()) {
                                    s = c.getString(5);
                                    dates.add(s);
                                }
                                if (!(dates.contains(tempInfo[6]))) {
                                    ContentValues cv = new ContentValues();
                                    cv.put("name", tempInfo[1]);
                                    cv.put("dis", distance + "");
                                    cv.put("des", tempInfo[4]);
                                    cv.put("tel", tempInfo[5]);
                                    cv.put("hid", tempInfo[6]);
                                    dbWrite.insert("pinfo", null, cv);
                                    refreshList();
                                }

                            }

                        } else if (data.contains("cancel")) {
                            //取消本地订单信息
                            String[] tempC = data.split("@");
                            int dbId;
                            Cursor c = dbRead.query("pinfo", null, null, null, null, null, null);
                            while (c.moveToNext()) {
                                if (tempC[1].equals(c.getString(1)) && tempC[2].equals(c.getString(3)) && tempC[3].equals(c.getString(4))) {
                                    dbId = c.getInt(0);
                                    dbWrite.delete("pinfo", "_id = ?", new String[]{dbId + ""});
                                }
                            }
                            refreshList();
                        } else if (data.contains("order")) {
                            //其他司机端发送来的接单信息，用于删除本地订单信息
                            String[] tempO = data.split("@");
                            int dbId;
                            Cursor c = dbRead.query("pinfo", null, null, null, null, null, null);
                            while (c.moveToNext()) {
                                if (tempO[1].equals(c.getString(1)) && tempO[2].equals(c.getString(3)) && tempO[3].equals(c.getString(4))) {
                                    dbId = c.getInt(0);
                                    dbWrite.delete("pinfo", "_id = ?", new String[]{dbId + ""});
                                }
                            }
                            refreshList();
                        } else if (data.equals("you are late")) {
                            tvCallPassenger.setText("您太迟了，该订单已被其他司机接单！");
                            tvCallPassenger.setClickable(false);
                        }
                    }

                }

        );
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public void refreshList() {
        Cursor cursor = dbRead.query("pinfo", null, null, null, null, null, "_id desc");
        adapter = new PassengerAdapter(this, cursor);
        adapter.notifyDataSetChanged();
        passengerList.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnTBack:
                layoutPassengerList.setVisibility(View.VISIBLE);
                layoutTakeOrder.setVisibility(View.GONE);
                break;
            case R.id.tvBack:
                finish();
                break;
            case R.id.tvTBack:
                layoutPassengerList.setVisibility(View.VISIBLE);
                layoutTakeOrder.setVisibility(View.GONE);
                break;
            case R.id.tvCallPassenger:

                Intent intentCall = new Intent();
                intentCall.setAction(Intent.ACTION_DIAL);
                intentCall.setData(Uri.parse("tel:" + passengerNum));
                intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);

                break;
        }
    }
}
