package com.example.w7.touchtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import android.os.Handler;
import android.view.WindowManager;


import static android.content.ContentValues.TAG;
import static com.example.w7.touchtest.MyGlobals.counter;

public class TouchTest extends Activity {

    private static final boolean mDebug = false;
    private ArrayList<TouchTest.Rect> rectList;
    public TouchTest.TPTestView tpView;
    public int mDisplayWidth;
    public int mDisplayHeight;
    public int numX;
    public int numY;
    //long timeToCloseTest = 60*1000 + 38*1000;
    long timeToCloseTest = 2000;
    Handler mHandler;
    public int squareEdge;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int uiOptions = fullScreen();
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, timeToCloseTest);
        decorView.setSystemUiVisibility(uiOptions);
        initTouchPanelTest();
        tpView = new TouchTest.TPTestView(this);
        setContentView(tpView);
    }

    private void initTouchPanelTest(){

        Display myDisplay = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        myDisplay.getSize(size);
        mDisplayWidth =size.x;
        if (mDisplayWidth == 720) {
            mDisplayHeight = 1280;
            squareEdge = 80;
        }
        else if (mDisplayWidth==1080) {
            mDisplayHeight = 1920;
            squareEdge = 120;
        }
        else mDisplayHeight = size.y;

        numY = mDisplayHeight/squareEdge; //her kare 100*100 pixel
        numX = mDisplayWidth/squareEdge;

        rectList = new ArrayList<TouchTest.Rect>(numX * numY);

        for(int i = 0; i < numX; i++){
            for(int j = 0; j < numY; j++){
                TouchTest.Rect r = new TouchTest.Rect(i*mDisplayWidth/numX,j*mDisplayHeight/numY,mDisplayWidth/numX,mDisplayHeight/numY);
                rectList.add(r);
            }
        }
    }

    private class TPTestView extends View {
        private Bitmap bitmap;
        private Canvas canvas;
        private Paint paint;
        private TouchTest.Rect tempRect;


        public TPTestView(Context context){
            super(context);
            paint=new Paint(Paint.DITHER_FLAG);
            bitmap = Bitmap.createBitmap(mDisplayWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);
            canvas=new Canvas();
            canvas.setBitmap(bitmap);

            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            Iterator<TouchTest.Rect> iterator = rectList.iterator();
            while(iterator.hasNext()){
                TouchTest.Rect rect = iterator.next();
                canvas.drawRect(rect.x+1, rect.y+1, rect.x+rect.dx-1, rect.y+rect.dy-1, paint);
            }
            invalidate();
            paint.setColor(Color.BLUE);
        }

        protected void onDraw(Canvas canvas){
            canvas.drawBitmap(bitmap,0,0,null);
        }

        public boolean onTouchEvent(MotionEvent event){
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();

            if(mDebug)
                Log.v(TAG, "onTouchEvent" + " x=" + x + " y=" + y);

            if( action== MotionEvent.ACTION_DOWN){

                boolean bNeedFindRect = false;
                if(tempRect==null){
                    bNeedFindRect = true;
                }
                else{
                    if(!tempRect.isInRect((int)x, (int)y)){
                        bNeedFindRect = true;
                    }else {
                        bNeedFindRect = false;
                    }
                }
                if(mDebug) Log.v(TAG, "onTouchEvent" + " bNeedFindRect=" + bNeedFindRect);
                if(bNeedFindRect){
                    Iterator<TouchTest.Rect> iterator = rectList.iterator();
                    while(iterator.hasNext()){
                        TouchTest.Rect rect = iterator.next();
                        if(rect.isInRect((int)x, (int)y)){
                            tempRect = rect;
                            canvas.drawRect(rect.x, rect.y, rect.x+rect.dx, rect.y+rect.dy, paint);
                            invalidate();
                            rectList.remove(rect);
                            break;
                        }
                    }
                }

                if (rectList.size() == 0) {
                    writeToFile("PASS");
                    mHandler.removeCallbacks(mRunnable);
                    finish();
                }
            }
            return true;
        }
    }

    private class Rect{
        int x,y,dx,dy;
        Rect(int x, int y, int dx, int dy)	{this.x = x;this.y = y;this.dx = dx;this.dy = dy;}

        public boolean isInRect(int x, int y){
            if(mDebug)
                Log.v(TAG, "isInRect" + " this.x=" + this.x + " this.y=" + this.y + " this.x+dx=" + (this.x + this.dx) + " this.y+dy=" + (this.y + this.dy));
            if(x>=this.x && (x<=this.x+this.dx) && y>=this.y && (y<=this.y+this.dy)){
                return true;
            }else{
                return false;
            }
        }
    }

    private void writeToFile(String result){

        SharedPreferences counterSaved = getSharedPreferences("savedCounter", Activity.MODE_PRIVATE);
        counter = counterSaved.getInt("savedCounter", -1);

        String filename = "results.txt";
        counter = counter + 1;
        String log = "  " + counter + ":" + resultTime() + " :" + result + "\n";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(log.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sp = getSharedPreferences("savedCounter", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("savedCounter", counter);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public Date resultTime(){
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
        return now;
    }

    public int fullScreen(){
        int fullScreenInt = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
                /*| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;*/
        return fullScreenInt;
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("Timer", "Calls");
            writeToFile("FAIL!!");
            takeScreenshot();
            mHandler.removeCallbacks(mRunnable);
            finish();
        }
    };
}
