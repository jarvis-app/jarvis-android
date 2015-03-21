package com.jarvis.jarvis;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by subhgupt on 21/03/15.
 */
public class JarvisServerUtils {

    private static String SERVER_URL = "http://192.168.4.102:9393";
    private static String TAG = "JarvisServerUtils";

    public static void getQueryResult(String query , final HttpUtils.Callback callback) throws IOException
    {
        String path = SERVER_URL + "/query/" + URLEncoder.encode(query, "UTF-8");
        HttpUtils.performGet(path, new HttpUtils.Callback() {
                    @Override
                    public void onResult(String responseStr) {
                        callback.onResult(responseStr);
                    }
                }


                );

    }
}
