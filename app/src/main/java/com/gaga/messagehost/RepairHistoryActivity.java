package com.gaga.messagehost;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by re on 2016/5/2.
 */
public class RepairHistoryActivity extends AppCompatActivity {

    private EditText etRPHistoryCode = null;
    private ListView lvRPhistory = null;
    private MyDataBase dbSingle = MyDataBase.GetDb(this);

    private List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();//先创建一个，以后就不用担心null指针异常问题

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_repairhistory);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("维修经验查询");
//        toolbar.setSubtitle("linyuan");
        toolbar.setLogo(R.drawable.zoom);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.goback_bg);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        lvRPhistory = (ListView)findViewById(R.id.lv_repairhistory);

        final MyAdapter myAdapter = new MyAdapter(this);
        lvRPhistory.setAdapter(myAdapter);

        etRPHistoryCode = (EditText)findViewById(R.id.et_repairhistoryCode);
        etRPHistoryCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = etRPHistoryCode.getText().toString();
                str = str.replace("\n","");
                str = str.replace(" ","");
                if (!str.equals("")) {
                    Cursor cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_REPARE, null, MyDataBase.RP_PHENOMENON + " LIKE ?", new String[]{"%" + str + "%"}, null, null, null, null);
                    mData = GetData(cursor);
                    myAdapter.notifyDataSetChanged();
                    cursor.close();
                }
            }
        });

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            String code = etRPHistoryCode.getText().toString().replace("\n", "");
            code = code.replace(" ","");
            etRPHistoryCode.setText(code);

            if (code.equals("")) {
                //返回的数据是空的
            } else {
                //返回的数据不空
                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etRPHistoryCode.getWindowToken(), 0);

                Cursor cursor = dbSingle.dbReader.query(MyDataBase.TABLENAME_REPARE, null, MyDataBase.RP_PHENOMENON + " LIKE ?", new String[]{"%"+code+"%"}, null, null, null, null);
                mData = GetData(cursor);
                cursor.close();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private List<Map<String, Object>> GetData(Cursor cursor) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = null;

        if (cursor.getCount() == 0){

            map = new HashMap<String, Object>();
            map.put("name", "无记录");
            map.put("content", "");
            map.put("section",true);
            list.add(map);

            Toast.makeText(this, "未查詢到!", Toast.LENGTH_SHORT).show();
            //置空
            return list;
        }

        int count = 1;
        while (cursor.moveToNext()) {

            map = new HashMap<String, Object>();
            map.put("name", "记录"+count+"：");
            map.put("content","");
            map.put("section",true);
            count++;
            list.add(map);

            for (int i = 2; i < MyDataBase.RP_ALL_TITLE.length-2; i++) {
                map = new HashMap<String, Object>();
                map.put("name", MyDataBase.RP_ALL_CHINESE[i] + ':');
                map.put("content", cursor.getString(i + 1).toString());
                map.put("section",false);
                list.add(map);
            }

        }
        return list;
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

                convertView = mInflater.inflate(R.layout.layout_repaircheck_item, null);
                holder.title = (TextView) convertView.findViewById(R.id.tv_RPName);
                holder.info = (TextView) convertView.findViewById(R.id.tv_RPCheck_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

//            ViewHolder holder  = new ViewHolder();
//            convertView = mInflater.inflate(R.layout.layout_repaircheck_item, null);
//            holder.title = (TextView) convertView.findViewById(R.id.tv_RPName);
//            holder.info = (TextView) convertView.findViewById(R.id.tv_RPCheck_item);


            if ((Boolean) (mData.get(position).get("section")) ){
                holder.info.setVisibility(View.GONE);
                convertView.setBackgroundColor(parent.getResources().getColor(R.color.green_light));
            }else {
                holder.info.setVisibility(View.VISIBLE);
                convertView.setBackgroundColor(parent.getResources().getColor(R.color.test));
            }

            holder.title.setText((String) mData.get(position).get("name"));
            holder.info.setText((String) mData.get(position).get("content"));
            if ((holder.title.getText().toString()).equals(MyDataBase.MT_ALL_CHINESE[11]+":")) {
                holder.info.setFocusable(false);
                holder.info.setEnabled(false);
            } else {
                holder.info.setFocusable(true);
                holder.info.setEnabled(true);
            }
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
