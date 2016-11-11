package com.gaga.messagehost;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by re on 2016/5/2.
 */
public class RepairImportActivity extends AppCompatActivity {
    private EditText etCode,etRiqi,etXianxiang,etBanfa,etRenyuan;
    private Button btnSaveMess;
    private boolean inSelectDate = false;

    private MyDataBase dbSingle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_repairimport);
        dbSingle = MyDataBase.GetDb(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("维修情况录入");
        toolbar.setLogo(R.drawable.basket);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.goback_bg);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etCode = (EditText) findViewById(R.id.et_repairimportCode);
        etRiqi = (EditText) findViewById(R.id.et_RPriqi);
        etRiqi.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus&&!inSelectDate){
                    inSelectDate = true;
                    //调出日期选择界面
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog dialog = new DatePickerDialog(RepairImportActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            etRiqi.setText(year+"年"+(monthOfYear+1)+"月"+dayOfMonth+"日");
                            inSelectDate =false;
                        }
                    },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    dialog.show();
                }
            }
        });
        etXianxiang = (EditText) findViewById(R.id.et_RPxianxiang);
        etBanfa = (EditText) findViewById(R.id.et_RPbanfa);
        etRenyuan = (EditText) findViewById(R.id.et_RPrenyaun);

        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日");
        final Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String strDate = formatter.format(curDate);

        etRiqi.setText(strDate);

        ((TextView)findViewById(R.id.tv_recordCheck)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RepairImportActivity.this, RepairCheckActivity.class));
            }
        });

        btnSaveMess= (Button) findViewById(R.id.btn_RPSaveRPMess);
        btnSaveMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCode, strRiqi, strXianxiang, strBanfa, strRenyuan;
                strCode = etCode.getText().toString();
                strBanfa = etBanfa.getText().toString();
                strRenyuan = etRenyuan.getText().toString();
                strRiqi = etRiqi.getText().toString();
                strXianxiang = etXianxiang.getText().toString();

                if (strCode.equals("")
                        || strRiqi.equals("")
                        || strXianxiang.equals("")
                        || strBanfa.equals("")
                        || strRenyuan.equals("")) {
                    //不能为空，请输入完整
                    Toast.makeText(RepairImportActivity.this, "请输入完整", Toast.LENGTH_SHORT).show();
                } else {
                    //向数据库中插入新的维修信息,如果维修信息表中无当前条形码对应的信息怎么办

                    ContentValues cv = new ContentValues();
                    String strEtCode = etCode.getText().toString();
                    Cursor cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_REPARE, null, MyDataBase.RP_CODE + " =?", new String[]{strEtCode}, null, null, null, null);

                    if (cursor.getCount() != 0) {
                        cursor.moveToNext();
                        cv.put(MyDataBase.RP_ID_EQUIPMENTS, cursor.getString(cursor.getColumnIndex(MyDataBase.RP_ID_EQUIPMENTS)));
                        cv.put(MyDataBase.RP_TYPE_EQUIPMENTS, cursor.getString(cursor.getColumnIndex(MyDataBase.RP_TYPE_EQUIPMENTS)));
                        cv.put(MyDataBase.RP_POSITION, cursor.getString(cursor.getColumnIndex(MyDataBase.RP_POSITION)));
                        cv.put(MyDataBase.RP_CODE, strCode);
                        cv.put(MyDataBase.RP_DATE, strRiqi);
                        cv.put(MyDataBase.RP_PHENOMENON, strXianxiang);
                        cv.put(MyDataBase.RP_METHOD, strBanfa);
                        cv.put(MyDataBase.RP_SERVICE_PERSON, strRenyuan);
                        long a = dbSingle.dbWriter.insert(MyDataBase.TABLENAME_REPARE, null, cv);
                        if (a >= 0) {
                            Toast.makeText(RepairImportActivity.this, "插入成功", Toast.LENGTH_SHORT).show();
                            etXianxiang.setText("");
                            etBanfa.setText("");
                            etRenyuan.setText("");
                            etCode.setText("");
                        }
                        cursor.close();
                    } else {
                        cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_MAINTAIN, null, MyDataBase.MT_CODE + " =?", new String[]{strEtCode}, null, null, null, null);
                        if (cursor.getCount() == 0) {
                            //不存在
                            Toast.makeText(RepairImportActivity.this, "数据库中无对应条形码，请检查", Toast.LENGTH_SHORT).show();
                        } else {
                            cursor.moveToNext();
                            cv.put(MyDataBase.RP_ID_EQUIPMENTS, cursor.getString(cursor.getColumnIndex(MyDataBase.MT_ID_EQUIPMENTS)));
                            cv.put(MyDataBase.RP_CODE, strCode);
                            cv.put(MyDataBase.RP_DATE, strRiqi);
                            cv.put(MyDataBase.RP_PHENOMENON, strXianxiang);
                            cv.put(MyDataBase.RP_METHOD, strBanfa);
                            cv.put(MyDataBase.RP_SERVICE_PERSON, strRenyuan);
                            long a = dbSingle.dbWriter.insert(MyDataBase.TABLENAME_REPARE, null, cv);

                            if (a >= 0) {
                                Toast.makeText(RepairImportActivity.this, "插入成功", Toast.LENGTH_SHORT).show();
                                etXianxiang.setText("");
                                etBanfa.setText("");
                                etRenyuan.setText("");
                                etCode.setText("");
                            }
                        }
                        cursor.close();
                    }

                }
            }
        });

    }
}
