package com.washappkorea.corp.cleanbasket.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.washappkorea.corp.cleanbasket.io.model.AppInfo;
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

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, AppInfo.class);
            TableUtils.createTable(connectionSource, OrderCategory.class);
            TableUtils.createTable(connectionSource, OrderItem.class);
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

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int currentVersion) {

    }
}
