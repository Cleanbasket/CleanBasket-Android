package com.washappkorea.corp.cleanbasket.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.washappkorea.corp.cleanbasket.R;
import com.washappkorea.corp.cleanbasket.io.model.Address;
import com.washappkorea.corp.cleanbasket.io.model.Alarm;
import com.washappkorea.corp.cleanbasket.io.model.AppInfo;
import com.washappkorea.corp.cleanbasket.io.model.Notice;
import com.washappkorea.corp.cleanbasket.io.model.Notification;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;

import java.sql.SQLException;

public class DBHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();

    static final String DATABASE_NAME = "cleanbasket.db";
    static final int DATABASE_VERSION = 1;

    private RuntimeExceptionDao<AppInfo, Integer> appInfoDao = null;
    private RuntimeExceptionDao<OrderItem, Integer> orderItemDao = null;
    private RuntimeExceptionDao<OrderCategory, Integer> orderCategoryDao = null;
    private RuntimeExceptionDao<Address, Integer> addressDao = null;
    private RuntimeExceptionDao<Notification, Integer> notificationDao = null;
    private RuntimeExceptionDao<Notice, Integer> noticeDao = null;
    private RuntimeExceptionDao<Alarm, Integer> alarmDao = null;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, AppInfo.class);
            TableUtils.createTable(connectionSource, OrderCategory.class);
            TableUtils.createTable(connectionSource, OrderItem.class);
            TableUtils.createTable(connectionSource, Address.class);
            TableUtils.createTable(connectionSource, Notification.class);
            TableUtils.createTable(connectionSource, Notice.class);
            TableUtils.createTable(connectionSource, Alarm.class);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e("Err", e.toString());
        }
    }

    public RuntimeExceptionDao<AppInfo, Integer> getAppInfoDao() {
        if (appInfoDao == null)
            appInfoDao = getRuntimeExceptionDao(AppInfo.class);

        return appInfoDao;
    }

    public RuntimeExceptionDao<OrderCategory, Integer> getOrderCategoryDao() {
        if (orderCategoryDao == null)
            orderCategoryDao = getRuntimeExceptionDao(OrderCategory.class);

        return orderCategoryDao;
    }

    public RuntimeExceptionDao<OrderItem, Integer> getOrderItemDao() {
        if (orderItemDao == null)
            orderItemDao = getRuntimeExceptionDao(OrderItem.class);

        return orderItemDao;
    }

    public RuntimeExceptionDao<Address, Integer> getAddressDao() {
        if (addressDao == null)
            addressDao = getRuntimeExceptionDao(Address.class);

        return addressDao;
    }

    public RuntimeExceptionDao<Notification, Integer> getNotificationDao() {
        if (notificationDao == null)
            notificationDao = getRuntimeExceptionDao(Notification.class);

        return notificationDao;
    }

    public RuntimeExceptionDao<Notice, Integer> getNoticeDao() {
        if (noticeDao == null)
            noticeDao = getRuntimeExceptionDao(Notice.class);

        return noticeDao;
    }

    public RuntimeExceptionDao<Alarm, Integer> getAlarmDao() {
        if (alarmDao == null)
            alarmDao = getRuntimeExceptionDao(Alarm.class);

        return alarmDao;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int currentVersion) {

    }
}
