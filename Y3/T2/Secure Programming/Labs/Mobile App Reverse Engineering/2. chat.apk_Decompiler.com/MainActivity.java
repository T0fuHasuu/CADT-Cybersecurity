package com.selector.loginapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.username);
        ((MaterialButton) findViewById(R.id.loginbtn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String obj = textView.getText().toString();
                Log.d(MainActivity.TAG, "Daily attemp number :" + (((int) (Math.random() * 1000.0d)) + 1));
                Log.d(MainActivity.TAG, "Session ID is :" + LoginAppAes.DecryptAES("k/ar5urlyHajpEidXTv1xU0yVUpyNeZ2Pzt7GE/VNO435gT698R264jc971B1TT4", "0123456789abcdef".getBytes(), "1234567890abcdef".getBytes()));
                Log.d(MainActivity.TAG, (((int) (Math.random() * 100.0d)) + 1) + "th User");
                MainActivity.this.openchat(obj);
            }
        });
    }

    public void openchat(String str) {
        Intent intent = new Intent(this, ChatApp.class);
        intent.putExtra("USERNAME", str);
        startActivity(intent);
    }
}
