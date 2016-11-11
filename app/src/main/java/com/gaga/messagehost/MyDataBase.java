package com.gaga.messagehost;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库对象类，单例模式
 */
public class MyDataBase extends SQLiteOpenHelper {

    public static SQLiteDatabase dbReader = null,dbWriter = null;
    private static MyDataBase dbInstance = null;

    public static final String JSON_SIGN = "sign";
    public static final String JSON_TOTALNUM = "totalnum";
    public static final String JSON_CURRENTNUM = "currentnum";
    public static final String JSON_ORDER = "order";
    public static final String JSON_TABLENAME = "tablename";

    /**
     * 改为公共静态变量，方便外部调用
     */
    //表一，电脑配置信息表
    public static final String TABLENAME_MAINTAIN = "t_configure_message";//电脑配置信息表_表名
    public static final String MT_STATUS = "mt_status";//装备状态，MT表示MAINTAIN，表示配置表
    public static final String MT_ID_EQUIPMENTS = "mt_id_equipments";//装备编码
    public static final String MT_DATE_EQUIPMENTS = "mt_date_equipments";//装备日期
    public static final String MT_LOGO = "mt_logo";//品牌型号
    public static final String MT_CPU = "mt_cpu";//CPU参数
    public static final String MT_MAINBOARD = "mt_main_board";//主板参数
    public static final String MT_MEMORY = "mt_memory";//内存信息
    public static final String MT_HARDDISK = "mt_hard_disk";//硬盘信息
    public static final String MT_POWER = "mt_power";//电源信息
    public static final String MT_INSTORAGE = "mt_in_storage";//入库时间
    public static final String MT_OUTSTORAGE = "mt_out_storage";//出库时间
    public static final String MT_NOTE = "mt_note";//备注
    public static final String MT_CODE = "mt_code";//条形码
//表一信息集合
    public static final String[] MT_ALL_TITLE = new String[]{MT_STATUS,MT_ID_EQUIPMENTS,MT_DATE_EQUIPMENTS,MT_LOGO,
            MT_CPU,MT_MAINBOARD,MT_MEMORY,MT_HARDDISK,MT_POWER,MT_INSTORAGE,MT_OUTSTORAGE,MT_CODE,MT_NOTE};
    public static final String[] MT_ALL_CHINESE = new String[]{"装备状态","装备编码","装备日期","品牌型号",
            "CPU参数","主板参数","内存信息","硬盘信息","电源信息","入库时间","出库时间","条形编码","备注信息"};


    //表二，电脑维修信息表
    public static final String TABLENAME_REPARE = "t_repare_message";//电脑维修信息表_表名

    public static final String RP_TYPE_EQUIPMENTS = "rp_type_equipments";//装备类别
    public static final String RP_ID_EQUIPMENTS = "rp_id_equipments";//装备编码
    public static final String RP_DATE = "rp_dat";//维修日期
    public static final String RP_PHENOMENON = "rp_phenomenon";//故障现象
    public static final String RP_METHOD = "rp_method";//处理办法
    public static final String RP_SERVICE_PERSON = "rp_service_person";//维修人员
    public static final String RP_POSITION = "rp_position";//装备位置
    public static final String RP_CODE = "rp_code";//条形码
    //表二信息集合
    public static final String[] RP_ALL_TITLE = new String[]{RP_TYPE_EQUIPMENTS,RP_ID_EQUIPMENTS,RP_DATE,RP_PHENOMENON,
            RP_METHOD,RP_SERVICE_PERSON,RP_CODE,RP_POSITION};
    public static final String[] RP_ALL_CHINESE = new String[]{"装备类别","装备编码","维修日期","故障现象",
            "处理办法","维修人员","条形编码","装备位置"};

    //表三，用户信息表
    public static final String TABLENAME_USER = "t_users";//电脑维修信息表_表名
    public static final String USER_NAME = "name";//电脑维修信息表_表名
    public static final String USER_PASSWORD = "password";//电脑维修信息表_表名

    //表四，综合故障诊断
    public static final String TABLENAME_DIGNOSE = "t_bug_dignose";//电脑维修信息表_表名
    public static final String DIG_PHENOMENON1 = "dig_phenomenon1";//电脑维修信息表_表名
    public static final String DIG_PHENOMENON2 = "dig_phenomenon2";//电脑维修信息表_表名
    public static final String DIG_PHENOMENON3 = "dig_phenomenon3";//电脑维修信息表_表名
    public static final String DIG_PHENOMENON4 = "dig_phenomenon4";//电脑维修信息表_表名
    public static final String DIG_PHENOMENON5 = "dig_phenomenon5";//电脑维修信息表_表名
    public static final String DIG_PHENOMENON6 = "dig_phenomenon6";//电脑维修信息表_表名
    public static final String DIG_POSITION1 = "dig_position1";//电脑维修信息表_表名
    public static final String DIG_POSITION2 = "dig_position2";//电脑维修信息表_表名
    public static final String DIG_POSITION3 = "dig_position3";//电脑维修信息表_表名
    public static final String DIG_POSITION4 = "dig_position4";//电脑维修信息表_表名
    //表四信息集合
    public static final String[] DIG_ALL_TITLE = new String[]{DIG_PHENOMENON1,DIG_PHENOMENON2,DIG_PHENOMENON3,DIG_PHENOMENON4,DIG_PHENOMENON5,DIG_PHENOMENON6,
            DIG_POSITION1,DIG_POSITION2,DIG_POSITION3,DIG_POSITION4};

    private MyDataBase(Context context) {
        super(context, "messagehost.db", null, 1);
    }

    public static MyDataBase GetDb(Context context){
        if (dbInstance ==null){
            dbInstance = new MyDataBase(context);

            dbWriter = dbInstance.getWritableDatabase();
            dbReader = dbInstance.getReadableDatabase();
        }
        return dbInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //创建表一
        db.execSQL("CREATE TABLE " + TABLENAME_MAINTAIN + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                MT_STATUS + " VARCHAR(20)  DEFAULT \"\"," +
                MT_ID_EQUIPMENTS + " VARCHAR(20) UNIQUE DEFAULT \"\"," +
                MT_DATE_EQUIPMENTS + " VARCHAR(20) DEFAULT \"\"," +
                MT_LOGO + " VARCHAR(20) DEFAULT \"\"," +
                MT_CPU + " VARCHAR(20) DEFAULT \"\"," +
                MT_MAINBOARD + " VARCHAR(20) DEFAULT \"\"," +
                MT_MEMORY + " VARCHAR(20) DEFAULT \"\"," +
                MT_HARDDISK + " VARCHAR(20) DEFAULT \"\"," +
                MT_POWER + " VARCHAR(20) DEFAULT \"\"," +
                MT_INSTORAGE + " VARCHAR(20) DEFAULT \"\"," +
                MT_OUTSTORAGE + " VARCHAR(20) DEFAULT \"\"," +
                MT_CODE + " VARCHAR(20) DEFAULT \"\"," +
                MT_NOTE + " VARCHAR(20) DEFAULT \"\")");
        //创建表二
        db.execSQL("CREATE TABLE " + TABLENAME_REPARE + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                RP_TYPE_EQUIPMENTS + " VARCHAR(20) DEFAULT \"\"," +
                RP_ID_EQUIPMENTS + " VARCHAR(20) DEFAULT \"\"," +
                RP_DATE + " VARCHAR(20) DEFAULT \"\"," +
                RP_PHENOMENON + " VARCHAR(20) DEFAULT \"\"," +
                RP_METHOD + " VARCHAR(20) DEFAULT \"\"," +
                RP_SERVICE_PERSON + " VARCHAR(20) DEFAULT \"\"," +
                RP_CODE + " VARCHAR(20) DEFAULT \"\"," +
                RP_POSITION + " VARCHAR(20) DEFAULT \"\")");

        //创建表三
        db.execSQL("CREATE TABLE " + TABLENAME_USER + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_NAME + " VARCHAR(20) DEFAULT \"\"," +
                USER_PASSWORD + " VARCHAR(20) DEFAULT \"\")");

        //创建表四
        db.execSQL("CREATE TABLE " + TABLENAME_DIGNOSE + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                DIG_PHENOMENON1 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_PHENOMENON2 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_PHENOMENON3 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_PHENOMENON4 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_PHENOMENON5 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_PHENOMENON6 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_POSITION1 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_POSITION2 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_POSITION3 + " VARCHAR(20) DEFAULT \"\"," +
                DIG_POSITION4 + " VARCHAR(20) DEFAULT \"\")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
