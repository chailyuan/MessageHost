package com.gaga.messagehost;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by 临园 on 2016/4/24.
 */
public class RepairMessageActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_repairmess);
        ((Button)findViewById(R.id.btn_RepairCheck)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_RepairImport)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_RepairHistory)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_RepairCheck:
                startActivity(new Intent(RepairMessageActivity.this,RepairCheckActivity.class));
                break;
            case R.id.btn_RepairImport:
                startActivity(new Intent(RepairMessageActivity.this,RepairImportActivity.class));
                break;
            case R.id.btn_RepairHistory:
                startActivity(new Intent(RepairMessageActivity.this,RepairHistoryActivity.class));
                break;
        }

    }
}
