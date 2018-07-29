package com.example.zeryx.qms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MerchantMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_menu);
        this.setTitle(String.format("Welcome, %1$s",QMS.merchantName));
    }
}
