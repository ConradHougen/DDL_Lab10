package com.example.lab10servicedemo;

import android.os.Bundle;
import android.os.Messenger;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements  ServiceHelper.EEGHandler{
    
     /** Service communication */
    Messenger mService = null;    // Messenger for communicating with the service
    boolean mIsBound;              // Flag indicating whether we have called bind on the service
    
    private Bitmap mBitMap;
    private ImageView mImage;
    private TextView mText;
    private int mImgWidth, mImgHeight;    // Size of image
    private float mWidthRatio, mHeightRatio;    // Ratio of rescaling
    private ServiceHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
         super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            mHelper = new ServiceHelper(this, this, null,"00:13:12:06:42:63");//bluetooth address
            mImage = (ImageView) findViewById(R.id.image);
            mText = (TextView) findViewById(R.id.text);
            mBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            mImgHeight = mBitMap.getHeight();
            mImgWidth = mBitMap.getWidth();
            mWidthRatio = (float)mImgWidth / (mImgHeight + mImgWidth);
            mHeightRatio = (float)mImgHeight / (mImgHeight + mImgWidth);
            
            // Calculate the center position of mImage view
            mImage.setImageBitmap(mBitMap);
            mHelper.startService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
     @Override
        public void onDestroy() {
            super.onDestroy();
            mHelper.stopService();
        }
     
     private void scale(int ratio) {
            mImgHeight -= ratio * 10 * mHeightRatio;
            mImgWidth -= ratio * 10 * mWidthRatio;
            if (mImgHeight < 0 || mImgWidth < 0) { 
                mImgHeight = mBitMap.getHeight(); 
                mImgWidth = mBitMap.getWidth();
            }
            Bitmap bitMap = Bitmap.createScaledBitmap(mBitMap, mImgWidth, mImgHeight, true);
            mImage.setImageBitmap(bitMap);
        }

        @Override
        public void onBTConnected() {
            Log.d("Lab10", "Bluetooth connected");
        }

        @Override
        public void onBTConnecting() {
            Log.d("Lab10", "Bluetooth connecting");
        }

        @Override
        public void onBTDisconnected() {
            Log.d("Lab10", "Bluetooth disconnected");
        }

        @Override
        public void onSmile(boolean status, int index) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onGrit(boolean status, int index) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onLeftGrit(boolean status, int index) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onRightGrit(boolean status, int index) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onLeftBlink(boolean status, int index) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onRightBlink(boolean status, int index) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onDataNoise(float[] quality) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onRawData(float[][] data, int index) {
            // TODO Auto-generated method stub
            int freq = 0;
            for (int i = 1; i < data[0].length; i++) {
                Log.d("Lab10", "" + data[0][i]);
                if (data[0][i] * data[0][i-1] < 0) {
                    freq++;
                }
            }
            scale(freq);
            
        }

        @Override
        public void onTraining(int result) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onBTError() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onWriteResult(int r) {
            // TODO Auto-generated method stub
            
        }

}