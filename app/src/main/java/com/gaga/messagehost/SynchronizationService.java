package com.gaga.messagehost;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SynchronizationService extends Service {

    public static final String BROADCAST_MESSAGE = "com.gaga.messagehost.servicebroadcast";
    public static final String BROADCAST_TITLE = "fromservice";
    public static final String BROADCAST_PC = "frompc";

    public boolean quitSign = false;//退出标志

    public PcClient clientClass = null;
    private MyBinder myBinder = new MyBinder();
    public SynchronizationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
    public class MyBinder extends Binder{
        SynchronizationService getService(){
            return SynchronizationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread() {
            @Override
            public void run() {
                super.run();
                ServerSocket serverSocket = null;

                try {
                    serverSocket = new ServerSocket(12321);
                    System.out.println("服务已启动，正在等待连接");
                    while (!quitSign) {
                        Socket socket = serverSocket.accept();
                        System.out.println("客户端已连接");

                        clientClass =new PcClient(socket,SynchronizationService.this);
                        clientClass.start();


                        //动态发送广播给导入导出activity，用他来进行消息的显示
                        Intent intent = new Intent(BROADCAST_MESSAGE);
                        intent.putExtra(BROADCAST_TITLE, "PC已连接");
                        sendBroadcast(intent);

                    }
                    System.out.println("服务监听已退出");
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (serverSocket!=null){
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }.start();
    }
    //显示连接信息
    private void ShowMessage(){

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    //客户端类，指的是PC端
    public class PcClient extends Thread {

        private Context context;
        private MyDataBase dbSingle = null;
        private Cursor sendCursorA = null;
        private Cursor sendCursorB = null;
        private Cursor sendCursorC = null;
        private int CountA = 0;
        private int CountB = 0;
        private int CountC = 0;

        private Socket client = null;
        private InputStream inStream;
        private OutputStream outStream;
        private BufferedReader bufferedReader;
        private PrintWriter outWriter;
        private JSONObject jsonAsk = null;
        private JSONObject jsonConfig = null;
        private JSONObject jsonRepair = null;


        public PcClient(Socket socket,Context context) {

            this.context = context;
            this.client = socket;

            dbSingle = MyDataBase.GetDb(context);

            try {
                inStream = client.getInputStream();
                outStream = client.getOutputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inStream,"UTF-8"));
                outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream,"UTF-8")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            super.run();

            String str = null;
            try {
                while ((str = bufferedReader.readLine())!=null){
                    ReceiveManager(str);
                }
                } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            clientClass = null;
                            if (sendCursorA!=null){
                                sendCursorA.close();
                                sendCursorA=null;
                            }
                            if (sendCursorB!=null){
                                sendCursorB.close();
                                sendCursorB=null;
                            }

                            Intent intent = new Intent(BROADCAST_MESSAGE);
                            intent.putExtra(BROADCAST_TITLE, "PC已断开");
                            sendBroadcast(intent);

                            if (!client.isInputShutdown())
                                client.shutdownInput();
                            if (!client.isOutputShutdown())
                                client.shutdownOutput();
                            if (client.isConnected())
                                client.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
        }

        public void InitSendCursor(){
            sendCursorA = MyDataBase.GetDb(context).dbReader.rawQuery("SELECT * FROM "+MyDataBase.TABLENAME_MAINTAIN,null);
            sendCursorB = MyDataBase.GetDb(context).dbReader.rawQuery("SELECT * FROM "+MyDataBase.TABLENAME_REPARE,null);
            sendCursorC = MyDataBase.GetDb(context).dbReader.rawQuery("SELECT * FROM "+MyDataBase.TABLENAME_DIGNOSE,null);
            CountA =sendCursorA.getCount();
            CountB =sendCursorB.getCount();
            CountC =sendCursorC.getCount();


            try {
                SendManager(2, 0, CountA+CountB+CountC, 0, 0, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        /**
         * 接收消息管理
         * @param str：接收到JSON字符串
         *
         */
        public void ReceiveManager(String str){
            try {
                JSONObject jsonObject = new JSONObject(str);
                String order = jsonObject.getString("order");
                switch (order){
                    case "ask":
                        break;
                    case "import":
                        ImportDataBase(jsonObject);
                        break;
                    case "export":
                        int currentNum = jsonObject.getInt(MyDataBase.JSON_CURRENTNUM);

                        Intent intent = new Intent(BROADCAST_MESSAGE);
                        intent.putExtra("percent", currentNum*100/(CountA+CountB+CountC));
                        sendBroadcast(intent);

                        if (currentNum<=CountA && CountA>0){
                            sendCursorA.moveToNext();
                            SendManager(2,1,CountA+CountB+CountC,currentNum,1,sendCursorA);
                        }else if (currentNum>CountA&&currentNum<=CountA+CountB&&CountB>0){
                            if (sendCursorA!=null){
                                sendCursorA.close();
                                sendCursorA = null;
                            }
                            sendCursorB.moveToNext();
                            SendManager(2,1,CountA+CountB+CountC,currentNum,2,sendCursorB);
                        }else if (currentNum>CountA+CountB && currentNum<=CountA+CountB+CountC&&CountC>0){
                            if (sendCursorB!=null){
                                sendCursorB.close();
                                sendCursorB = null;
                            }
                            sendCursorC.moveToNext();
                            SendManager(2,1,CountA+CountB+CountC,currentNum,3,sendCursorB);
                        }else if (currentNum>CountA+CountB+CountC){
                            if (sendCursorC!=null){
                                sendCursorC.close();
                                sendCursorC = null;
                            }
                            SendManager(2,2,0,0,0,null);
                        }
                        break;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * 导入数据库管理
         * @param jsonObject
         */
        private void ImportDataBase(JSONObject jsonObject){
            try {
                int totalnum = jsonObject.getInt("totalnum");
                int current = jsonObject.getInt("currentnum");
                int sign = jsonObject.getInt("sign");

                switch (sign){
                    case 0:
                        //请求导入数据，sign=0是开始的标志，此时清空数据库
                        if (current==-1){
                            //清空数据库,清空两个表，第三个用户表留着
                            dbSingle.dbWriter.delete(MyDataBase.TABLENAME_REPARE,null,null);
                            dbSingle.dbWriter.delete(MyDataBase.TABLENAME_MAINTAIN,null,null);
                            dbSingle.dbWriter.delete(MyDataBase.TABLENAME_DIGNOSE,null,null);
                            //同时把递增列归零
                            ContentValues cv = new ContentValues();
                            cv.put("seq", 0);
                            dbSingle.dbWriter.update("sqlite_sequence",cv,"name = ? OR name = ?",new String[]{MyDataBase.TABLENAME_MAINTAIN, MyDataBase.TABLENAME_REPARE});
                        }
                        SendManager(1,sign,totalnum,current+1,0,null);
                        break;
                    case 1:
                        //收到有效数据，要保存

                        Intent intent = new Intent(BROADCAST_MESSAGE);
                        intent.putExtra("percent", current*100/totalnum);
                        sendBroadcast(intent);

                        InsertDateBase(totalnum,current,jsonObject);
                        SendManager(1,sign,totalnum,current,0,null);
                        break;
                    case 2:
                        //发送结束
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private void InsertDateBase(int totalnum,int current,JSONObject jsonObject){
            ContentValues cv = new ContentValues();
            Cursor cursor = null;
            try {
                String tableName = jsonObject.getString("tablename");
                switch (tableName){
                    case MyDataBase.TABLENAME_MAINTAIN:
                        //每一列是唯一的
                        for (int i=0;i< MyDataBase.MT_ALL_TITLE.length;i++){
                            cv.put(MyDataBase.MT_ALL_TITLE[i],jsonObject.getString(MyDataBase.MT_ALL_TITLE[i]));
                        }

                        cursor = dbSingle.dbReader.rawQuery("SELECT * FROM "+ MyDataBase.TABLENAME_MAINTAIN+" WHERE "+ MyDataBase.MT_ID_EQUIPMENTS+"=?",
                                new String[]{jsonObject.getString(MyDataBase.MT_ID_EQUIPMENTS)});
                        if(cursor.moveToNext()){
                            dbSingle.dbWriter.update(MyDataBase.TABLENAME_MAINTAIN,cv, MyDataBase.MT_ID_EQUIPMENTS+"=?",new String[]{jsonObject.getString(MyDataBase.MT_ID_EQUIPMENTS)});
                        }else {
                            dbSingle.dbWriter.insert(MyDataBase.TABLENAME_MAINTAIN,null,cv);
                        }

                        break;

                    case MyDataBase.TABLENAME_REPARE:
                        //每一列不是唯一的
                        for (int i=0;i< MyDataBase.RP_ALL_TITLE.length;i++){
                            cv.put(MyDataBase.RP_ALL_TITLE[i],jsonObject.getString(MyDataBase.RP_ALL_TITLE[i]));
                        }
                        dbSingle.dbWriter.insert(MyDataBase.TABLENAME_REPARE, null, cv);
                        break;

                    case MyDataBase.TABLENAME_DIGNOSE:
                        for (int i=0;i< MyDataBase.DIG_ALL_TITLE.length;i++){
                            cv.put(MyDataBase.DIG_ALL_TITLE[i],jsonObject.getString(MyDataBase.DIG_ALL_TITLE[i]));
                        }
                        dbSingle.dbWriter.insert(MyDataBase.TABLENAME_DIGNOSE, null, cv);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                cv.clear();

                if (cursor!=null){
                    cursor.close();
                    cursor=null;
                }
            }
        }
        /**
         * 发送信息管理
         * @param cursor:数据库的指针
         * @param order：标明是 0：ASK;1：IMPORT;2：EXPORT
         * @param tableName 0,无效；1，MT；2,RP；3,dig
         *
         */
        public  void SendManager(int order,int sign,int totalnum,int currentnum,int tableName,Cursor cursor) throws JSONException {
//            System.out.println("进入发送管理器");
            JSONObject jsonMain = new JSONObject();

            switch (order){
                case 0:
                    jsonMain.put(MyDataBase.JSON_ORDER,"ask");
                    break;
                case 1:
                    jsonMain.put(MyDataBase.JSON_ORDER,"import");
                    jsonMain.put(MyDataBase.JSON_SIGN,sign);
                    jsonMain.put(MyDataBase.JSON_TOTALNUM,totalnum);
                    jsonMain.put(MyDataBase.JSON_CURRENTNUM,currentnum+1);
                    break;
                case 2:
                    jsonMain.put(MyDataBase.JSON_ORDER,"export");
                    jsonMain.put(MyDataBase.JSON_SIGN,sign);
                    jsonMain.put(MyDataBase.JSON_TOTALNUM,totalnum);
                    jsonMain.put(MyDataBase.JSON_CURRENTNUM,currentnum);
                    if (cursor==null){

                    }else {
                        if (tableName==0){
//                            throw new Exception("表名不能无效");
                        }else if (tableName==1){
                            jsonMain.put(MyDataBase.JSON_TABLENAME,MyDataBase.TABLENAME_MAINTAIN);
                            for (int i=0;i<MyDataBase.MT_ALL_TITLE.length;i++){
                                jsonMain.put(MyDataBase.MT_ALL_TITLE[i],cursor.getString(cursor.getColumnIndex(MyDataBase.MT_ALL_TITLE[i])));
                            }
                        }else if (tableName==2){
                            jsonMain.put(MyDataBase.JSON_TABLENAME,MyDataBase.TABLENAME_REPARE);
                            for (int i=0;i<MyDataBase.RP_ALL_TITLE.length;i++){
                                jsonMain.put(MyDataBase.RP_ALL_TITLE[i],cursor.getString(cursor.getColumnIndex(MyDataBase.RP_ALL_TITLE[i])));
                            }
                        }else if (tableName == 3){
                            jsonMain.put(MyDataBase.JSON_TABLENAME,MyDataBase.TABLENAME_DIGNOSE);
                            for (int i=0;i<MyDataBase.DIG_ALL_TITLE.length;i++){
                                jsonMain.put(MyDataBase.DIG_ALL_TITLE[i],cursor.getString(cursor.getColumnIndex(MyDataBase.DIG_ALL_TITLE[i])));
                            }
                        }

                    }
                    break;
            }

//自己调试的版本，带\n
//            SendMessage(jsonMain.toString()+"\n");
//给温龙飞的版本，不带\n
            SendMessage(jsonMain.toString());
        }
        //发送信息
        public void SendMessage(String str){
            outWriter.write(str);
            outWriter.flush();
        }
    }
}
