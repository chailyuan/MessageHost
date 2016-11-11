package com.gaga.messagehost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by 临园 on 2016/4/20.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.mainactivity_name);
        toolbar.setLogo(R.drawable.logomine);
        setSupportActionBar(toolbar);

        ((LinearLayout)findViewById(R.id.ll_1)).setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.ll_2)).setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.ll_3)).setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.ll_4)).setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.ll_5)).setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.ll_6)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            //分别跳转到不同的ACTIVITY
            case R.id.ll_1:
                startActivity(new Intent(MainActivity.this,ShowConfigActivity.class));
            break;
            case R.id.ll_2:
                startActivity(new Intent(MainActivity.this,RepairMessageActivity.class));
            break;
            case R.id.ll_3:
                startActivity(new Intent(MainActivity.this,InExportActivity.class));
                break;
            case R.id.ll_4:
                startActivity(new Intent(MainActivity.this,StorageActivity.class));
                break;
            case R.id.ll_5:
                startActivity(new Intent(MainActivity.this,BugDigActivity.class));
                break;
            case R.id.ll_6:
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
