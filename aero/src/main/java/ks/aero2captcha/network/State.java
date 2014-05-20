package ks.aero2captcha.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Kamil on 01.04.14.
 */
public class State {
    public static boolean turnOnDataConnection(boolean flag, Context context) {
        try {
            ConnectivityManager connectivitymanager = (ConnectivityManager)context.getSystemService("connectivity");
            Field field = Class.forName(connectivitymanager.getClass().getName()).getDeclaredField("mService");
            field.setAccessible(true);
            Object obj = field.get(connectivitymanager);
            Class class1 = Class.forName(obj.getClass().getName());
            Class aclass[] = new Class[1];
            aclass[0] = Boolean.TYPE;
            Method method = class1.getDeclaredMethod("setMobileDataEnabled", aclass);
            method.setAccessible(true);
            Object aobj[] = new Object[1];
            aobj[0] = Boolean.valueOf(flag);
            method.invoke(obj, aobj);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        NetworkInfo info = State.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = State.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
}
