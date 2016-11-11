package com.gaga.messagehost;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 临园 on 2016/4/24.
 * 出入库管理
 */
public class StorageActivity extends AppCompatActivity implements View.OnClickListener{
    private MyDataBase dbSingle = null;


    private EditText etStorageCode = null;
    private EditText etStoragePosition = null;
    private EditText etStorageStyle = null;
    private Button btnSave = null;

    private RadioGroup genderGroup=null;
    private RadioButton inRadio,outRadio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_storage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("出入库管理");
        toolbar.setLogo(R.drawable.sign_in);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.goback_bg);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dbSingle = MyDataBase.GetDb(this);

        etStorageCode = (EditText)findViewById(R.id.et_storageCode);
        etStoragePosition = (EditText)findViewById(R.id.et_StoragePosition);
        etStorageStyle = (EditText)findViewById(R.id.et_StorageStyle);
        genderGroup = (RadioGroup)findViewById(R.id.radio_group);
        inRadio = (RadioButton)findViewById(R.id.radio_inStorage);
        outRadio = (RadioButton)findViewById(R.id.radio_outStorage);
        btnSave = (Button)findViewById(R.id.btn_StorageSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //需要在两个数据库中修改，第一个库是装备状态，第二个库是装备位置和装备类别
                ContentValues cv = new ContentValues();
                cv.put(MyDataBase.RP_POSITION, etStoragePosition.getText().toString());
                cv.put(MyDataBase.RP_TYPE_EQUIPMENTS, etStorageStyle.getText().toString());
                String whereClause = MyDataBase.RP_CODE+"=?";

                dbSingle.dbWriter.update(MyDataBase.TABLENAME_REPARE, cv, whereClause, new String[]{etStorageCode.getText().toString()});

                cv.clear();
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日");
                final Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String strDate = formatter.format(curDate);

                if (inRadio.isChecked()){
                    cv.put(MyDataBase.MT_INSTORAGE,strDate);
                }else {
                    cv.put(MyDataBase.MT_OUTSTORAGE,strDate);
                }
                cv.put(MyDataBase.MT_STATUS,genderGroup.getCheckedRadioButtonId()==R.id.radio_inStorage?"库存":"在用");
                whereClause = MyDataBase.MT_CODE+"=?";
                dbSingle.dbWriter.update(MyDataBase.TABLENAME_MAINTAIN, cv, whereClause, new String[]{etStorageCode.getText().toString()});
                Toast.makeText(StorageActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
            }
        });
        etStorageCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onCheck();
                return true;
            }
        });

    }

    private boolean onCheck(){
        //回车事件
        String code = etStorageCode.getText().toString().replace("\n", "");
        etStorageCode.setText(code);

        if (code.equals("")){
            //返回的数据是空的
            Toast.makeText(this,"请输入条形码！",Toast.LENGTH_SHORT).show();
            etStorageStyle.setEnabled(false);
            etStoragePosition.setEnabled(false);
            btnSave.setEnabled(false);

        } else {
            //返回的数据不空
            //隐藏键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etStorageCode.getWindowToken(), 0);


            Cursor cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_MAINTAIN,
                    null,
                    MyDataBase.MT_CODE + " =?",
                    new String[]{code},
                    null, null, null, null);

            if (cursor.moveToNext()) {
                //查询到了数据
                etStorageStyle.setEnabled(true);
                etStoragePosition.setEnabled(true);
                btnSave.setEnabled(true);
                inRadio.setEnabled(true);
                outRadio.setEnabled(true);


                String status = cursor.getString(cursor.getColumnIndex(MyDataBase.MT_STATUS));

                cursor.close();

                cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_REPARE, null, MyDataBase.RP_CODE + " =?", new String[]{code}, null, null, null, null);
                if (cursor.moveToNext()) {
                    String position = cursor.getString(cursor.getColumnIndex(MyDataBase.RP_POSITION));
                    String type = cursor.getString(cursor.getColumnIndex(MyDataBase.RP_TYPE_EQUIPMENTS));
                    etStoragePosition.setText(position);
                    etStorageStyle.setText(type);
                }

                if (status.equals("在用")){
                    outRadio.setChecked(true);
                }else if (status.equals("库存")){
                    inRadio.setChecked(true);
                }
            }else {
                etStorageStyle.setEnabled(false);
                etStoragePosition.setEnabled(false);
                btnSave.setEnabled(false);
                inRadio.setEnabled(false);
                outRadio.setEnabled(false);

                Toast.makeText(this,"未查询到数据！请检查",Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
        return true;



    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER){
            onCheck();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
        }
    }
}
