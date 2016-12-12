package com.zhang.taxiclient;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class MyService extends Service {

    private Socket socket = null;
    private BufferedWriter writer = null;
    private BufferedReader reader = null;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return new Binder();
    }

    public class Binder extends android.os.Binder {

        public MyService getService() {
            return MyService.this;
        }

        //socket数据上传
        public void uploadData(String str) {

            if (writer != null) {
                try {
                    writer.write(str + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();

        //连接socket
        socketConnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //关闭socket
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //socket通信
    public void socketConnect() {

        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    socket = new Socket("192.168.0.105", 55555);//我自己的真机访问本地站点为192.168.0.105  虚拟机是10.0.3.2

                    if (socket.isConnected()) {
                        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        publishProgress(line);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                if (callback != null) {
                    callback.onDataChange(values[0]);
                }
                super.onProgressUpdate(values);
            }


        }.execute();
    }

    private Callback callback = null;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }

    //回调接口
    public interface Callback {
        void onDataChange(String data);
    }
}
