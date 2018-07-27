package com.vodka2.gmscredentials;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vodka2.gmscredentials.helper.Credentials;
import com.vodka2.gmscredentials.helper.CredentialsException;
import com.vodka2.gmscredentials.helper.CredentialsHelper;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends AppCompatActivity {

    private CredentialsHelper _helper;
    private HandlerThread _thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            _helper = new CredentialsHelper();
            _thread = new HandlerThread("GMS Credentials bg thread");
            _thread.start();
            findViewById(R.id.get_creds).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((TextView)findViewById(R.id.status_text)).setText(R.string.wait);
                    new Handler(_thread.getLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Credentials creds = _helper.getCredentials();
                                new Handler(getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((EditText) findViewById(R.id.gms_id)).setText(creds.id);
                                        ((EditText) findViewById(R.id.gms_token)).setText(creds.token);
                                        ((TextView) findViewById(R.id.status_text)).setText(R.string.ok);
                                    }
                                });
                            } catch (final CredentialsException e) {
                                new Handler(getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((TextView) findViewById(R.id.status_text)).setText(e.getMessage());
                                    }
                                });
                            }
                        }
                    });
                }
            });
        } catch (XmlPullParserException ignored) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        _thread.quit();
    }
}
