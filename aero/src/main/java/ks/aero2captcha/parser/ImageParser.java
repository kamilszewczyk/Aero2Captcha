package ks.aero2captcha.parser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import ks.aero2captcha.network.TaskResult;

/**
 * Created by Kamil on 01.04.14.
 */
public class ImageParser implements BaseParser {
    @Override
    public TaskResult parse(InputStream in) {
        TaskResult result = new TaskResult();
        Bitmap bitmap;
        String message = "";
        try {
            bitmap = parseInputStream(in);
            result.setResultData(bitmap);
            result.code = TaskResult.CODE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
            result.message = e.getMessage();
            result.code = TaskResult.CODE_ERROR;
        }

        return result;
    }

    /** Initiates the fetch operation. */
    public Bitmap parseInputStream(InputStream stream) throws IOException {
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return bitmap;
    }
}