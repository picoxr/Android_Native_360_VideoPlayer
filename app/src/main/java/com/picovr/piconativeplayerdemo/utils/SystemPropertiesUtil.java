package com.picovr.piconativeplayerdemo.utils;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class SystemPropertiesUtil {

    public static String getSystemProperties(String key, String defaultValue) {
        try {
            final Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            final Method get = systemProperties.getMethod("get", String.class,
                    String.class);
            String result = (String) get.invoke(null, key, defaultValue);
            return TextUtils.isEmpty(result) ? defaultValue : result;
        } catch (Exception e) {
            // This should never happen
            return defaultValue;
        }
    }


    public static void setSystemProperties(String key, String value) {
        try {
            final Class<?> systemProperties = Class
                    .forName("android.os.SystemProperties");
            final Method set = systemProperties.getMethod("set", String.class,
                    String.class);
            set.invoke(null, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
