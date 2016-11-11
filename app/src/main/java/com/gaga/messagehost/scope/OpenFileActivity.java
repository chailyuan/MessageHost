package com.gaga.messagehost.scope;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.gaga.messagehost.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenFileActivity extends AppCompatActivity {

    private ListView lv;
    private List<HashMap<String,Object>> specs = new ArrayList<HashMap<String, Object>>();
    private SimpleAdapter adapter;

    private String currentDir="/";
    private String parentDir="/";
    public static String FILENAME="/";
    public static String LASTFILEFOLDER ="/";//上一次的文件夹名
    public static final String ROOTFOLDER = "/";

    //preference保存上次打开目录
    public static final String LASTFOLDER_PREF ="lastfolder_pred";//上一次的文件名

    private File[] files;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_choosefile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setLogo(R.drawable.ic_action_openfile);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.settings);
        }

        ((LinearLayout)findViewById(R.id.go_upper)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goUpperLayer();
                    }
                }
        );
        lv = (ListView)findViewById(R.id.spec_item_list);

        //获取上次打开的目录
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        LASTFILEFOLDER = preferences.getString(LASTFOLDER_PREF,ROOTFOLDER);
        File file = new File(LASTFILEFOLDER);
        if (file.exists()){
            parentDir = file.getParent();
            GetDirectory(true);
        }
        else
            GetDirectory(false);

        adapter = new SimpleAdapter(this,specs,R.layout.layout_choosefile_item,
                new String[]{"seq","name"},
                new int[]{R.id.spec_item_seq,R.id.spec_item_name});

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //判断点击是否是文件
                if(!files[position].isDirectory()){
                    //点击的是文件，则打开
                    LASTFILEFOLDER = currentDir;
                    FILENAME = currentDir+"/"+files[position].getName();
                    finish();
                }else{
                    parentDir = currentDir;
                    currentDir = currentDir+"/"+files[position].getName();

                    specs.clear();
                    GetDirectory(false);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LASTFOLDER_PREF,LASTFILEFOLDER);
        editor.commit();
    }

    private void GetDirectory(boolean first){
        if (first){
            currentDir = LASTFILEFOLDER;
        }
        File specItemDir = new File(currentDir);
        if (!specItemDir.exists()){
            specItemDir.getParent();
        }else{

            files = specItemDir.listFiles();

            int seq = 0;
            for (File spec:files){
//                if(!spec.isHidden()){
                    seq++;
                    HashMap<String ,Object> hashMap = new HashMap<String ,Object>();
                    hashMap.put("seq",seq);
                    hashMap.put("name",spec.getName());
                    specs.add(hashMap);
//                }
            }

        }
    }

    private void goUpperLayer(){
        currentDir = parentDir;
        if (!currentDir.equals("/")){
            File file = new File(currentDir);
            parentDir=file.getParent();
        }

        specs.clear();
        GetDirectory(false);
        adapter.notifyDataSetChanged();
    }
}
