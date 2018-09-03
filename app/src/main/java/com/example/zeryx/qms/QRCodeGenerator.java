package com.example.zeryx.qms;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class QRCodeGenerator extends Activity {


    Integer queueID;
    JSONObject qrData;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generator);
        generateQR();

        final Handler handler = new Handler();
        final int delay = 5000; //milliseconds

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
            qrData.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }

 
        String qrDataString = String.valueOf(qrData);

        ImageView qrCode = findViewById(R.id.qr_code_image);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            byte[] base64data = qrDataString.getBytes("UTF-8");
            String base64string = Base64.encodeToString(base64data, Base64.DEFAULT);

            BitMatrix bitMatrix = multiFormatWriter.encode(base64string, BarcodeFormat.QR_CODE,1000,1000);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) pixels[offset + x] = bitMatrix.get(x, y) ? BLACK : WHITE;
            }

            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            bitmap.setConfig(Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            qrCode.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            Log.e("QMS", "Invalid QR Data");
        }
    }

}
