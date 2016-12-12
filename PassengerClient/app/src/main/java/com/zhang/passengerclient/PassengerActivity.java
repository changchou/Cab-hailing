package com.zhang.passengerclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class PassengerActivity extends AppCompatActivity implements BDLocationListener, View.OnClickListener, ServiceConnection {

    private String telNum, passengerName, from, destination, locInfo, driverNum;
    private String passengerCity, passengerAdd;
    private ImageButton btnBack, btnWBack, btnOBack;
    private Button btnFrom, btnDes;
    private TextView tvCar, tvBack, tvWBack, tvCancel, tvOBack;
    private TextView tvDriver, tvDriverNum, tvCallDriver;
    private boolean waitingOrder = false;

    private RelativeLayout layoutPassenger;
    private LinearLayout layoutWaiting;
    private LinearLayout layoutOrder;

    private Intent i;
    private static final int CHOOSE_FROM_REQUEST = 1;
    private static final int CHOOSE_DES_REQUEST = 2;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private MyLocationConfiguration.LocationMode mCurrentMode = null;
    private BitmapDescriptor mCurrentMarker = null;
    private LocationClient mLocationClient = null;
    private MyLocationData locData = null;
    private LatLng myLL;

    private boolean shareable = true;//判断服务能否开始或停止
    private boolean isBound = false;//判断服务是否绑定
    private MyService.Binder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_passenger_content);

        //获取乘客电话、名字
        telNum = getIntent().getStringExtra("telNum");
        passengerName = getIntent().getStringExtra("name");

        layoutPassenger = (RelativeLayout) findViewById(R.id.passenger_layout);
        layoutWaiting = (LinearLayout) findViewById(R.id.waiting_layout);
        layoutOrder = (LinearLayout) findViewById(R.id.order_layout);

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnWBack = (ImageButton) findViewById(R.id.btnWBack);
        btnOBack = (ImageButton) findViewById(R.id.btnOBack);
        btnFrom = (Button) findViewById(R.id.btnLoc);
        btnDes = (Button) findViewById(R.id.btnDes);
        tvCar = (TextView) findViewById(R.id.tvCar);
        tvBack = (TextView) findViewById(R.id.tvBack);
        tvWBack = (TextView) findViewById(R.id.tvWBack);
        tvOBack = (TextView) findViewById(R.id.tvOBack);
        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvDriver = (TextView) findViewById(R.id.tvDriver);
        tvDriverNum = (TextView) findViewById(R.id.tvDriverNum);
        tvCallDriver = (TextView) findViewById(R.id.tvCallDriver);

        btnBack.setOnClickListener(this);
        btnFrom.setOnClickListener(this);
        btnDes.setOnClickListener(this);
        tvCar.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        tvWBack.setOnClickListener(this);
        btnWBack.setOnClickListener(this);
        btnOBack.setOnClickListener(this);
        tvOBack.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        tvCallDriver.setOnClickListener(this);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
        mBaiduMap.setMyLocationConfigeration(config);

        //触摸地图时，取消中心固定在定位坐标
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                MyLocationConfiguration config = new MyLocationConfiguration(null, true, mCurrentMarker);
                mBaiduMap.setMyLocationConfigeration(config);
            }
        });

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

        //设置缩放级别
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));

        //进入主界面开启service
        if (shareable) {
            Intent i = new Intent(PassengerActivity.this, MyService.class);
            //启动service
            startService(i);
            //绑定service，利用onServiceConnected来获取socket传回的数据
            isBound = bindService(i, this, Context.BIND_AUTO_CREATE);

            shareable = false;//开启后 才可停止
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;


        //关闭程序是停止共享及服务
        stopService(new Intent(PassengerActivity.this, MyService.class));
        //解绑服务  如果没解绑会报错崩溃  若为绑定就解绑也会崩溃
        if (isBound) {
            unbindService(this);
            isBound = false;
        }
        shareable = true;

        waitingOrder = false;
    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        //客户城市
        passengerCity = bdLocation.getCity();
        //客户地址
        passengerAdd = bdLocation.getAddrStr();
        //客户坐标
        locInfo = (bdLocation.getLatitude()) + "@" + bdLocation.getLongitude();

        myLL = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

        // 构造定位数据
        locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();

        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                finish();
                waitingOrder = false;
                break;
            case R.id.btnWBack:
                layoutPassenger.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                layoutOrder.setVisibility(View.GONE);
                waitingOrder = false;
                break;
            case R.id.btnOBack:
                layoutPassenger.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                layoutOrder.setVisibility(View.GONE);
                waitingOrder = false;
                break;
            case R.id.tvBack:
                finish();
                waitingOrder = false;
                break;
            case R.id.tvWBack:
                layoutPassenger.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                layoutOrder.setVisibility(View.GONE);
                waitingOrder = false;
                break;
            case R.id.tvOBack:
                layoutPassenger.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                layoutOrder.setVisibility(View.GONE);
                waitingOrder = false;
                break;
            case R.id.btnLoc:
                //选择出发地
                i = new Intent(PassengerActivity.this, ChooseLocActivity.class);
                i.putExtra("address", passengerAdd);
                startActivityForResult(i, CHOOSE_FROM_REQUEST);
                break;
            case R.id.btnDes:
                //选择目的地
                i = new Intent(PassengerActivity.this, ChooseDesActivity.class);
                i.putExtra("city", passengerCity);
                startActivityForResult(i, CHOOSE_DES_REQUEST);
                break;
            case R.id.tvCar:
                //开始约车
                if (!(btnFrom.getText().toString().equals("出发地")) && !(btnDes.getText().toString().equals("目的地"))) {
                    //上传客户信息
                    String passengerInfo = "passenger" + "@" + passengerName + "@" + locInfo + "@" + destination + "@" + telNum;
                    if (binder != null) {
                        binder.uploadData(passengerInfo);
                    }
                    layoutPassenger.setVisibility(View.GONE);
                    layoutWaiting.setVisibility(View.VISIBLE);
                    layoutOrder.setVisibility(View.GONE);

                    waitingOrder = true;
                }else {
                    Toast.makeText(PassengerActivity.this,"请输入出发地和目的地！",Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.tvCancel:
                //取消约车
                String cancelInfo = "cancel" + "@" + passengerName + "@" + destination + "@" + telNum;
                if (binder != null) {
                    binder.uploadData(cancelInfo);
                }
                layoutPassenger.setVisibility(View.VISIBLE);
                layoutWaiting.setVisibility(View.GONE);
                layoutOrder.setVisibility(View.GONE);

                waitingOrder = false;
                break;
            case R.id.tvCallDriver:
                //呼叫司机
                Intent intentCall = new Intent();
                intentCall.setAction(Intent.ACTION_DIAL);
                intentCall.setData(Uri.parse("tel:" + driverNum));
                intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);

                waitingOrder = false;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CHOOSE_FROM_REQUEST:
                if (resultCode == RESULT_OK) {
                    from = data.getStringExtra("from");
                    btnFrom.setText(from);
                }
                break;
            case CHOOSE_DES_REQUEST:
                if (resultCode == RESULT_OK) {
                    destination = data.getStringExtra("destination");
                    btnDes.setText(destination);
                }
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        binder = (MyService.Binder) service;
        binder.getService().setCallback(new MyService.Callback() {
            @Override
            public void onDataChange(String data) {


                //获取出租车位置
                if (data.contains("taxi")) {
                    mBaiduMap.clear();

                    String[] locData = data.split("@");
                    if (locData.length == 3) {
                        double mLat = Double.parseDouble(locData[1]);
                        double mLon = Double.parseDouble(locData[2]);
                        //出租车坐标
                        LatLng taxiLL = new LatLng(mLat, mLon);
                        //距离
                        double distance = DistanceUtil.getDistance(myLL, taxiLL);
                        //显示1.5公里以内的车辆
                        if (distance < 1500) {
                            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.taxi);
                            MarkerOptions marker = new MarkerOptions().position(taxiLL).icon(bitmapDescriptor);
                            mBaiduMap.addOverlay(marker);
                        }
                    }
                } else if (waitingOrder && data.contains("order")){
                    //当用户在等待接单且回传来的接单信息与用户名字、目的地、电话相同时，可认为司机接的订单是该客户发出的

                    String[] tempO = data.split("@");

                    if (tempO[1].equals(passengerName) && tempO[2].equals(destination) && tempO[3].equals(telNum)){

                        String driverName = tempO[5];
                        driverNum = tempO[6];
                        tvDriver.setText("司机" + driverName + "已接单");
                        tvDriverNum.setText(driverNum);

                        layoutPassenger.setVisibility(View.GONE);
                        layoutWaiting.setVisibility(View.GONE);
                        layoutOrder.setVisibility(View.VISIBLE);

                        waitingOrder = false;
                    }
                }
            }
        });


    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
