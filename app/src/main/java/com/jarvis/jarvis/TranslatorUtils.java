package com.jarvis.jarvis;

/**
 * Created by subhgupt on 21/03/15.
 */

import java.io.IOException;
import java.net.URLEncoder;
public class TranslatorUtils{

    public static void translate(String text, final HttpUtils.Callback callback) throws IOException{
        // fetch
        //http://translate.google.com/translate_a/t?client=t&text='.$word.'&hl=en&sl=hi&tl=en&ie=UTF-8&oe=UTF-8&multires=1&otf=1&pc=1&trs=1&ssel=3&tsel=6&sc=1
        String url = "http://translate.google.com/translate_a/t?client=t&hl=en&sl=" +
                "hi" + "&tl=" + "en" + "&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=2&ssel=0&tsel=0&sc=1&q=" +
                URLEncoder.encode(text, "UTF-8");
        HttpUtils.performGet(url, new HttpUtils.Callback() {
            @Override
            public void onResult(String responseStr) {
                    StringBuilder sb = new StringBuilder();
                    String[] splits = responseStr.split("(?<!\\\\)\"");
                    for(int i = 1; i < splits.length; i += 8) {
                        sb.append(splits[i]);
                        break;
                    }
                    responseStr =  sb.toString();
                    callback.onResult(responseStr);

            }
        });

    }
}