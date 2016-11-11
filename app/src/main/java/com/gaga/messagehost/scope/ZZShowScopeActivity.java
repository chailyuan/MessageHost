package com.gaga.messagehost.scope;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.gaga.messagehost.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ZZShowScopeActivity extends Activity {
    private static final String PREF_SCREEN = "pref_screen";
    private static final String PREF_AUTO = "pref_auto";
    private static final String DEAL_DATA = "dealdata";

    private static final String STATE = "state";

    private static final String SINGLE = "single";
    private static final String TIMEBASE = "timebase";

    private static final String INDEX = "index";
    private static final String LEVEL = "level";

    protected static final int DEFAULT_TIMEBASE = 2;
    protected int timebase;

    public static final int GONEXTPAGE = 1;
    public static final int GOBACKPAGE = 2;
    public static final int GOCURRPAGE = 3;

    private ZZScope zzscope;
    private ZZXScale zzxscale;
    public ZZYScale zzyscale;

    private Toast toast;
    private SubMenu submenu_tb;

    private boolean screen;
    private boolean dealdata;//是否处理读取的数据
    //read the file
    //要打开的文件
    /*
    要打开的文件
    文件长度
    当前指向位置
    每一页显示的个数
     */
    private File fileToOpen = null;
    private RandomAccessFile randomAccessFile = null;
    protected long fileLength = 0;
    protected long curOffset = 0;//当前读取的偏移量
    public static int numEveryPage=500;//每页显示的数量
    public int numRead = 0;

    //读取的数据放在这里
    protected byte[] data;

    private Handler handler = null;
    private Runnable runnable= null;
    private int sleepTime = 5;
    private boolean inAutoThread = false;
    private int Sleep_Time[] = {1,2,5,10,20,50};
    private static final String strings[] =
            {"1 s", "2 s", "5 s",
                    "10 s", "20 s", "50 s"};

    // On create

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scopemainzz);

        zzscope = (ZZScope) findViewById(R.id.zzscope);
        zzxscale = (ZZXScale) findViewById(R.id.zzxscale);
        zzyscale = (ZZYScale) findViewById(R.id.zzyscale);

        // Get action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Set short title
        if (actionBar != null)
            actionBar.setTitle(R.string.short_name);

        //init data
        data = new byte[1024];

        if (zzscope != null) {
            zzscope.main = this;
        }
        if (zzxscale!=null){
            zzxscale.main=this;
        }

        // Set timebase index
        timebase = DEFAULT_TIMEBASE;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,sleepTime*1000);
                inAutoThread = true;
                readDataSetting(GONEXTPAGE);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item;

        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.main, menu);


        // Timebase
        item = menu.findItem(R.id.countbase);
        if (timebase != DEFAULT_TIMEBASE) {
            if (item.hasSubMenu()) {
                submenu_tb = item.getSubMenu();
                clearLast(submenu_tb, DEFAULT_TIMEBASE);
                item = submenu_tb.getItem(timebase);
                setTimebase(timebase,false);
                if (item != null)
                    item.setChecked(true);
            }
        }

        item = menu.findItem(R.id.automove);
        item.setIcon(inAutoThread ? R.drawable.ic_action_autofalse :
                R.drawable.ic_action_autotrue);

        item = menu.findItem(R.id.action_dealdata);
        if (dealdata){
            item.setChecked(true);
        }else {
            item.setChecked(false);
        }

        return true;
    }

    // Restore state
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved state bundle

        Bundle bundle = savedInstanceState.getBundle(STATE);

        inAutoThread = bundle.getBoolean(SINGLE);

        // Timebase
        timebase = bundle.getInt(TIMEBASE, DEFAULT_TIMEBASE);
        setTimebase(timebase, false);


        // Start
        zzxscale.postInvalidate();

        // Index

        zzscope.index = bundle.getFloat(INDEX, 0);

        // Level

        zzyscale.index = bundle.getFloat(LEVEL, 0);
        zzyscale.postInvalidate();
    }

    // Save state

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // State bundle

        Bundle bundle = new Bundle();

        // Timebase
        bundle.putInt(TIMEBASE, timebase);

        bundle.putBoolean(SINGLE,inAutoThread);

        // Index
        bundle.putFloat(INDEX, zzscope.index);

        // Level
        bundle.putFloat(LEVEL, zzyscale.index);

        // Save bundle
        outState.putBundle(STATE, bundle);
    }

    // On options item

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get id



        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                this.finish();
                break;


            //ic_bigger
            case R.id.bigger:
                numEveryPage -= 20;
                if (numEveryPage<20)
                    numEveryPage = 20;
                zzxscale.postInvalidate();
                zzscope.postInvalidate();
                break;

            // Trigger
            //ic_smaller
            case R.id.smaller:
                numEveryPage += 20;
                if (numEveryPage>500)
                    numEveryPage = 500;
                readDataSetting(GOCURRPAGE);
                zzxscale.postInvalidate();
                zzscope.postInvalidate();
                break;
            // Single shot

            case R.id.automove:
                inAutoThread = !inAutoThread;
                item.setIcon(inAutoThread ? R.drawable.ic_action_autofalse :
                        R.drawable.ic_action_autotrue);
                showToast(inAutoThread ? R.string.pref_auto : R.string.pref_menual);

                if (inAutoThread){
                    handler.postDelayed(runnable,sleepTime*1000);
                    //禁用其他菜单
                }else {
                    handler.removeCallbacks(runnable);
                    //启用其他菜单
                }
                break;

            // Timebase
            case R.id.countbase:
                if (item.hasSubMenu())
                    submenu_tb = item.getSubMenu();
                break;

            // 0.1 ms
            case R.id.tb_1s:
                clearLast(submenu_tb, timebase);
                timebase = 0;
                item.setChecked(true);
                setTimebase(timebase, true);
                break;

            // 0.2 ms
            case R.id.tb_2s:
                clearLast(submenu_tb, timebase);
                timebase = 1;
                item.setChecked(true);
                setTimebase(timebase, true);
                break;

            // 0.5 ms
            case R.id.tb_5s:
                clearLast(submenu_tb, timebase);
                timebase = 2;
                item.setChecked(true);
                setTimebase(timebase, true);
                break;

            // 1.0 ms
            case R.id.tb_10s:
                clearLast(submenu_tb, timebase);
                timebase = 3;
                item.setChecked(true);
                setTimebase(timebase, true);
                break;

            // 2.0 ms
            case R.id.tb_20s:
                clearLast(submenu_tb, timebase);
                timebase = 4;
                item.setChecked(true);
                setTimebase(timebase, true);
                break;

            // 5.0 ms
            case R.id.tb_50s:
                clearLast(submenu_tb, timebase);
                timebase = 5;
                item.setChecked(true);
                setTimebase(timebase, true);
                break;




            // Clear
            case R.id.clear:
                openFile();
                break;

            // Left
            case R.id.goleft:
                readDataSetting(GOBACKPAGE);
                break;

            // Right
            case R.id.goright:
                readDataSetting(GONEXTPAGE);
                break;

            // Start
            case R.id.gotostart:
                curOffset = 0;
                readDataSetting(GOCURRPAGE);
                break;

            // End
            case R.id.gotoend:
                curOffset = fileLength-numEveryPage;
                readDataSetting(GOCURRPAGE);
                break;

            // openfile
            case R.id.action_getfile:
                return onOpenFileClick(item);
            //dealdata
            case R.id.action_dealdata:
                dealdata = !dealdata;
                item.setChecked(dealdata);
                break;

            // Settings
            case R.id.action_settings:
                return onSettingsClick(item);

            default:
                return false;
        }

        return true;
    }

    // On settings click

    private boolean onSettingsClick(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    // On spectrum click

    private boolean onOpenFileClick(MenuItem item) {
        Intent intent = new Intent(this, OpenFileActivity.class);
        startActivity(intent);

        return true;
    }

    // Clear last

    void clearLast(SubMenu submenu, int timebase) {
        // Clear last submenu item tickbox

        if (submenu != null) {
            MenuItem last = submenu.getItem(timebase);

            if (last != null)
                last.setChecked(false);
        }
    }

    // Set timebase

    void setTimebase(int timebase, boolean show) {
        sleepTime = Sleep_Time[timebase];
        // Show timebase
        if (show)
            showTimebase(timebase);
    }

    // Show timebase
    void showTimebase(int timebase) {
        String text = "Timebase: " + strings[timebase];

        showToast(text);
    }

    // Show toast.

    void showToast(int key) {
        Resources resources = getResources();
        String text = resources.getString(key);

        showToast(text);
    }

    void showToast(String text) {
        // Cancel the last one

        if (toast != null)
            toast.cancel();

        // Make a new one
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    //open file
    protected boolean openFile(){
        if (OpenFileActivity.FILENAME.equals(OpenFileActivity.ROOTFOLDER)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast( R.string.error_choosefile);
                }
            });
            fileToOpen = null;
            randomAccessFile=null;
            return false;
        }

        fileToOpen = new File(OpenFileActivity.FILENAME);
        if (!fileToOpen.exists()){
            //文件不存在，请重新选择
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(R.string.error_fileNotExist);
                }
            });
            fileToOpen= null;
            randomAccessFile=null;
            return false;
        }
        //判断是否是文件
        if (!fileToOpen.isFile()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(R.string.error_filewrong);
                }
            });
            fileToOpen= null;
            randomAccessFile=null;
            return false;
        }
        //判断文件是否为空
        long fileSize = fileToOpen.length();
        if (fileSize<=0){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(R.string.error_fileEmpty);
                }
            });
            fileToOpen= null;
            randomAccessFile=null;
            return false;
        }

        fileLength = fileSize;
        curOffset = 0;

        try {
            randomAccessFile = new RandomAccessFile(fileToOpen,"r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileToOpen = null;
            randomAccessFile=null;
            return  false;
        }

        readDataSetting(GOCURRPAGE);
        return  true;
    }
    protected boolean isFileOpened(){

        if (fileToOpen == null|| randomAccessFile == null)
            return false;
        else
            return true;
    }
    //read data
    /*
    * direction: int 方向，1向前；2向后,其他不动
     */
    protected void readDataSetting(int direction){
        if (direction == GONEXTPAGE ){
            curOffset+=numEveryPage;
        }
        else if (direction==GOBACKPAGE){
            curOffset-=numEveryPage;
        }
        if (curOffset<0){
            curOffset=0;
            showToast(R.string.error_firstpage);
        }
        if (curOffset>=fileLength-numEveryPage){
            showToast(R.string.error_lastpage);
            curOffset = fileLength-numEveryPage;
            if (inAutoThread){
                inAutoThread = false;
                handler.removeCallbacks(runnable);
            }
        }
        readData();

    }
    private void readData(){
        try {
            randomAccessFile.seek(curOffset);
            numRead = randomAccessFile.read(data,0,numEveryPage+1);

            if (dealdata){
                dealTheData();
            }

            zzscope.postInvalidate();
            zzxscale.postInvalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dealTheData() {
        for (int i=0;i<numRead;i++){
            //deal data
            data[i] /= 2;
        }
    }

    // On start

    @Override
    protected void onStart() {
        super.onStart();
    }

    // On Resume

    @Override
    protected void onResume() {
        super.onResume();

        // Get preferences
        getPreferences();

        // Open the file
        if (!isFileOpened()){
            if (!openFile()){
                Intent intent = new Intent(this, OpenFileActivity.class);
                startActivity(intent);
                return;
            }
        }
        readDataSetting(GOCURRPAGE);
        //自动运行
        if (inAutoThread){
            handler.postDelayed(runnable,sleepTime*1000);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(runnable);
        // Save preferences

        savePreferences();

    }

    // On stop

    @Override
    protected void onStop() {
        super.onStop();
    }

    // Get preferences

    void getPreferences() {
        // Load preferences

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        // Set preferences
        screen = preferences.getBoolean(PREF_SCREEN, false);
        inAutoThread = preferences.getBoolean(PREF_AUTO,false);
        dealdata = preferences.getBoolean(DEAL_DATA,false);
        timebase = preferences.getInt(TIMEBASE,2);

        String string = preferences.getString("pref_inputlength","500");
        if (string!=null && !string.equals(""))
            numEveryPage = Integer.parseInt(string);
        else
            numEveryPage = 500;

        //最大值500
        numEveryPage = numEveryPage>500?500:numEveryPage;

        zzxscale.postInvalidate();
        zzscope.postInvalidate();

        // Check screen

        if (screen) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    // Save preferences

    void savePreferences() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pref_inputlength", numEveryPage+"");
        editor.putInt(TIMEBASE,timebase);
        editor.putBoolean(DEAL_DATA,dealdata);
        editor.commit();

        // TODO
    }

    // Show alert

    void showAlert(int appName, int errorBuffer) {
        // Create an alert dialog builder

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        // Set the title, message and button
        builder.setTitle(appName);
        builder.setMessage(errorBuffer);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // Dismiss dialog
                        dialog.dismiss();
                    }
                });
        // Create the dialog
        AlertDialog dialog = builder.create();

        // Show it
        dialog.show();
    }
}
