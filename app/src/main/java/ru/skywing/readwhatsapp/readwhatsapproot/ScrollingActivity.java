package ru.skywing.readwhatsapp.readwhatsapproot;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ScrollingActivity extends AppCompatActivity {

    private final static String TAG = "readwhatsapp";
    ReadWhatsApp readWhatsApp = null;
    RootUtil rootUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rootUtil = new RootUtil();
        rootUtil.CheckSU();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(!rootUtil.Can())
                {
                    Toast.makeText(getBaseContext(), "Root is not avaiable not", Toast.LENGTH_LONG);
                    return;
                }
                TextView textView = (TextView) findViewById(R.id.textview);
                try {
                    if(readWhatsApp == null)
                        readWhatsApp = new ReadWhatsApp(getBaseContext());
                }
                catch(NoSuchFieldException nfe)
                {
                    Log.v(TAG, "Assets not found");
                }
                if(readWhatsApp == null)return;
                if(readWhatsApp.getLastID() == 0)textView.setText("");
                String text = readWhatsApp.ReadNextBlock(4);
                textView.append(text);
            }
        });
    }
}
