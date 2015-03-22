package com.jarvis.jarvis;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    enum SpeakBtnState {
        READY,
        LISTENING,
        WAITING_FOR_RESPONSE,
        RELAYING_RESPONSE,
    }

    private TextView textInput;

    private static final String TAG = "MainActivity";
    private ImageButton speakBtn;
    private ImageView speakBtnHighlight;
    private SpeakBtnState speakBtnState = SpeakBtnState.READY;
    ProgressDialog waitDialog;

    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://192.168.4.102:9393";

    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.e(TAG, Log.getStackTraceString(volleyError));
            showErrorResponse();
        }
    };

    private final Runnable setReadyStateRunnable = new Runnable() {
        @Override
        public void run() {
            setReadyState();
        }
    };

    private final UtteranceProgressListener speechDoneListener = new UtteranceProgressListener() {
        @Override public void onStart(String utteranceId) { }
        @Override public void onDone(String utteranceId) { runOnUiThread(setReadyStateRunnable); }
        @Override public void onError(String utteranceId) { runOnUiThread(setReadyStateRunnable); }
        @Override public void onError(String utteranceId, int errorCode) { runOnUiThread(setReadyStateRunnable); }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        this.speakBtnHighlight = (ImageView) findViewById(R.id.speakBtnHighlight);
        this.speakBtn = (ImageButton) findViewById(R.id.speakBtn);
        speakBtn.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakBtn.setOnTouchListener(new View.OnTouchListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getBackground().setHotspot(event.getX(), event.getY());
                    return false;
                }
            });
        }

        this.textInput = (TextView) findViewById(R.id.textInput);
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle("Processing...");
        if (!HttpUtils.isNetworkAvailable(this)) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            finish();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.setMessage(getResources().getString(R.string.InternetNotAvailableMsg)).setPositiveButton(getResources().getString(R.string.Yes), dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.No), dialogClickListener).create();
            dialog.setTitle(getResources().getString(R.string.NoInternetTitle));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            dialog.show();
        } else {
            // Init utils
            TextSpeechUtils.init(this, speechDoneListener);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsDlg();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void queryJarvisServer(String input) {
        showWaitDialog();
        try {
            Response.Listener<String> successListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String responseStr) {
                    hideWaitDialog();
                    speakBtn.setImageDrawable(getResources().getDrawable(R.drawable.mic_fg));
                    if (responseStr == null || responseStr.isEmpty())
                        showErrorResponse();
                    else {
                        try {
                            Response.Listener<String> translatorCallback = new Response.Listener<String>() {
                                @Override
                                public void onResponse(String output) {
                                    setRelayingResponseState(output);
                                }
                            };
                            if (PrefManager.langEnglish(MainActivity.this.getApplicationContext()))
                                TranslatorUtils.translateToEnglish(requestQueue, responseStr,
                                        translatorCallback, errorListener);
                            else
                                TranslatorUtils.translateToHindi(requestQueue, responseStr,
                                        translatorCallback, errorListener);
                        } catch (Exception e) {
                            hideWaitDialog();
                            showErrorResponse();
                        }

                    }
                }
            };
            StringRequest req = new StringRequest(Request.Method.GET,
                    BASE_URL + "/query/" + URLEncoder.encode(input, "UTF-8"),
                    successListener, errorListener);
            req.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(req);
        } catch (Exception e) {
            hideWaitDialog();
            showErrorResponse();
        }
    }

    @Override
    public void onClick(View v) {
        switch (speakBtnState) {
            case READY:
                setListeningState();
                break;
            case LISTENING:
                setReadyState();
                break;
            case WAITING_FOR_RESPONSE:
                setReadyState();
                break;
            case RELAYING_RESPONSE:
                break;
        }
    }

    private void setReadyState() {
        if (speakBtnState != SpeakBtnState.LISTENING &&
                speakBtnState != SpeakBtnState.WAITING_FOR_RESPONSE &&
                speakBtnState != SpeakBtnState.RELAYING_RESPONSE) {
            return;
        }
        speakBtnState = SpeakBtnState.READY;
        Log.d(TAG, speakBtnState.name());
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        speakBtnHighlight
                .animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        textInput.setText(getString(R.string.IntroText));
    }

    private void setListeningState() {
        if (speakBtnState != SpeakBtnState.READY)
            return;
        speakBtnState = SpeakBtnState.LISTENING;
        Log.d(TAG, speakBtnState.name());
        TextSpeechUtils.getVoiceInput(new TextSpeechUtils.TextSpeechUtilsCallback() {
            @Override
            public void onSpeechStart() {
            }

            @Override
            public void onPartialSpeech(String input) {
                textInput.setText(input);
            }

            @Override
            public void onSpeechInputComplete(String input) {
                setWaitingForResponseState(input);
            }
        });
        speakBtnHighlight
                .animate()
                .scaleX(1.35f)
                .scaleY(1.35f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        textInput.setText(getString(R.string.listening));
    }

    private void setWaitingForResponseState(String input) {
        if (speakBtnState != SpeakBtnState.LISTENING)
            return;
        speakBtnState = SpeakBtnState.WAITING_FOR_RESPONSE;
        Log.d(TAG, speakBtnState.name());
        try {
            textInput.setText(input);
            TranslatorUtils.translateToEnglish(requestQueue, input,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String translatedText) {
                            queryJarvisServer(translatedText);
                        }
                    },
                    errorListener);
        } catch (Exception e) {
            showErrorResponse();
        }
    }

    private void setRelayingResponseState(String output) {
        if (speakBtnState != SpeakBtnState.WAITING_FOR_RESPONSE)
            return;
        speakBtnState = SpeakBtnState.RELAYING_RESPONSE;
        Log.d(TAG, speakBtnState.name());
        textInput.setText(output);
        output = output.replace(",", "");
        TextSpeechUtils.speakText(output, speechDoneListener);
    }

    public void showWaitDialog() {
        if (this.waitDialog == null) {
            waitDialog = new ProgressDialog(this);
            waitDialog.setTitle("Processing...");
        }
        this.waitDialog.show();
    }

    public void hideWaitDialog() {
        if (this.waitDialog != null && this.waitDialog.isShowing()) {
            this.waitDialog.hide();
        }
    }

    public void showErrorResponse() {
        hideWaitDialog();
        String error = "Some error has occurred while processing your request";
        textInput.setText(error);
        TextSpeechUtils.speakText(error, speechDoneListener);
        // String msg = "Some error has occurred while processing your request";
        // if(PrefManager.langEnglish(getApplicationContext()))
        //     TextSpeechUtils.speakText(msg);
        // else
        //     {
        //         try {
        //             TranslatorUtils.translateToHindi(msg, new HttpUtils.Callback() {
        //                     @Override
        //                     public void onResult(String translatedMsg) {
        //                         TextSpeechUtils.speakText(translatedMsg);
        //                     }
        //                 });
        //         }catch (Exception e)
        //             {

        //             }

        //     }
    }

    public void showSettingsDlg() {
        Intent intent = new Intent(this, PreferenceActivity.class);
        startActivity(intent);
    }


}
