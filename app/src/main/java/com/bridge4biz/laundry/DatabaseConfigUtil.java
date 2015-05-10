package com.bridge4biz.laundry;


import com.bridge4biz.laundry.io.model.Address;
import com.bridge4biz.laundry.io.model.AppInfo;
import com.bridge4biz.laundry.io.model.District;
import com.bridge4biz.laundry.io.model.Notice;
import com.bridge4biz.laundry.io.model.Notification;
import com.bridge4biz.laundry.io.model.OrderCategory;
import com.bridge4biz.laundry.io.model.OrderItem;
import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    private static final Class<?>[] classes = new Class[] {
            AppInfo.class, OrderCategory.class, OrderItem.class, Address.class, Notification.class, Notice.class, District.class
    };

    public static void main(String[] args) throws Exception {
        writeConfigFile(new File("/Users/ganghan-yong/Documents/forclean/cleanbasket/app/src/main/res/raw/ormlite_config.txt"), classes);
    }
}
