package ks.aero2captcha.parser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ks.aero2captcha.network.TaskResult;

/**
 * Created by Kamil on 01.04.14.
 */
public class StringParser implements BaseParser {
    @Override
    public TaskResult parse(InputStream in) {
        TaskResult result = new TaskResult();
        result.code = TaskResult.CODE_ERROR;
        result.message = "";
        try {
            String str = parseInputStream(in);
            Pattern phpssidPattern = Pattern.compile("PHPSESSID=(.*?)\'");
            Matcher phpssidMatcher = phpssidPattern.matcher(str);
            if (phpssidMatcher.find()) {
                String sessionId = phpssidMatcher.group(1);
                result.setResultData(sessionId);
                result.code = TaskResult.CODE_SUCCESS;
            }
            if (str.indexOf("Niepoprawna odpowiedź.") != -1) {
                result.message = "error_answer";
            }
            if (str.indexOf("Odpowiedź prawidłowa.") != -1) {
                result.code = TaskResult.CODE_SUCCESS;
            }
        } catch (Exception e) {
            result.message = e.getMessage();
        }

        return result;
    }

    /** Initiates the fetch operation. */
    public String parseInputStream(InputStream stream) throws IOException {
        String str;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            str = total.toString();
            Log.e("captcha_log", str);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return str;
    }
}