package ks.aero2captcha.alarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpResponse;

import java.io.IOException;

import ks.aero2captcha.app.Captcha;
import ks.aero2captcha.app.R;
import ks.aero2captcha.network.Aero;
import ks.aero2captcha.network.ConnectionManager;
import ks.aero2captcha.network.State;

public class CaptchaReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (!State.isConnectedMobile(context)) {
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            cancelAlarm(context);
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            setAlarm(context);
        }
        if (intent.getAction().equals("ks.aero2captcha.START_ALARM")) {
            new Aero().execute(context);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean notify = sharedPref.getBoolean("notifications_new_message", true);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && notify) {
            context.startService(new Intent(context, CaptchaService.class));
        }
    }

    public void setAlarm(Context context)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("ks.aero2captcha.START_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        int frequency = Integer.parseInt(sharedPref.getString("sync_frequency", "10")) * 1000;
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, frequency, frequency, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent("ks.aero2captcha.START_ALARM");
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}