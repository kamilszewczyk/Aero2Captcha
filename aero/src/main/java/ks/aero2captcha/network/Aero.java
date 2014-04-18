package ks.aero2captcha.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.apache.http.HttpResponse;

import java.io.IOException;

import ks.aero2captcha.app.Captcha;
import ks.aero2captcha.app.R;

public class Aero extends AsyncTask<Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... contexts) {

        if (isCaptchaRequired()) {
            Context context = contexts[0];
            Intent mIntent = new Intent(context, Captcha.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, mIntent, 0);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.captcha_required))
                    .setSmallIcon(R.drawable.logo)
                    .setContentIntent(pIntent)
                    .setSound(Uri.parse(sharedPref.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound")));;

            Notification note = mBuilder.build();
            Boolean vibrate = sharedPref.getBoolean("notifications_new_message_vibrate", true);
            if (vibrate) {
                note.defaults |= Notification.DEFAULT_VIBRATE;
            }

            note.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, note);
        }

        return null;
    }

    public static boolean isCaptchaRequired() {
        ConnectionManager manager = new ConnectionManager();
        manager.setConnectionTimeout(5000);
        manager.setSocketTimeout(5000);
        manager.setRequestType(ConnectionManager.GET_REQUEST);
        manager.setUrl(Captcha.AERO_SERVER);

        try {
            HttpResponse response = manager.getHttpResponse();

            return true;
        } catch (IOException e) {

        }

        return false;
    }
}