package com.alextim.bank.front.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
