package com.jarvis.jarvis;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private  TextView textInput;

    private  final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button speakBtn = (Button)findViewById(R.id.speakBtn);
        speakBtn.setOnClickListener(this);
        this.textInput = (TextView) findViewById(R.id.textInput);

        // Init utils
        TextSpeechUtils.init(this);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.speakBtn:

                    TextSpeechUtils.getVoiceInput(new TextSpeechUtils.TextSpeechUtilsCallback() {
                        @Override
                        public void onSpeechInputComplete(String input) {
                            //String englishText = Translate.
                            try {
                                TranslatorUtils.translate(input, new HttpUtils.Callback() {
                                    @Override
                                    public void onResult(String translatedText) {
                                        try {
                                            JarvisServerUtils.getQueryResult(translatedText, new HttpUtils.Callback() {
                                                @Override
                                                public void onResult(String responseStr) {
                                                    textInput.setText(responseStr);
                                                    TextSpeechUtils.speakText(responseStr);
                                                }
                                            });
                                        }catch (Exception e)
                                        {
                                            textInput.setText(e.getMessage());
                                        }
                                    }
                                });
                            }catch (Exception e)
                            {
                                textInput.setText(e.getMessage());
                            }

                        }
                    });

                break;
        }

    }

}