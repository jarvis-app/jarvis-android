package com.jarvis.jarvis;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;


public class PreferenceActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        final RadioButton engBtn = (RadioButton) findViewById(R.id.radio_english);
        final RadioButton hindiBtn = (RadioButton) findViewById(R.id.radio_hindi);
        if(PrefManager.langEnglish(getApplicationContext()))
            engBtn.setChecked(true);
        else
            hindiBtn.setChecked(true);
        engBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefManager.setLangEnglish(getApplicationContext(), engBtn.isChecked());
                TextSpeechUtils.setSpeakerLanguage(engBtn.isChecked());
            }
        });

        hindiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefManager.setLangEnglish(getApplicationContext(), !hindiBtn.isChecked());
                TextSpeechUtils.setSpeakerLanguage(!hindiBtn.isChecked());
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preference, menu);
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
}
