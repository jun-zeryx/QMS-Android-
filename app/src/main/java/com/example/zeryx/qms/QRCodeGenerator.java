package com.example.zeryx.qms;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class QRCodeGenerator extends Activity {


    Integer queueID;
    JSONObject qrData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generator);
        generateQR();

        final Handler handler = new Handler();
        final int delay = 10000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                generateQR();
                handler.postDelayed(this, delay);
            }
        }, delay);

    }

    private void generateQR() {
        queueID = getIntent().getIntExtra("queueID",0);
        qrData = new JSONObject();

        try {
            qrData.put("q_id",queueID);
            qrData.put("timestamp", System.currentTimeMillis() / 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String qrDataString = String.valueOf(qrData);

        ImageView qrCode = findViewById(R.id.qr_code_image);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrDataString, BarcodeFormat.QR_CODE,500,500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }

}
