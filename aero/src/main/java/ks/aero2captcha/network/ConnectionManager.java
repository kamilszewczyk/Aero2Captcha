package ks.aero2captcha.network;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Kamil on 01.04.14.
 */
public class ConnectionManager {

    private ArrayList<NameValuePair> params;
    private  ArrayList <NameValuePair> headers;
    private URI url;
    HttpResponse httpResponse;

    public static int GET_REQUEST = 0;
    public static int POST_REQUEST = 1;

    public static int CONNECTION_TIMEOUT = 5000;
    public static int SOCKET_TIMEOUT = 10000;

    private int mRequestType = POST_REQUEST;
    private int mConnectionTimeout = CONNECTION_TIMEOUT;
    private int mSocketTimeout = SOCKET_TIMEOUT;

    public ConnectionManager() {
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public void addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    public void setRequestType(int type) {
        mRequestType = type;
    }
    public void setConnectionTimeout(int timeout) {
        mConnectionTimeout = timeout;
    }
    public void setSocketTimeout(int timeout) {
        mSocketTimeout = timeout;
    }


    public void setUrl(String url) {
        try {
            this.url = new URI(url);
        } catch (URISyntaxException e) {
        }
    }

    public HttpResponse getHttpResponse() throws IOException {
        if(url == null) {
            throw new NullPointerException();
        }
        System.setProperty("http.keepAlive", "false");

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, mConnectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParameters, mSocketTimeout);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

        if(mRequestType == POST_REQUEST) {
            HttpPost request = new HttpPost(url);

            if(!params.isEmpty()){
                HttpEntity httpEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                request.setEntity(httpEntity);
            }

            DefaultHttpClient mClient = new DefaultHttpClient(httpParameters);
            httpResponse = mClient.execute(request);
            return httpResponse;
        }
        else if(mRequestType == GET_REQUEST) {
            String queryString = "";
            for(int i = 0; i < params.size(); i++) {
                if(i == 0) {
                    queryString = queryString + params.get(i).getName( ) + "=" + params.get(i).getValue();
                }
                else {
                    queryString = queryString + "&" + params.get(i).getName( ) + "=" + params.get(i).getValue();
                }
            }
            DefaultHttpClient mClient = new DefaultHttpClient(httpParameters);
            HttpGet httpGet = new HttpGet(url + queryString);

            httpResponse = mClient.execute(httpGet);
            return httpResponse;
        }
        return null;
    }

    public InputStream getHttpInputStream() throws ClientProtocolException, Exception {
        if (httpResponse == null) {
            httpResponse = getHttpResponse();
        }
        HttpEntity mEntity = httpResponse.getEntity();
        if (mEntity != null) {
            return mEntity.getContent();
        }
        return null;
    }

}