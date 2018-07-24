package com.example.zeryx.qms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.setTitle(String.format("Welcome, %1$s %2$s",QMS.lastName,QMS.firstName));
    }
}
