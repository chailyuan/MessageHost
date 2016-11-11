package com.gaga.messagehost;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DignoseActivity extends AppCompatActivity {
    private Button button;

    private EditText phenomenon[];

    private EditText position[];

    private MyDataBase dbSingle = MyDataBase.GetDb(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dignose);

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

        phenomenon = new EditText[6];
        phenomenon[0] = (EditText)findViewById(R.id.phenomenon1);
        phenomenon[1] = (EditText)findViewById(R.id.phenomenon2);
        phenomenon[2] = (EditText)findViewById(R.id.phenomenon3);
        phenomenon[3] = (EditText)findViewById(R.id.phenomenon4);
        phenomenon[4] = (EditText)findViewById(R.id.phenomenon5);
        phenomenon[5] = (EditText)findViewById(R.id.phenomenon6);

        position = new EditText[4];
        position[0] = (EditText)findViewById(R.id.position1);
        position[1] = (EditText)findViewById(R.id.position2);
        position[2] = (EditText)findViewById(R.id.position3);
        position[3] = (EditText)findViewById(R.id.position4);

        button = (Button)findViewById(R.id.check_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().toString().equals("重新诊断")){
                    ((LinearLayout)findViewById(R.id.error_linear)).setVisibility(View.VISIBLE);
                    ((LinearLayout)findViewById(R.id.position_linear)).setVisibility(View.GONE);
                    button.setText("诊断");
                    return;
                }

                boolean success = onDignose();
                if (success){
                    ((LinearLayout)findViewById(R.id.error_linear)).setVisibility(View.GONE);
                    ((LinearLayout)findViewById(R.id.position_linear)).setVisibility(View.VISIBLE);

                    button.setText("重新诊断");
                    return;
                }else {

                }
            }
        });
    }

    //判断故障位置
    private boolean onDignose() {
        String phenomennonString[] = new String[6];

        Map<String, Integer> map = new HashMap<>();
        for (int phe=0;phe<6;phe++){
            phenomennonString[phe] = phenomenon[phe].getText().toString();
            if (!phenomennonString[phe].equals("")) {
                Cursor cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_DIGNOSE,
                        null,
                        MyDataBase.DIG_ALL_TITLE[phe] + " LIKE ?",
                        new String[]{"%" + phenomennonString[phe] + "%"},
                        null, null, null, null);
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        for (int j = 6; j < 10; j++) {
                            String str = cursor.getString(j);
                            Integer integer = map.get(str);
                            if (integer == null) {
                                map.put(str, 1);
                            } else {
                                map.put(str, integer.intValue() + 1);
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        Set<Map.Entry<String, Integer>> set = map.entrySet();
        Iterator<Map.Entry<String, Integer>> iterator = set.iterator();
        int max = -1,second = -1,third = -1,fouth = -1;
        String maxStr = "",secStr = "",thirdStr = "",fouthStr = "";
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            int tmp = entry.getValue().intValue();
            if (tmp > max) {
                fouth=third;third=second;second=max;max=tmp;
                fouthStr=thirdStr;thirdStr=secStr;secStr=maxStr;maxStr = entry.getKey();
            }
            else if (tmp < max&&tmp>second) {
                fouth=third;third=second;second=tmp;
                fouthStr=thirdStr;thirdStr=secStr;secStr=entry.getKey();
            }
            else if (tmp <second&&tmp>third) {
                fouth=third;third=tmp;
                fouthStr=thirdStr;thirdStr=entry.getKey();
            }
            else if (tmp <third &&tmp>fouth) {
                fouth=tmp;
                fouthStr= entry.getKey();
            }
        }
        position[0].setText(maxStr);
        position[1].setText(secStr);
        position[2].setText(thirdStr);
        position[3].setText(fouthStr);

        return true;
    }
}
