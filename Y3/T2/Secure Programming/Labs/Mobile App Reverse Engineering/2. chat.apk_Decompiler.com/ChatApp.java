package com.selector.loginapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ChatApp extends AppCompatActivity {
    private static final String TAG = "ChatApp";
    private ArrayAdapter<String> messageAdapter;
    private EditText messageEditText;
    private ListView messageListView;
    private ArrayList<String> messages;
    private Button sendButton;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat_app);
        this.messageListView = (ListView) findViewById(R.id.messageListView);
        this.messageEditText = (EditText) findViewById(R.id.messageEditText);
        this.sendButton = (Button) findViewById(R.id.sendButton);
        this.messages = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, 17367043, this.messages);
        this.messageAdapter = arrayAdapter;
        this.messageListView.setAdapter(arrayAdapter);
        ((TextView) findViewById(R.id.usernameTextView)).setText(getIntent().getStringExtra("USERNAME"));
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ChatApp.this.sendMessage();
            }
        });
    }

    /* access modifiers changed from: private */
    public void sendMessage() {
        String trim = this.messageEditText.getText().toString().trim();
        if (!trim.isEmpty()) {
            this.messages.add(trim);
            this.messageAdapter.notifyDataSetChanged();
            Log.d(TAG, "Message ID is : " + (((int) (Math.random() * 1000.0d)) + 1));
            Log.d(TAG, "Message is : " + trim);
            this.messageEditText.getText().clear();
        }
    }
}
