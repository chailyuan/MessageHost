package com.gaga.messagehost;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * create by 临园
 * 登录对话框
 */
public class LoginActivity extends AppCompatActivity {
    private String userName = null,password = null;
    private MyDataBase dbSingle = null;
    private EditText etName,etPass;
    SharedPreferences mySharedPreferences = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        etName =(EditText) findViewById(R.id.login_username);
        etPass = (EditText) findViewById(R.id.login_password);

        etPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==R.id.login || actionId == EditorInfo.IME_ACTION_GO){
                    OnLogin();
                    return true;
                }
                return false;
            }
        });

        mySharedPreferences= getSharedPreferences("test",Activity.MODE_PRIVATE);

        String name =mySharedPreferences.getString("name", "");
        etName.setText(name);



        MoveDataBase util = new MoveDataBase(this);
        // 判断数据库是否存在
        boolean dbExist = util.checkDataBase();

        if (dbExist) {
            Log.i("tag", "The database is exist.");
        } else {// 不存在就把raw里的数据库写入手机
            try {
                util.copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }


        //打开数据库
        dbSingle = MyDataBase.GetDb(this);

        InsertDataBase();

        findViewById(R.id.tv_forgotpassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //忘记密码
                Toast.makeText(LoginActivity.this, R.string.login_forget_method, Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.login_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnLogin();
            }
        });
    }

    private void OnLogin(){


        userName = etName.getText().toString();
        password = etPass.getText().toString();

        if (userName.equals("")){
            etName.setError(getString(R.string.login_username_notempty));
            return;
        }
        if (userName.equals("admin")&&password.equals("admin")){
            //默认的管理员账号登录，此账号登录只能进入注册界面
            ((EditText)findViewById(R.id.login_username)).setText("");
            ((EditText)findViewById(R.id.login_password)).setText("");
            startActivity(new Intent(LoginActivity.this, NewUserActivity.class));
            return;
        }
        Cursor lCursor = dbSingle.dbReader.rawQuery("SELECT * FROM "+ MyDataBase.TABLENAME_USER+" WHERE "+ MyDataBase.USER_NAME +"=?",
                new String[]{userName});
        if (lCursor.moveToNext()) {
            //表明存在数据
            int pass = lCursor.getColumnIndex(MyDataBase.USER_PASSWORD);
            String strValue=lCursor.getString(pass);
            lCursor.close();

            if(strValue.equals(password)){
                //密码正确
                //暂存当前用户名
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                //用putString的方法保存数据
                editor.putString("name", userName);
                //提交当前数据
                editor.commit();

                GotoMainActivity();
            }else{
                //密码错误
                Toast.makeText(LoginActivity.this, R.string.login_error_up, Toast.LENGTH_SHORT).show();
            }
        }else{
            //不存在当前用户
            Toast.makeText(LoginActivity.this, R.string.login_error_up, Toast.LENGTH_SHORT).show();
        }

    }

    //密码正确，转到主界面
    private void GotoMainActivity(){
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }
    //测试用，插入一条数据
    private void InsertDataBase(){
        Cursor lCursor = dbSingle.dbReader.rawQuery("SELECT * FROM "+ MyDataBase.TABLENAME_MAINTAIN+" WHERE "+ MyDataBase.MT_ID_EQUIPMENTS+"=?",
                new String[]{MyDataBase.MT_ID_EQUIPMENTS});

        if (!lCursor.moveToNext()){

            ContentValues values = new ContentValues();
            for (int i=0;i< MyDataBase.MT_ALL_TITLE.length;i++){
                values.put(MyDataBase.MT_ALL_TITLE[i], MyDataBase.MT_ALL_TITLE[i]);
            }
            values.put(MyDataBase.MT_CODE, MyDataBase.MT_CODE);
            dbSingle.dbWriter.insert(MyDataBase.TABLENAME_MAINTAIN, null, values);
        }
        lCursor.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
