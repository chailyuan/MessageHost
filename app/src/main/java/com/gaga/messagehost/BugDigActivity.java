package com.gaga.messagehost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.gaga.messagehost.scope.ZZShowScopeActivity;

public class BugDigActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_dig);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(R.string.bugdignose_name);
        setTitle(R.string.repairmessactivity_name);
//        toolbar.setSubtitle("linyuan");
        toolbar.setLogo(R.drawable.ic_main_scope);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.goback_bg);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ((LinearLayout)findViewById(R.id.ll_1)).setOnClickListener(this);
        ((LinearLayout)findViewById(R.id.ll_2)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_1:
                startActivity(new Intent(BugDigActivity.this,DignoseActivity.class));
                break;
            case R.id.ll_2:
                startActivity(new Intent(BugDigActivity.this,ZZShowScopeActivity.class));
                break;
        }
    }
}
