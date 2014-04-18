package ks.aero2captcha.network;

/**
 * Created by Kamil on 01.04.14.
 */
public class TaskResult {

    public static int CODE_ERROR = 0;
    public static int CODE_SUCCESS = 1;

    public int code = CODE_ERROR;
    public String message = null;
    private Object data = null;

    public Object getResultData() {
        return data;
    }

    public void setResultData(Object data) {
        this.data = data;
    }
}
