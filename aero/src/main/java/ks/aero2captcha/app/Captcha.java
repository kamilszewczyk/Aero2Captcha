package ks.aero2captcha.app;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import ks.aero2captcha.alarm.CaptchaService;
import ks.aero2captcha.image.TouchImageView;
import ks.aero2captcha.network.Aero;
import ks.aero2captcha.network.BaseAsyncTask;
import ks.aero2captcha.network.ConnectionManager;
import ks.aero2captcha.network.State;
import ks.aero2captcha.network.TaskResult;
import ks.aero2captcha.parser.ImageParser;
import ks.aero2captcha.parser.StringParser;

public class Captcha extends ActionBarActivity {

    public final static String TAG                 = "Aero2Captcha";
    public final static String AERO_SERVER         = "http://bdi.free.aero2.net.pl:8080/";
    TextView captchaText;
    Button submit; Button refresh;
    ProgressBar progress;
    SharedPreferences sharedPref;
    String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha);

        captchaText = (TextView) findViewById(R.id.captchaText);
        submit = (Button) findViewById(R.id.captchaButton);
        refresh = (Button) findViewById(R.id.refreshButton);
        progress = (ProgressBar) findViewById(R.id.captchaProgress);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //try to download captcha on first run
        downloadCaptcha();

        //attach button listener
        refresh.setOnClickListener(refreshButtonListener);

        //start service only if user wants to be notified
        Boolean notify = sharedPref.getBoolean("notifications_new_message", true);
        if (notify) {
            startService(new Intent(this, CaptchaService.class));
        }
    }

    protected void downloadCaptcha() {
        if (!State.isConnectedMobile(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.error_no_mobile, Toast.LENGTH_LONG).show();

            return;
        }

        submit.setVisibility(View.GONE);
        refresh.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        //show active connection text instead of captcha when no captcha is needed
        try {
            Boolean captchaRequired = new Aero().execute(getApplicationContext()).get();
            Log.d(TAG, String.valueOf(captchaRequired));
            if (!captchaRequired) {
                TouchImageView image = (TouchImageView) findViewById(R.id.captchaImage);
                TextView text = (TextView) findViewById(R.id.activeConnection);
                image.setVisibility(View.GONE);
                text.setVisibility(View.VISIBLE);

                submit.setEnabled(false);
                refresh.setEnabled(false);
                progress.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.VISIBLE);
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BaseAsyncTask mAsyncTask = new BaseAsyncTask();
        mAsyncTask.setUrl(AERO_SERVER);
        mAsyncTask.setCallbackListener(callbackListener);
        mAsyncTask.setParser(new StringParser());
        mAsyncTask.addParam("viewForm", "true");
        mAsyncTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.captcha, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_refresh:
                downloadCaptcha();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public BaseAsyncTask.OnTaskCompleteListener callbackListener = new BaseAsyncTask.OnTaskCompleteListener() {
        @Override
        public void onComplete(TaskResult rs) {
            if(rs.code == TaskResult.CODE_ERROR) {
                Log.e(TAG, rs.message);
                Toast.makeText(getApplicationContext(), R.string.error_download, Toast.LENGTH_LONG).show();
                submit.setEnabled(false);
            }
            else if(rs.code == TaskResult.CODE_SUCCESS) {
                sessionId = (String) rs.getResultData();

                BaseAsyncTask imageAsyncTask = new BaseAsyncTask();
                imageAsyncTask.setUrl(AERO_SERVER + "getCaptcha.html");
                imageAsyncTask.setRequestType(ConnectionManager.GET_REQUEST);
                imageAsyncTask.setCallbackListener(imageListener);
                imageAsyncTask.setParser(new ImageParser());
                imageAsyncTask.addParam("PHPSESSID", sessionId);
                imageAsyncTask.execute();
            }
        }
    };

    public BaseAsyncTask.OnTaskCompleteListener imageListener = new BaseAsyncTask.OnTaskCompleteListener() {

        @Override
        public void onComplete(TaskResult rs) {
            if(rs.code == TaskResult.CODE_ERROR) {
                Log.e(TAG, rs.message);
                Toast.makeText(getApplicationContext(), R.string.error_download, Toast.LENGTH_LONG).show();
                submit.setEnabled(false);
                refresh.setEnabled(false);
            }
            else if(rs.code == TaskResult.CODE_SUCCESS) {
                TouchImageView image = (TouchImageView) findViewById(R.id.captchaImage);
                TextView text = (TextView) findViewById(R.id.activeConnection);
                image.setMinZoom(0.1f);
                image.setImageBitmap((Bitmap) rs.getResultData());
                image.setVisibility(View.VISIBLE);
                text.setVisibility(View.GONE);

                submit.setEnabled(true);
                refresh.setEnabled(true);
                submit.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.VISIBLE);

                progress.setVisibility(View.GONE);
                captchaText.setText("");
                captchaText.setOnEditorActionListener(enterButtonListener);

                submit.setOnClickListener(submitButtonListener);
            }
        }
    };

    public BaseAsyncTask.OnTaskCompleteListener submitListener = new BaseAsyncTask.OnTaskCompleteListener() {

        @Override
        public void onComplete(TaskResult rs) {
            if(rs.code == TaskResult.CODE_ERROR) {
                Log.e(TAG, rs.message);
                Toast.makeText(getApplicationContext(), R.string.error_submit, Toast.LENGTH_LONG).show();
                downloadCaptcha();
            }
            else if(rs.code == TaskResult.CODE_SUCCESS) {
                //remove notification
                NotificationManager notificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
                notificationManager.cancel(Aero.NOTIFICATION_ID);

                int restartDelay = Integer.parseInt(sharedPref.getString("restart_delay", "2"));
                //show toast
                Toast.makeText(getApplicationContext(), restartDelay == -1 ? R.string.success_manual : R.string.success, Toast.LENGTH_LONG).show();
                //restart connection
                if (restartDelay != -1) {
                    State.turnOnDataConnection(false, getApplicationContext());
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            State.turnOnDataConnection(true, getApplicationContext());
                        }
                    }, restartDelay);
                }

                //close activity
                finish();
            }
        }
    };

    View.OnClickListener refreshButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            downloadCaptcha();
        }
    };

    View.OnClickListener submitButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Aero.sendCaptcha(getApplicationContext(), captchaText.getText().toString(), submitListener, sessionId);
        }
    };

    TextView.OnEditorActionListener enterButtonListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Aero.sendCaptcha(getApplicationContext(), captchaText.getText().toString(), submitListener, sessionId);
                return true;
            }
            return false;
        }
    };
}

