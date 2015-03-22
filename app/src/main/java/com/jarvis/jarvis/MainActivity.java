package com.jarvis.jarvis;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private  TextView textInput;

    private  final String TAG = "MainActivity";
    private ImageButton speakBtn;
    private ImageView speakBtnHighlight;
    ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        }else {
            // Init utils
            TextSpeechUtils.init(this);
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

    public void queryJarvisServer(String input)
    {
        showWaitDialog();
        try {
            JarvisServerUtils.getQueryResult(input, new HttpUtils.Callback() {
                @Override
                public void onResult(String responseStr) {
                    hideWaitDialog();
                    speakBtn.setImageDrawable(getResources().getDrawable(R.drawable.mic_fg));
                    if(responseStr == null || responseStr.isEmpty())
                        showErrorResponse();
                    else {
                        try {
                            HttpUtils.Callback translatorCallback  = new HttpUtils.Callback() {
                                @Override
                                public void onResult(String translatedStr) {
                                    textInput.setText(translatedStr);
                                    translatedStr = translatedStr.replace(",", "");
                                    TextSpeechUtils.speakText(translatedStr);

                                }
                            };
                            if(PrefManager.langEnglish(MainActivity.this.getApplicationContext()))
                                TranslatorUtils.translateToEnglish(responseStr, translatorCallback);
                            else
                                TranslatorUtils.translateToHindi(responseStr, translatorCallback);

                        }catch (Exception e)
                        {
                            hideWaitDialog();
                            showErrorResponse();
                        }

                    }

                }
            });
        }catch (Exception e)
        {
            hideWaitDialog();
            showErrorResponse();
        }
    }

    @Override
    public void onClick(View v) {

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
                try {
                    textInput.setText(input);
                    HttpUtils.Callback translatorCallback = new HttpUtils.Callback() {
                        @Override
                        public void onResult(String translatedText) {
                            queryJarvisServer(translatedText);
                        }
                    };
                    TranslatorUtils.translateToEnglish(input, translatorCallback);
                } catch (Exception e) {
                    textInput.setText(e.getMessage());
                    showErrorResponse();
                }
            }
        });
        speakBtnHighlight
                .animate()
                .scaleX(1.35f)
                .scaleY(1.35f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        textInput.setText("Go on, I'm listening...");
    }

    public void showWaitDialog()
    {
        if(this.waitDialog == null )
        {
            waitDialog = new ProgressDialog(this);
            waitDialog.setTitle("Processing...");
        }
        this.waitDialog.show();
    }
    public void hideWaitDialog()
    {
        if(this.waitDialog !=null && this.waitDialog.isShowing()) {
            this.waitDialog.hide();
        }
    }

    public void showErrorResponse()
    {
        String msg = "Some error has occurred while processing your request";
        if(PrefManager.langEnglish(getApplicationContext()))
            TextSpeechUtils.speakText(msg);
        else
        {
            try {
                TranslatorUtils.translateToHindi(msg, new HttpUtils.Callback() {
                    @Override
                    public void onResult(String translatedMsg) {
                        TextSpeechUtils.speakText(translatedMsg);
                    }
                });
            }catch (Exception e)
            {

            }

        }
    }

    public void showSettingsDlg()
    {
        Intent intent = new Intent(this, PreferenceActivity.class);
        startActivity(intent);
    }


}



