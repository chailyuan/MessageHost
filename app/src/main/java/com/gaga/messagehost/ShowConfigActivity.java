package com.gaga.messagehost;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 临园 on 2016/4/24.
 * 第一项功能，电脑配置信息维护界面
 */
public class ShowConfigActivity extends AppCompatActivity {

    private MyDataBase dbSingle = null;
    private EditText etShowCode;
    private ListView lvShow;
    private List<Map<String, Object>> mData;
    private boolean focus_etShowCode = true;

    private String tmpString;

    MyAdapter myAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        setContentView(R.layout.layout_showconfig);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(R.string.showconfigactivity_name);
//        toolbar.setSubtitle("linyuan");
        toolbar.setLogo(R.drawable.edit);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.goback_bg);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        dbSingle = MyDataBase.GetDb(this);

        if (mData!=null){
            mData.clear();
            mData = null;
        }
        mData = InitmData();//初始化

        lvShow = (ListView) findViewById(R.id.lv_showconfig);

        myAdapter = new MyAdapter(this);
        lvShow.setAdapter(myAdapter);
        lvShow.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //长按编辑相关选项

                Intent i =new Intent(ShowConfigActivity.this,ShowItemEditActivity.class);
                i.putExtra("name",mData.get(position).get("name").toString());
                i.putExtra("content",mData.get(position).get("content").toString());
                i.putExtra("position",position);
                startActivityForResult(i, 100);
                return true;
            }
        });

        etShowCode = (EditText) findViewById(R.id.et_showCode);
        etShowCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                System.out.println("进入回车事件了");
                String code = etShowCode.getText().toString().replace("\n", "");
                etShowCode.setText(code);

                if (code.equals("")) {
                    //返回的数据是空的
                    Toast.makeText(getApplicationContext(),"条形码不能为空！",Toast.LENGTH_SHORT).show();
                } else {
                    //返回的数据不空
                    //隐藏键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etShowCode.getWindowToken(), 0);

                    Cursor cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_MAINTAIN, null, MyDataBase.MT_CODE + " =?", new String[]{code}, null, null, null, null);
                    if (cursor.moveToNext()) {
                        String status = cursor.getString(cursor.getColumnIndex(MyDataBase.MT_STATUS));
                        System.out.println(status);

                        if (mData!=null){
                            mData.clear();
                            mData=null;
                        }
                        mData = GetData(cursor);


                    }else {
                        Toast.makeText(getApplicationContext(),"无记录！",Toast.LENGTH_SHORT).show();
                        if (mData!=null){
                            mData.clear();
                            mData=null;
                        }
                        mData = InitmData();
                    }

                    cursor.close();
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100&&resultCode==101){
            String name = data.getStringExtra("name");
            String content = data.getStringExtra("content");
            int position = data.getIntExtra("position",-1);
            if (position!=-1){
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name",name);
                map.put("content",content);
                mData.set(position,map);
                myAdapter.notifyDataSetChanged();

                ContentValues cv = new ContentValues();
                cv.put(MyDataBase.MT_ALL_TITLE[position],content);
                String whereClause = MyDataBase.MT_CODE + "=?";
                dbSingle.dbWriter.update(MyDataBase.TABLENAME_MAINTAIN, cv, whereClause, new String[]{etShowCode.getText().toString()});
            }
        }
    }

    private List<Map<String, Object>> GetData(Cursor cursor) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;

        for (int i = 0; i < MyDataBase.MT_ALL_TITLE.length; i++) {
            map = new HashMap<String, Object>();
            map.put("name", MyDataBase.MT_ALL_CHINESE[i] + ':');
            map.put("content", cursor.getString(i + 1).toString());
            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> InitmData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        return list;
    }

    /**
     * 在此判断是否调用GetCodeActivity
     */
    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 在此判断是否取消调用GetCodeActivity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //提取出来方便点
    public final class ViewHolder {
        public TextView title;
        public TextView info;
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public MyAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {

                holder = new ViewHolder();

                //可以理解为从vlist获取view  之后把view返回给ListView

                convertView = mInflater.inflate(R.layout.layout_show_item_new, null);
                holder.title = (TextView) convertView.findViewById(R.id.tv_showItem);
                holder.info = (TextView) convertView.findViewById(R.id.et_showItem);
//                holder.saveBtn = (Button) convertView.findViewById(R.id.btn_showItem);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText((String) mData.get(position).get("name"));
            holder.info.setText((String) mData.get(position).get("content"));

//            if ((holder.title.getText().toString()).equals(MyDataBase.MT_ALL_CHINESE[11]+":")) {
//                holder.info.setFocusable(false);
//                holder.info.setEnabled(false);
//            } else {
//                holder.info.setFocusable(true);
//                holder.info.setEnabled(true);
//            }


//
//            holder.info.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus) {
//                        //当被点击的时候，相应的设置为有效
//                        finalHolder.saveBtn.setEnabled(true);
//                    } else {
//                        //失去焦点的时候
//                        String ls = finalHolder.info.getText().toString();
//                        ((Map) mData.get(position)).put("content", ls);
//                    }
//                }
//            });
//            holder.saveBtn.setTag(position);
//            holder.saveBtn.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    System.out.println("点击的位置是" + position);
//                    //保存数据
//                    ContentValues cv = new ContentValues();
//                    cv.put(MyDataBase.MT_ALL_TITLE[position], finalHolder.info.getText().toString());
//                    String whereClause = MyDataBase.MT_CODE + "=?";
//                    dbSingle.dbWriter.update(MyDataBase.TABLENAME_MAINTAIN, cv, whereClause, new String[]{etShowCode.getText().toString()});
//
//                    finalHolder.saveBtn.setEnabled(false);
//                }
//            });

            //holder.viewBtn.setOnClickListener(MyListener(position));

            return convertView;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

    }
}
