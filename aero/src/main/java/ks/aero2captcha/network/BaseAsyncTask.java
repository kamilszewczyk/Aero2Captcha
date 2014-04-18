package ks.aero2captcha.network;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

import ks.aero2captcha.parser.BaseParser;

/**
 * Created by Kamil on 01.04.14.
 */
public class BaseAsyncTask extends AsyncTask<Void, Integer, TaskResult> {

    ConnectionManager mConnectionManager = null;
    OnTaskCompleteListener mListener;
    BaseParser mParser;

    public BaseAsyncTask() {
        mConnectionManager = new ConnectionManager();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TaskResult doInBackground(Void... params) {
        String message = "";
        try {
            InputStream in = mConnectionManager.getHttpInputStream();
            return mParser.parse(in);
        } catch (IOException e) {
            e.printStackTrace();
            message = e.getMessage();
        } catch(Exception e) {
            e.printStackTrace();
            message = e.getMessage();
        }

        TaskResult rs = new TaskResult();
        rs.message = "Internal error. Please try later." + message;
        return rs;
    }

    @Override
    protected void onPostExecute(TaskResult result) {
        super.onPostExecute(result);
        mListener.onComplete(result);
    }

    public void setUrl(String url) {
        mConnectionManager.setUrl(url);
    }

    public void setRequestType(int type) {
        mConnectionManager.setRequestType(type);
    }

    public void setCallbackListener(OnTaskCompleteListener listener) {
        this.mListener = listener;
    }

    public void addParam(String name, String value) {
        mConnectionManager.addParam(name, value);
    }

    public void setParser(BaseParser parser) {
        this.mParser = parser;
    }

    public interface OnTaskCompleteListener {
        public void onComplete(TaskResult rs);
    }

}