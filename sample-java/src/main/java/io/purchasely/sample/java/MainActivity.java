package io.purchasely.sample.java;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.purchasely.Purchasely;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Purchasely.start(getApplicationContext(), "a9bcc756-b3a2-4e17-b46d-d5da40b6d202", "", null, null);
    }
    
}
