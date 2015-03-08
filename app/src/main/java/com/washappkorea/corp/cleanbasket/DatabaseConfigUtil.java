package com.washappkorea.corp.cleanbasket;


import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import com.washappkorea.corp.cleanbasket.io.model.AppInfo;
import com.washappkorea.corp.cleanbasket.io.model.OrderCategory;
import com.washappkorea.corp.cleanbasket.io.model.OrderItem;

import java.io.File;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    private static final Class<?>[] classes = new Class[] {
            AppInfo.class, OrderCategory.class, OrderItem.class
    };

    public static void main(String[] args) throws Exception {
        writeConfigFile(new File("/Users/ganghan-yong/Documents/forclean/cleanbasket/app/src/main/res/raw/ormlite_config.txt"), classes);
    }
}
