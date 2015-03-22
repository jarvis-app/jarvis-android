package com.jarvis.jarvis;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TranslatorUtils {

    public static final String HINDI_TO_ENG = "http://translate.google.com/translate_a/t?client=t&hl=en&sl=" +
            "hi" + "&tl=" + "en" + "&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=2&ssel=0&tsel=0&sc=1&q=";
    public static final String ENG_TO_HINDI = "http://translate.google.com/translate_a/t?" +
            "client=t&hl=hi&sl=en&tl=hi&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=2&ssel=0&tsel=0&sc=1&q=";

    public static void translateToEnglish(RequestQueue requestQueue, String text,
                                          final Response.Listener<String> successListener,
                                          final Response.ErrorListener errorListener)
            throws UnsupportedEncodingException {
        if (isEnglish(text)) {
            successListener.onResponse(text);
            return;
        }

        StringRequest req = new StringRequest(Request.Method.GET,
                HINDI_TO_ENG + URLEncoder.encode(text, "UTF-8"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        successListener.onResponse(responseStr.split("(?<!\\\\)\"")[1]);
                    }
                }, errorListener);
        req.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    public static void translateToHindi(RequestQueue requestQueue, String text,
                                        final Response.Listener<String> successListener,
                                        final Response.ErrorListener errorListener)
            throws UnsupportedEncodingException {
        if (!isEnglish(text)) {
            successListener.onResponse(text);
            return;
        }

        StringRequest req = new StringRequest(Request.Method.GET,
                ENG_TO_HINDI + URLEncoder.encode(text, "UTF-8"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseStr) {
                        successListener.onResponse(responseStr.split("(?<!\\\\)\"")[1]);
                    }
                }, errorListener);
        req.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(req);
    }

    public static boolean isEnglish(String input) {
        boolean isEnglish = true;
        for (char c : input.toCharArray()) {
            if (Character.UnicodeBlock.of(c) != Character.UnicodeBlock.BASIC_LATIN) {
                isEnglish = false;
                break;
            }
        }
        return isEnglish;
    }


}
