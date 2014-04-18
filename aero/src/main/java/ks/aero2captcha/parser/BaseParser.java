package ks.aero2captcha.parser;

import java.io.InputStream;

import ks.aero2captcha.network.TaskResult;

/**
 * Created by Kamil on 01.04.14.
 */
public interface BaseParser {
    public TaskResult parse(InputStream in);
}
