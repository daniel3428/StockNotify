package com.example.daniellin.stocknotify;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class MyService extends IntentService {
    public static final String ACTION1 = "ACTION1";
    public static final String ACTION2 = "ACTION2";

    public MyService() {
        super("MyService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        if (ACTION1.equals(action)) {
            // do stuff...
        } else if (ACTION2.equals(action)) {
            // do some other stuff...
        } else {
            throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }
}