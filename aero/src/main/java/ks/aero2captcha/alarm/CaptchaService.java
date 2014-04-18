package ks.aero2captcha.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class CaptchaService extends Service {

    CaptchaReceiver alarm = new CaptchaReceiver();
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setAlarm(CaptchaService.this);
        registerReceiver(alarm, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(alarm, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        return START_STICKY;
    }

    public void onStart(Context context,Intent intent, int startId)
    {
        alarm.setAlarm(context);
        registerReceiver(alarm, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(alarm, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public void onDestroy() {
        alarm.cancelAlarm(getApplicationContext());
        unregisterReceiver(alarm);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
