package com.gaga.messagehost;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by 临园 on 2016/4/24.
 * 导入导出界面
 */
public class InExportActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar = null;
    private TextView progressPer = null;
    private SynchronizationService.MyBinder myBinder = null;
    private SynchronizationService myService = null;
    private MyReceive denymicBroad = null;
    private boolean inProgressing = false;
    private Button btnExport,btnImport;

    private boolean wifiMode = false;
    private  String wifiIp;


    public class MyReceive extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            String str = intent.getStringExtra(SynchronizationService.BROADCAST_TITLE);
            String str2 = intent.getStringExtra(SynchronizationService.BROADCAST_PC);
            if (str!=null) {
                Toast.makeText(InExportActivity.this, str, Toast.LENGTH_SHORT).show();
                if (str.equals("PC已断开")){
                    btnExport.setEnabled(false);
                    btnImport.setEnabled(false);
                    inProgressing = false;
                }
                if (str.equals("PC已连接")&& wifiMode){
                    btnExport.setEnabled(true);
                    btnImport.setEnabled(true);
                }
            }

            if (str2!=null&&str2.equals("export")){
                btnExport.setEnabled(true);
                btnImport.setEnabled(false);
            }else if (str2 !=null&&str2.equals("import")){
                btnExport.setEnabled(false);
                btnImport.setEnabled(true);
            }


            int percent = intent.getIntExtra("percent",-1);
            if (percent==-1){

            }else {
                if (percent==100){
                    inProgressing = false;
                }else {
                    inProgressing = true;
                }
                progressBar.setProgress(percent);
                progressPer.setText(getString(R.string.export_progress)+percent+"%");
            }
        }
    }

    private class MyConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (SynchronizationService.MyBinder)service;
            myService =  myBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private  MyConn conn;


    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_imexport);

        //wifi
        WifiManager wifimanage=(WifiManager)getSystemService(Context.WIFI_SERVICE);//获取WifiManager
        if(!wifimanage.isWifiEnabled())  {
            wifimanage.setWifiEnabled(true);
        }
        WifiInfo wifiinfo= wifimanage.getConnectionInfo();

        wifiIp=intToIp(wifiinfo.getIpAddress());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(getString(R.string.export_title));
        toolbar.setLogo(R.drawable.publish);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.goback_bg);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnExport = (Button)findViewById(R.id.btn_Export);
        btnImport = (Button)findViewById(R.id.btn_Import);

        btnExport.setOnClickListener(this);
        btnImport.setOnClickListener(this);

        progressBar = (ProgressBar)findViewById(R.id.progressBarPort);
        progressPer = (TextView)findViewById(R.id.progressPercent);

        Intent i = getIntent();
        String d = i.getStringExtra("order");
//        System.out.println("传递过来的命令是："+d);

        if (d!=null&&d.equals("import")){
            btnExport.setEnabled(false);
            btnImport.setEnabled(true);
        }else if (d!=null&&d.equals("export")){
            btnExport.setEnabled(true);
            btnImport.setEnabled(false);
        }

        //启动服务
        Intent intentService = new Intent(this,SynchronizationService.class);
        startService(intentService);

        conn = new MyConn();

        bindService(intentService, conn, Context.BIND_AUTO_CREATE);

        denymicBroad = new MyReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SynchronizationService.BROADCAST_MESSAGE);
        registerReceiver(denymicBroad, intentFilter);

        BroadcastReceiver myreceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }

    @Override
    public void onBackPressed() {
        if (inProgressing){
            Toast.makeText(InExportActivity.this, R.string.export_warning_nochange,Toast.LENGTH_LONG).show();
            return;
        }
        super.onBackPressed();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_Export:
                //开始导出
                    if (myService!=null && myService.clientClass!=null)
                        myService.clientClass.InitSendCursor();
                break;
            case R.id.btn_Import:
                try {
                    if (myService!=null && myService.clientClass!=null)
                        myService.clientClass.SendManager(0,0,0,0,0,null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    @Override
    protected void onDestroy() {

        myService.quitSign = true;//退出标志有效

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("127.0.0.1",12321);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        unbindService(conn);
        stopService(new Intent(this,SynchronizationService.class));
        unregisterReceiver(denymicBroad);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.export_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getitemid()) {
//            case r.id.action_wifimode:
//                wifimode=!wifimode;
//                item.setchecked(wifimode);
//                if (wifimode){
//                    ((textview)findviewbyid(r.id.tv_wifiip)).settext("wifi模块ip地址是："+wifiip);
//                    ((linearlayout)findviewbyid(r.id.ll_wifiip)).setvisibility(view.visible);
//                    btnexport.setenabled(true);
//                    btnimport.setenabled(true);
//                }
//                else{
//                    btnexport.setenabled(false);
//                    btnimport.setenabled(false);
//                    ((linearlayout)findviewbyid(r.id.ll_wifiip)).setvisibility(view.gone);
//                }
//
//                break;
//        }

        return true;
    }
}
